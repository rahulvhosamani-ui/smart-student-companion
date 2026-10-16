package com.example.demo;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder) {

        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Register a new student
    public Student registerStudent(Student student) {

        String email = student.getEmail().trim();

        if (studentRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException(
                    "This email is already registered.");
        }

        student.setEmail(email);

        // Encrypt password before saving
        student.setPassword(
                passwordEncoder.encode(student.getPassword()));

        return studentRepository.save(student);
    }

    // Find student by email
    public Student findByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElse(null);
    }

    // Find student by ID
    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElse(null);
    }

    // Update student details
    public Student updateStudent(
            Long id, Student updatedStudent) {

        return studentRepository.findById(id)
                .map(student -> {

                    student.setName(updatedStudent.getName());

                    String newEmail = updatedStudent.getEmail().trim();

                    if (!newEmail.equalsIgnoreCase(student.getEmail())
                            && studentRepository.findByEmail(newEmail)
                                    .isPresent()) {
                        throw new IllegalArgumentException(
                                "This email is already in use.");
                    }

                    student.setEmail(newEmail);

                    if (updatedStudent.getPassword() != null
                            && !updatedStudent.getPassword().isBlank()) {

                        student.setPassword(
                                passwordEncoder.encode(
                                        updatedStudent.getPassword()));
                    }

                    return studentRepository.save(student);
                })
                .orElse(null);
    }

    // Delete student
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
}