package com.college.fest.service;

import com.college.fest.dto.CheckInResponse;
import com.college.fest.entity.Registration;
import com.college.fest.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
public class CheckInService {

    private final RegistrationRepository registrationRepo;

    public CheckInService(RegistrationRepository registrationRepo) {
        this.registrationRepo = registrationRepo;
    }

    // Just views the status without checking them in (useful for info lookups)
    public CheckInResponse verifyCode(String uniqueCode) {
        Optional<Registration> regOpt = registrationRepo.findByUniqueCode(uniqueCode);

        if (regOpt.isEmpty()) {
            return new CheckInResponse("INVALID CODE", null, null);
        }

        Registration reg = regOpt.get();
        String currentStatus = reg.getStatus().equals("CHECKED_IN") ? "ALREADY CHECKED IN" : "VALID AND UNUSED";

        return new CheckInResponse(currentStatus, reg.getStudent().getFullName(), reg.getStudent().getRollNumber());
    }

    // Actually performs the check-in mutation with correct local time
    @Transactional
    public CheckInResponse processCheckIn(String uniqueCode) {
        Optional<Registration> regOpt = registrationRepo.findByUniqueCode(uniqueCode);

        if (regOpt.isEmpty()) {
            return new CheckInResponse("INVALID CODE", null, null);
        }

        Registration reg = regOpt.get();

        // Check if already checked in
        if (reg.getStatus().equals("CHECKED_IN")) {
            return new CheckInResponse("ALREADY CHECKED IN", reg.getStudent().getFullName(), reg.getStudent().getRollNumber());
        }

        // Update status and set explicit IST time
        reg.setStatus("CHECKED_IN");

        LocalDateTime localNow = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime();
        reg.setCheckedInAt(localNow);

        registrationRepo.save(reg);

        return new CheckInResponse("ENTRY ALLOWED", reg.getStudent().getFullName(), reg.getStudent().getRollNumber());
    }
}