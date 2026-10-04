package com.college.fest.service;

import com.college.fest.entity.Registration;
import com.college.fest.entity.Student;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExcelHelperService {

    // MIME types for both .xlsx and .xls
    public static String TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static String TYPE_XLS = "application/vnd.ms-excel";

    public boolean hasExcelFormat(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        return TYPE_XLSX.equals(contentType) || TYPE_XLS.equals(contentType)
                || (filename != null && (filename.endsWith(".xlsx") || filename.endsWith(".xls")));
    }

    public List<Student> excelToStudents(InputStream is) {
        // WorkbookFactory automatically detects if it is an .xls or .xlsx file
        try (Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<Student> students = new ArrayList<>();
            Map<String, Integer> headerMap = new HashMap<>();

            for (Row row : sheet) {
                // 1. Keep searching for headers until we find "Application Id"
                if (!headerMap.containsKey("Application Id") || !headerMap.containsKey("Application Name")) {
                    headerMap.clear(); // Clear any junk from title rows

                    for (Cell cell : row) {
                        if (cell.getCellType() == CellType.STRING) {
                            String headerValue = cell.getStringCellValue().trim();
                            // Make it case-insensitive just in case
                            if (headerValue.equalsIgnoreCase("Application Id")) headerMap.put("Application Id", cell.getColumnIndex());
                            if (headerValue.equalsIgnoreCase("Application Name")) headerMap.put("Application Name", cell.getColumnIndex());
                            if (headerValue.equalsIgnoreCase("Self-Reported Course")) headerMap.put("Self-Reported Course", cell.getColumnIndex());
                        }
                    }

                    // If we found the headers in this row, skip to the next row to read the data
                    if (headerMap.containsKey("Application Id") && headerMap.containsKey("Application Name")) {
                        continue;
                    }
                    continue; // Keep looping to the next row to find headers
                }

                // 2. Parse the Data Rows based on the discovered column indexes
                Student student = new Student();

                // Extract Application Id -> Roll Number
                Integer idCol = headerMap.get("Application Id");
                if (idCol != null && row.getCell(idCol) != null) {
                    student.setRollNumber(getCellValueAsString(row.getCell(idCol)));
                }

                // Extract Application Name -> Full Name
                Integer nameCol = headerMap.get("Application Name");
                if (nameCol != null && row.getCell(nameCol) != null) {
                    student.setFullName(getCellValueAsString(row.getCell(nameCol)));
                }

                // Extract Self-Reported Course -> Branch
                Integer courseCol = headerMap.get("Self-Reported Course");
                if (courseCol != null && row.getCell(courseCol) != null) {
                    student.setBranch(getCellValueAsString(row.getCell(courseCol)));
                }

                student.setIsEligible(true);

                // Only add to list if we successfully found a Roll Number
                if (student.getRollNumber() != null && !student.getRollNumber().trim().isEmpty()) {
                    students.add(student);
                }
            }
            return students;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Excel file. Error: " + e.getMessage());
        }
    }

    // Helper to safely extract cell values whether they are formatted as text or numbers in Excel
    private String getCellValueAsString(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            // Replace the non-breaking space (\u00A0) with an empty string, THEN trim standard spaces
            return cell.getStringCellValue().replace("\u00A0", "").trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return "";
    }

    // Import these at the top if not already present:
    // import com.college.fest.entity.Registration;
    // import org.springframework.core.io.InputStreamResource;
    // import java.io.ByteArrayInputStream;
    // import java.io.ByteArrayOutputStream;
    // import java.util.stream.Collectors;

    public ByteArrayInputStream exportStudentsToExcel(List<Student> students, List<Registration> registrations) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Fest Data Report");

            // 1. Create Header Row (Added "Unique Code" here)
            Row headerRow = sheet.createRow(0);
            String[] columns = {"S.No", "Student Name", "Dost_ID (Roll No)", "Branch", "Unique Code", "Registered", "Checked In", "Checked In Time"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // 2. Map registrations by Student ID for instant lookup
            Map<Long, Registration> regMap = registrations.stream()
                    .collect(Collectors.toMap(r -> r.getStudent().getId(), r -> r));

            // Define a clean formatter for the check-in time
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

            // 3. Fill Data Rows
            int rowIdx = 1;
            for (Student student : students) {
                Row row = sheet.createRow(rowIdx);
                Registration reg = regMap.get(student.getId());

                row.createCell(0).setCellValue(rowIdx);
                row.createCell(1).setCellValue(student.getFullName());
                row.createCell(2).setCellValue(student.getRollNumber());
                row.createCell(3).setCellValue(student.getBranch() != null ? student.getBranch() : "N/A");

                // --- NEW: Add Unique Code ---
                // If the code is inside the Registration entity:
                String uniqueCode = (reg != null && reg.getUniqueCode() != null) ? reg.getUniqueCode() : "N/A";
                row.createCell(4).setCellValue(uniqueCode);

                boolean isRegistered = (reg != null);
                row.createCell(5).setCellValue(isRegistered ? "Yes" : "No");

                boolean isCheckedIn = isRegistered && "CHECKED_IN".equals(reg.getStatus());
                row.createCell(6).setCellValue(isCheckedIn ? "Yes" : "No");

                // Format the time cleanly instead of raw .toString()
                String time = "-";
                if (isCheckedIn && reg.getCheckedInAt() != null) {
                    time = reg.getCheckedInAt().format(formatter);
                }
                row.createCell(7).setCellValue(time); // Shifted to index 7

                rowIdx++;
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Failed to export data to Excel: " + e.getMessage());
        }
    }}