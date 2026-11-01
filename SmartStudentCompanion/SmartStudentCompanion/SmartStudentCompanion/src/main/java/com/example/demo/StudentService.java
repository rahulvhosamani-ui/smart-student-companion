package com.example.demo;

import java.util.Locale;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder) {

        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new student.
     * The password is encoded before it is stored.
     */
    public Student registerStudent(Student student) {

        if (student == null) {
            throw new IllegalArgumentException(
                    "Student details are required.");
        }

        String name = normalizeName(student.getName());
        String email = normalizeEmail(student.getEmail());
        String password = student.getPassword();

        validateName(name);
        validateEmail(email);
        validatePassword(password);

        if (studentRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException(
                    "This email is already registered.");
        }

        student.setName(name);
        student.setEmail(email);
        student.setPassword(passwordEncoder.encode(password));

        try {
            return studentRepository.saveAndFlush(student);
        } catch (DataIntegrityViolationException ex) {
            // Also handles duplicate registrations submitted concurrently.
            throw new IllegalArgumentException(
                    "This email is already registered.", ex);
        }
    }

    /**
     * Find a student by email.
     * Returns null if no student exists, preserving your original API.
     */
    @Transactional(readOnly = true)
    public Student findByEmail(String email) {

        if (!StringUtils.hasText(email)) {
            return null;
        }

        return studentRepository
                .findByEmail(normalizeEmail(email))
                .orElse(null);
    }

    /**
     * Find a student by database ID.
     * Returns null if no student exists.
     */
    @Transactional(readOnly = true)
    public Student findById(Long id) {

        if (id == null || id <= 0) {
            return null;
        }

        return studentRepository.findById(id).orElse(null);
    }

    /**
     * Update student details.
     * A blank password means the existing password remains unchanged.
     */
    public Student updateStudent(
            Long id,
            Student updatedStudent) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "A valid student ID is required.");
        }

        if (updatedStudent == null) {
            throw new IllegalArgumentException(
                    "Updated student details are required.");
        }

        Student existingStudent = studentRepository.findById(id)
                .orElse(null);

        if (existingStudent == null) {
            return null;
        }

        String name = normalizeName(updatedStudent.getName());
        String email = normalizeEmail(updatedStudent.getEmail());

        validateName(name);
        validateEmail(email);

        Optional<Student> studentWithEmail =
                studentRepository.findByEmail(email);

        if (studentWithEmail.isPresent()
                && !studentWithEmail.get().getId().equals(id)) {

            throw new IllegalArgumentException(
                    "This email is already in use.");
        }

        existingStudent.setName(name);
        existingStudent.setEmail(email);

        String newPassword = updatedStudent.getPassword();

        if (StringUtils.hasText(newPassword)) {
            validatePassword(newPassword);

            // Avoid encoding a password that is already stored as a hash
            // by mistake; callers should provide a new plaintext password.
            existingStudent.setPassword(
                    passwordEncoder.encode(newPassword));
        }

        try {
            return studentRepository.saveAndFlush(existingStudent);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException(
                    "The email is already in use or the student data is invalid.",
                    ex);
        }
    }

    /**
     * Delete a student by ID.
     */
    public void deleteStudent(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "A valid student ID is required.");
        }

        if (!studentRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Student not found.");
        }

        studentRepository.deleteById(id);
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }

    private String normalizeEmail(String email) {
        return email == null
                ? null
                : email.trim().toLowerCase(Locale.ROOT);
    }

    private void validateName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException(
                    "Student name is required.");
        }

        if (name.length() > 100) {
            throw new IllegalArgumentException(
                    "Student name must not exceed 100 characters.");
        }
    }

    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException(
                    "Email address is required.");
        }

        if (email.length() > 254
                || !email.matches(
                        "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@"
                        + "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}"
                        + "[A-Za-z0-9])?"
                        + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}"
                        + "[A-Za-z0-9])?)+$")) {

            throw new IllegalArgumentException(
                    "Please enter a valid email address.");
        }
    }

    private void validatePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException(
                    "Password is required.");
        }

        // BCrypt has a 72-byte input limit. This check supports ordinary
        // ASCII passwords and prevents excessively long input.
        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters.");
        }

        if (password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "Password must not exceed 72 UTF-8 bytes.");
        }
    }
}