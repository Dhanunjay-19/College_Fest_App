package com.college.fest.service;

import com.college.fest.dto.RegistrationRequest;
import com.college.fest.dto.RegistrationResponse;
import com.college.fest.entity.Registration;
import com.college.fest.entity.Student;
import com.college.fest.repository.RegistrationRepository;
import com.college.fest.repository.StudentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepo;
    private final StudentRepository studentRepo;
    private final CodeGeneratorService codeGenerator;

    public RegistrationService(RegistrationRepository registrationRepo, StudentRepository studentRepo, CodeGeneratorService codeGenerator) {
        this.registrationRepo = registrationRepo;
        this.studentRepo = studentRepo;
        this.codeGenerator = codeGenerator;
    }

    @Transactional
    public RegistrationResponse registerStudent(RegistrationRequest request) {
        // 1. Clean the input to prevent "hidden space" bugs from the frontend
        String cleanRollNumber = request.rollNumber() != null ? request.rollNumber().trim() : "";

        // ADD THESE TWO LINES:
        System.out.println("1. Raw Payload Received: " + request);
        System.out.println("2. Searching DB for EXACT Roll Number: [" + cleanRollNumber + "]");

        Optional<Student> studentOpt = studentRepo.findByRollNumber(cleanRollNumber);

        if (studentOpt.isEmpty()) {
            return new RegistrationResponse(null, "ERROR", "Invalid Roll Number. Student not found.");
        }

        Student student = studentOpt.get();

        if (!student.getIsEligible()) {
            return new RegistrationResponse(null, "ERROR", "Student is not eligible to attend the fest.");
        }

        // 2. Validate Name
        if (request.fullName() == null || !student.getFullName().trim().equalsIgnoreCase(request.fullName().trim())) {
            return new RegistrationResponse(null, "ERROR", "Provided name does not match our records.");
        }

        // 3. NEW: Validate Department (Ensure your Student entity uses getDepartment())
        if (request.branch() == null || !student.getBranch().trim().equalsIgnoreCase(request.branch().trim())) {
            return new RegistrationResponse(null, "ERROR", "Provided department/branch does not match our records.");
        }

        if (registrationRepo.existsByStudentId(student.getId())) {
            return new RegistrationResponse(null, "ERROR", "Student is already registered.");
        }

        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setStatus("REGISTERED");

        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                registration.setUniqueCode(codeGenerator.generateCode());
                Registration saved = registrationRepo.saveAndFlush(registration);
                return new RegistrationResponse(saved.getUniqueCode(), "SUCCESS", "Registration successful!");
            } catch (DataIntegrityViolationException e) {
                if (i == maxRetries - 1) {
                    throw new RuntimeException("Server busy, please try again.");
                }
            }
        }
        return new RegistrationResponse(null, "ERROR", "Failed to generate code.");
    }
}