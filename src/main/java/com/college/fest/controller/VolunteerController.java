package com.college.fest.controller;

import com.college.fest.dto.CheckInResponse;
import com.college.fest.service.CheckInService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/volunteer")
@CrossOrigin(origins = "http://localhost:4200")
public class VolunteerController {

    private final CheckInService checkInService;

    public VolunteerController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    // Used when the volunteer scans the QR to see details BEFORE allowing entry
    @GetMapping("/verify/{code}")
    public ResponseEntity<CheckInResponse> verifyCode(@PathVariable String code) {
        return ResponseEntity.ok(checkInService.verifyCode(code));
    }

    // Used when the volunteer clicks "Confirm Check-In"
    @PostMapping("/checkin/{code}")
    public ResponseEntity<CheckInResponse> processCheckIn(@PathVariable String code) {
        CheckInResponse response = checkInService.processCheckIn(code);

        if ("ENTRY ALLOWED".equals(response.status())) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().body(response);
    }
}