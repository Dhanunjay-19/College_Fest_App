package com.college.fest.controller;

import com.college.fest.entity.Registration;
import com.college.fest.entity.Student;
import com.college.fest.repository.RegistrationRepository;
import com.college.fest.repository.StudentRepository;
import com.college.fest.service.ExcelHelperService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminController {

    private final StudentRepository studentRepo;
    private final RegistrationRepository registrationRepo;
    private final ExcelHelperService excelHelperService;

    public AdminController(StudentRepository studentRepo, RegistrationRepository registrationRepo, ExcelHelperService excelHelperService) {
        this.studentRepo = studentRepo;
        this.registrationRepo = registrationRepo;
        this.excelHelperService = excelHelperService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();

        long totalEligible = studentRepo.count();
        long totalRegistered = registrationRepo.count();

        // Count how many registrations have the CHECKED_IN status
        long totalCheckedIn = registrationRepo.findAll().stream()
                .filter(r -> "CHECKED_IN".equals(r.getStatus()))
                .count();

        stats.put("totalEligible", totalEligible);
        stats.put("totalRegistered", totalRegistered);
        stats.put("totalCheckedIn", totalCheckedIn);

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/students/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();

        if (excelHelperService.hasExcelFormat(file)) {
            try {
                // 1. Parse all students from the Excel file
                List<Student> uploadedStudents = excelHelperService.excelToStudents(file.getInputStream());

                if (uploadedStudents.isEmpty()) {
                    response.put("message", "The file is empty or missing required headers (Application Id, Application Name).");
                    return ResponseEntity.badRequest().body(response);
                }

                // 2. Extract just the roll numbers into a list
                List<String> uploadedRollNumbers = uploadedStudents.stream()
                        .map(Student::getRollNumber)
                        .toList();

                // 3. Check the database to see which of these roll numbers already exist
                Set<String> existingRollNumbers = studentRepo.findExistingRollNumbers(uploadedRollNumbers);

                // 4. Filter the list to only include brand new students
                List<Student> newStudents = uploadedStudents.stream()
                        .filter(s -> !existingRollNumbers.contains(s.getRollNumber()))
                        .toList();

                // 5. Determine the response based on what we found
                if (newStudents.isEmpty()) {
                    response.put("message", "Data in the excel is already uploaded. All " + uploadedStudents.size() + " students currently exist in the database.");
                    return ResponseEntity.ok(response);
                }

                // 6. Save only the new students
                studentRepo.saveAll(newStudents);

                if (existingRollNumbers.isEmpty()) {
                    response.put("message", "Uploaded successfully: " + file.getOriginalFilename() + ". Imported all " + newStudents.size() + " students.");
                } else {
                    response.put("message", "Partial upload successful. Added " + newStudents.size() + " new students. (" + existingRollNumbers.size() + " students were already in the system).");
                }

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                response.put("message", "Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        }

        response.put("message", "Please upload an Excel file (.xlsx or .xls)!");
        return ResponseEntity.badRequest().body(response);
    }

    // Import these:
    // import org.springframework.core.io.InputStreamResource;
    // import org.springframework.http.HttpHeaders;
    // import org.springframework.http.MediaType;

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> downloadReport() {
        // Fetch all students and registrations
        List<Student> allStudents = studentRepo.findAll();
        List<Registration> allRegistrations = registrationRepo.findAll();

        ByteArrayInputStream in = excelHelperService.exportStudentsToExcel(allStudents, allRegistrations);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=Fest_Report.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}