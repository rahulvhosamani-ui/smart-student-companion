package com.example.demo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentRepository assignmentRepository;
    private final CurrentStudent currentStudent;

    public AssignmentController(
            AssignmentRepository assignmentRepository, CurrentStudent currentStudent) {
        this.assignmentRepository = assignmentRepository;
        this.currentStudent = currentStudent;
    }

    // Get all assignments
    @GetMapping
    public ResponseEntity<List<Assignment>> getAssignments(Authentication auth) {
        List<Assignment> assignments =
                assignmentRepository.findAllByStudent_IdOrderByDueDateAsc(currentStudent.require(auth).getId());

        return ResponseEntity.ok(assignments);
    }

    // Create a new assignment
    @PostMapping
    public ResponseEntity<?> saveAssignment(
            @Valid @RequestBody Assignment assignment, Authentication auth) {

        if (assignment.getTitle() == null
                || assignment.getTitle().isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Assignment title is required.");
        }

        if (assignment.getTitle().trim().length() > 150) {
            return ResponseEntity.badRequest()
                    .body("Title cannot exceed 150 characters.");
        }

        if (assignment.getSubject() != null
                && assignment.getSubject().trim().length() > 100) {
            return ResponseEntity.badRequest()
                    .body("Subject cannot exceed 100 characters.");
        }

        if (!isValidPriority(assignment.getPriority())) {
            return ResponseEntity.badRequest()
                    .body("Priority must be Low, Medium, or High.");
        }

        assignment.setId(null);
        assignment.setStudent(currentStudent.require(auth));
        assignment.setTitle(assignment.getTitle().trim());
        assignment.setSubject(normalizeSubject(assignment.getSubject()));
        assignment.setPriority(normalizePriority(assignment.getPriority()));

        boolean done = assignment.isCompleted()
                || "Completed".equalsIgnoreCase(assignment.getStatus());

        assignment.setCompleted(done);
        assignment.setStatus(done ? "Completed" : "Pending");

        Assignment saved =
                assignmentRepository.save(assignment);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saved);
    }

    // Update an existing assignment
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody Assignment updated, Authentication auth) {

        if (updated.getTitle() == null
                || updated.getTitle().isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Assignment title is required.");
        }

        if (updated.getTitle().trim().length() > 150) {
            return ResponseEntity.badRequest()
                    .body("Title cannot exceed 150 characters.");
        }

        if (updated.getSubject() != null
                && updated.getSubject().trim().length() > 100) {
            return ResponseEntity.badRequest()
                    .body("Subject cannot exceed 100 characters.");
        }

        if (!isValidPriority(updated.getPriority())) {
            return ResponseEntity.badRequest()
                    .body("Priority must be Low, Medium, or High.");
        }

        return assignmentRepository.findByIdAndStudent_Id(id, currentStudent.require(auth).getId())
                .map(existing -> {

                    existing.setTitle(updated.getTitle().trim());
                    existing.setSubject(
                            normalizeSubject(updated.getSubject()));
                    existing.setDueDate(updated.getDueDate());
                    existing.setPriority(
                            normalizePriority(updated.getPriority()));

                    boolean done = updated.isCompleted()
                            || "Completed".equalsIgnoreCase(
                                    updated.getStatus());

                    existing.setCompleted(done);
                    existing.setStatus(
                            done ? "Completed" : "Pending");

                    Assignment saved =
                            assignmentRepository.save(existing);

                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }

    // Mark an assignment as completed or pending
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Assignment updated, Authentication auth) {

        return assignmentRepository.findByIdAndStudent_Id(id, currentStudent.require(auth).getId())
                .map(existing -> {

                    boolean done = updated.isCompleted()
                            || "Completed".equalsIgnoreCase(
                                    updated.getStatus());

                    existing.setCompleted(done);
                    existing.setStatus(
                            done ? "Completed" : "Pending");

                    Assignment saved =
                            assignmentRepository.save(existing);

                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }

    // Delete an assignment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long id, Authentication auth) {

        Long studentId = currentStudent.require(auth).getId();
        if (!assignmentRepository.existsByIdAndStudent_Id(id, studentId)) {
            return ResponseEntity.notFound().build();
        }

        assignmentRepository.deleteByIdAndStudent_Id(id, studentId);

        return ResponseEntity.noContent().build();
    }

    // Helper methods
    private String normalizeSubject(String subject) {
        if (subject == null || subject.isBlank()) {
            return "General";
        }

        return subject.trim();
    }

    private String normalizePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return "Medium";
        }

        return priority.trim();
    }

    private boolean isValidPriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return true;
        }

        return "Low".equalsIgnoreCase(priority.trim())
                || "Medium".equalsIgnoreCase(priority.trim())
                || "High".equalsIgnoreCase(priority.trim());
    }
}
