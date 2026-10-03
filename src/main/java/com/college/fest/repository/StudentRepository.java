package com.college.fest.repository;

import com.college.fest.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRollNumber(String rollNumber);
    // Add this query to quickly find which uploaded roll numbers already exist
    @Query("SELECT s.rollNumber FROM Student s WHERE s.rollNumber IN :rollNumbers")
    Set<String> findExistingRollNumbers(@Param("rollNumbers") List<String> rollNumbers);

    // Grabs only the unique department names from the database
    @Query("SELECT DISTINCT s.branch FROM Student s WHERE s.branch IS NOT NULL")
    List<String> findDistinctDepartments();

}