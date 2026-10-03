package com.college.fest.repository;

import com.college.fest.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, String> {

    // Checks if a student has already registered to prevent duplicates
    boolean existsByStudentId(Long studentId);

    // Used by volunteers to look up a QR code
    Optional<Registration> findByUniqueCode(String uniqueCode);

    // Atomic update: only updates if the current status is still 'REGISTERED'
    @Modifying
    @Query("UPDATE Registration r SET r.status = 'CHECKED_IN', r.checkedInAt = CURRENT_TIMESTAMP " +
            "WHERE r.uniqueCode = :code AND r.status = 'REGISTERED'")
    int atomicCheckIn(@Param("code") String code);
}