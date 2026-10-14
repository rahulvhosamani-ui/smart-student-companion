package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@CrossOrigin("*")
public class AssignmentController {

    private final AssignmentRepository assignmentRepository;

    public AssignmentController(
            AssignmentRepository assignmentRepository) {

        this.assignmentRepository =
                assignmentRepository;
    }

    @GetMapping
    public List<Assignment> getAssignments() {

        return assignmentRepository.findAll();
    }

    @PostMapping
    public Assignment saveAssignment(
            @RequestBody Assignment assignment) {

        return assignmentRepository.save(assignment);
    }

    @DeleteMapping("/{id}")
    public void deleteAssignment(
            @PathVariable Long id) {

        assignmentRepository.deleteById(id);
    }
}