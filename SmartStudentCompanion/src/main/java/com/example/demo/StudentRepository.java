package com.example.demo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Find a student by their email address.
     * Used by Spring Security during login and for loading profiles.
     */
    Optional<Student> findByEmail(String email);

    /**
     * Check whether an email address is already registered.
     * Used to prevent duplicate student registrations.
     */
    boolean existsByEmail(String email);
}