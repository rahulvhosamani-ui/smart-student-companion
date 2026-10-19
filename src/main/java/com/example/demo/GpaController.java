package com.example.demo;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gpa")
public class GpaController {

    private final GpaRepository gpaRepository;

    public GpaController(GpaRepository gpaRepository) {
        this.gpaRepository = gpaRepository;
    }

    // Get all GPA records
    @GetMapping
    public ResponseEntity<List<Gpa>> getGpa() {
        return ResponseEntity.ok(gpaRepository.findAll());
    }

    // Get one GPA record by ID
    @GetMapping("/{id}")
    public ResponseEntity<Gpa> getGpaById(
            @PathVariable Long id) {

        return gpaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Save a new GPA record
    @PostMapping
    public ResponseEntity<?> saveGpa(
            @RequestBody Gpa gpa) {

        String error = validateGpa(gpa);

        if (error != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", error));
        }

        // Ensure a new record is created.
        gpa.setId(null);

        Gpa saved = gpaRepository.save(gpa);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saved);
    }

    // Update an existing GPA record
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGpa(
            @PathVariable Long id,
            @RequestBody Gpa updatedGpa) {

        String error = validateGpa(updatedGpa);

        if (error != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", error));
        }

        return gpaRepository.findById(id)
                .map(existing -> {
                    existing.setSemester(updatedGpa.getSemester());
                    existing.setSgpa(updatedGpa.getSgpa());
                    existing.setCgpa(updatedGpa.getCgpa());

                    Gpa saved = gpaRepository.save(existing);

                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete a GPA record
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGpa(
            @PathVariable Long id) {

        if (!gpaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        gpaRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    // Validate GPA input
    private String validateGpa(Gpa gpa) {

        if (gpa == null) {
            return "GPA data is required.";
        }

        if (gpa.getSemester() == null
                || gpa.getSemester() < 1
                || gpa.getSemester() > 12) {
            return "Semester must be between 1 and 12.";
        }

        if (!isValidGpa(gpa.getSgpa())) {
            return "SGPA is required and must be between 0 and 10.";
        }

        if (gpa.getCgpa() != null
                && !isValidGpa(gpa.getCgpa())) {
            return "CGPA must be between 0 and 10.";
        }

        return null;
    }

    private boolean isValidGpa(Double value) {
        return value != null
                && Double.isFinite(value)
                && value >= 0.0
                && value <= 10.0;
    }
}