package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentRepository assignmentRepository;

    public AssignmentController(
            AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    @GetMapping
    public List<Assignment> getAssignments() {
        return assignmentRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> saveAssignment(
            @RequestBody Assignment assignment) {

        if (assignment.getTitle() == null ||
                assignment.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Assignment title is required.");
        }

        assignment.setId(null);
        assignment.setTitle(assignment.getTitle().trim());

        if (assignment.getSubject() == null ||
                assignment.getSubject().trim().isEmpty()) {
            assignment.setSubject("General");
        }

        if (assignment.getPriority() == null ||
                assignment.getPriority().trim().isEmpty()) {
            assignment.setPriority("Medium");
        }

        if (assignment.getStatus() == null ||
                assignment.getStatus().trim().isEmpty()) {
            assignment.setStatus("Pending");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assignmentRepository.save(assignment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAssignment(
            @PathVariable Long id,
            @RequestBody Assignment updated) {

        return assignmentRepository.findById(id)
                .map(existing -> {
                    if (updated.getTitle() == null ||
                            updated.getTitle().trim().isEmpty()) {
                        return ResponseEntity.badRequest()
                                .body((Object) "Assignment title is required.");
                    }

                    existing.setTitle(updated.getTitle().trim());
                    existing.setSubject(
                            updated.getSubject() == null ||
                            updated.getSubject().trim().isEmpty()
                                    ? "General"
                                    : updated.getSubject().trim()
                    );
                    existing.setDueDate(updated.getDueDate());
                    existing.setPriority(
                            updated.getPriority() == null ||
                            updated.getPriority().trim().isEmpty()
                                    ? "Medium"
                                    : updated.getPriority().trim()
                    );

                    boolean isDone = updated.isCompleted()
                            || "Completed".equalsIgnoreCase(
                                    updated.getStatus());

                    existing.setCompleted(isDone);
                    existing.setStatus(isDone ? "Completed" : "Pending");

                    return ResponseEntity.ok(
                            assignmentRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long id) {

        if (!assignmentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        assignmentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}