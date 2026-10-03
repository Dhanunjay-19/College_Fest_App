package com.college.fest;

import com.college.fest.entity.AppUser;
import com.college.fest.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        // 1. Create Default Admin
        if (userRepository.findByUsername("admin").isEmpty()) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123")); // Securely hashes the password
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println("✅ Default Admin user created! (Username: admin | Password: admin123)");
        }

        // 2. Create Default Volunteer
        if (userRepository.findByUsername("volunteer1").isEmpty()) {
            AppUser volunteer = new AppUser();
            volunteer.setUsername("volunteer1");
            volunteer.setPasswordHash(passwordEncoder.encode("vol123")); // Securely hashes the password
            volunteer.setRole("VOLUNTEER");
            userRepository.save(volunteer);
            System.out.println("✅ Default Volunteer user created! (Username: volunteer1 | Password: vol123)");
        }
    }
}