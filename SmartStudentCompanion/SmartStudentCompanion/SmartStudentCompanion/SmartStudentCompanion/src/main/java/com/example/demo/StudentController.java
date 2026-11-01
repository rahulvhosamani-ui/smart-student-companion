package com.example.demo;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /**
     * Returns the profile of the currently authenticated student.
     * Endpoint: GET /students/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(
            Authentication authentication) {

        // Verify that a real user is authenticated.
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in to view your profile."
            );
        }

        // Use the authenticated email, never an email supplied by the client.
        String email = authentication.getName();

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated student email is unavailable."
            );
        }

        Student student = studentRepository
                .findByEmail(email.trim().toLowerCase(java.util.Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student profile not found."
                ));

        // Return only safe profile fields; never expose the password.
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", student.getId());
        profile.put("name", student.getName());
        profile.put("email", student.getEmail());

        return ResponseEntity.ok(profile);
    }
}