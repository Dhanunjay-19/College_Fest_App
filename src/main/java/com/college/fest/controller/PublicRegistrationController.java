package com.college.fest.controller;

import com.college.fest.dto.RegistrationRequest;
import com.college.fest.dto.RegistrationResponse;
import com.college.fest.repository.StudentRepository;
import com.college.fest.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "http://localhost:4200")
public class PublicRegistrationController {

    private final RegistrationService registrationService;
    @Autowired
    private StudentRepository studentRepository;

    public PublicRegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@RequestBody RegistrationRequest request) {
        RegistrationResponse response = registrationService.registerStudent(request);

        if ("SUCCESS".equals(response.status())) {
            return ResponseEntity.ok(response);
        }

        // Return 400 Bad Request if validation or eligibility fails
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/departments")
    public ResponseEntity<List<String>> getDepartments() {
        List<String> departments = studentRepository.findDistinctDepartments();
        return ResponseEntity.ok(departments);
    }
}