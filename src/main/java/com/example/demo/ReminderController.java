package com.example.demo;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderRepository reminderRepository;
    private final CurrentStudent currentStudent;

    public ReminderController(
            ReminderRepository reminderRepository, CurrentStudent currentStudent) {
        this.reminderRepository = reminderRepository;
        this.currentStudent = currentStudent;
    }

    // Get all reminders
    @GetMapping
    public ResponseEntity<List<Reminder>> getAllReminders(Authentication auth) {
        return ResponseEntity.ok(reminderRepository.findAllByStudent_IdOrderByDueDateAsc(currentStudent.require(auth).getId()));
    }

    // Get one reminder by ID
    @GetMapping("/{id}")
    public ResponseEntity<Reminder> getReminderById(
            @PathVariable Long id, Authentication auth) {

        return reminderRepository.findByIdAndStudent_Id(id, currentStudent.require(auth).getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create a new reminder
    @PostMapping
    public ResponseEntity<?> saveReminder(
            @Valid @RequestBody Reminder reminder, Authentication auth) {

        String error = validateReminder(reminder);

        if (error != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", error));
        }

        reminder.setId(null);
        reminder.setStudent(currentStudent.require(auth));
        reminder.setTitle(reminder.getTitle().trim());
        if (reminder.getDueDate() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Due date is required."));
        }

        Reminder saved = reminderRepository.saveAndFlush(reminder);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saved);
    }

    // Update an existing reminder
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReminder(
            @PathVariable Long id,
            @Valid @RequestBody Reminder updatedReminder, Authentication auth) {

        String error = validateReminder(updatedReminder);

        if (error != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", error));
        }

        return reminderRepository.findByIdAndStudent_Id(id, currentStudent.require(auth).getId())
                .map(existing -> {
                    existing.setTitle(
                            updatedReminder.getTitle().trim());
                    existing.setDueDate(
                            updatedReminder.getDueDate());
                    existing.setCompleted(
                            updatedReminder.isCompleted());

                    Reminder saved =
                            reminderRepository.saveAndFlush(existing);

                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete a reminder
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReminder(
            @PathVariable Long id, Authentication auth) {

        Long studentId = currentStudent.require(auth).getId();
        if (!reminderRepository.existsByIdAndStudent_Id(id, studentId)) {
            return ResponseEntity.notFound().build();
        }

        reminderRepository.deleteByIdAndStudent_Id(id, studentId);

        return ResponseEntity.noContent().build();
    }

    // Validate incoming reminder data
    private String validateReminder(Reminder reminder) {

        if (reminder == null) {
            return "Reminder data is required.";
        }

        if (reminder.getTitle() == null
                || reminder.getTitle().isBlank()) {
            return "Reminder title is required.";
        }

        if (reminder.getTitle().trim().length() > 150) {
            return "Reminder title cannot exceed 150 characters.";
        }

        return null;
    }
}
