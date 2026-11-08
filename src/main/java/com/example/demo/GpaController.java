package com.example.demo;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/gpa")
public class GpaController {

    private final GpaRepository gpaRepository;
    private final CurrentStudent currentStudent;

    public GpaController(GpaRepository gpaRepository, CurrentStudent currentStudent) {
        this.gpaRepository = gpaRepository;
        this.currentStudent = currentStudent;
    }

    // Get all GPA records
    @GetMapping
    public ResponseEntity<List<Gpa>> getGpa(Authentication auth) {
        return ResponseEntity.ok(gpaRepository.findAllByStudent_IdOrderBySemesterAsc(currentStudent.require(auth).getId()));
    }

    // Get one GPA record by ID
    @GetMapping("/{id}")
    public ResponseEntity<Gpa> getGpaById(
            @PathVariable Long id, Authentication auth) {

        return gpaRepository.findByIdAndStudent_Id(id, currentStudent.require(auth).getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Save a new GPA record
    @PostMapping
    public ResponseEntity<?> saveGpa(
            @RequestBody Gpa gpa, Authentication auth) {

        String error = validateGpa(gpa);

        if (error != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", error));
        }

        // Ensure a new record is created.
        gpa.setId(null);
        Student student = currentStudent.require(auth);
        if (gpaRepository.existsByStudent_IdAndSemester(student.getId(), gpa.getSemester())) {
            return ResponseEntity.badRequest().body(Map.of("error", "This semester already has a GPA record."));
        }
        gpa.setStudent(student);

        Gpa saved = gpaRepository.save(gpa);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saved);
    }

    // Update an existing GPA record
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGpa(
            @PathVariable Long id,
            @RequestBody Gpa updatedGpa, Authentication auth) {

        String error = validateGpa(updatedGpa);

        if (error != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", error));
        }

        Long studentId = currentStudent.require(auth).getId();
        if (gpaRepository.existsByStudent_IdAndSemesterAndIdNot(
                studentId, updatedGpa.getSemester(), id)) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "This semester already has a GPA record."));
        }

        return gpaRepository.findByIdAndStudent_Id(id, studentId)
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
            @PathVariable Long id, Authentication auth) {

        Long studentId = currentStudent.require(auth).getId();
        if (!gpaRepository.existsByIdAndStudent_Id(id, studentId)) {
            return ResponseEntity.notFound().build();
        }

        gpaRepository.deleteByIdAndStudent_Id(id, studentId);

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
