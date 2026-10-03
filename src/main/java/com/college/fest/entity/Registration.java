package com.college.fest.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "registration")
public class Registration {

    @Id
    private String id;

    @OneToOne
    @JoinColumn(name = "student_id", referencedColumnName = "id", nullable = false, unique = true)
    private Student student;

    @Column(name = "unique_code", nullable = false, unique = true, length = 20)
    private String uniqueCode;

    @Column(nullable = false, length = 20)
    private String status = "REGISTERED"; // 'REGISTERED' or 'CHECKED_IN'

    @Column(name = "registered_at", updatable = false)
    private LocalDateTime registeredAt;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Version
    private Integer version; // Automatically prevents double check-ins

    // This runs automatically right before the database saves a new record
    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.registeredAt == null) {
            this.registeredAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public String getUniqueCode() { return uniqueCode; }
    public void setUniqueCode(String uniqueCode) { this.uniqueCode = uniqueCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }

    public LocalDateTime getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(LocalDateTime checkedInAt) { this.checkedInAt = checkedInAt; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}