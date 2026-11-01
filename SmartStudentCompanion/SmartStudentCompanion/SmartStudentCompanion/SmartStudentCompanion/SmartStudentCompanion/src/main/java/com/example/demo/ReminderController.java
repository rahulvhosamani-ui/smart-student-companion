package com.example.demo;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderRepository reminderRepository;

    public ReminderController(
            ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    // Get all reminders
    @GetMapping
    public ResponseEntity<List<Reminder>> getAllReminders() {
        return ResponseEntity.ok(reminderRepository.findAll());
    }

    // Get one reminder by ID
    @GetMapping("/{id}")
    public ResponseEntity<Reminder> getReminderById(
            @PathVariable Long id) {

        return reminderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create a new reminder
    @PostMapping
    public ResponseEntity<?> saveReminder(
            @Valid @RequestBody Reminder reminder) {

        String error = validateReminder(reminder);

        if (error != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", error));
        }

        reminder.setId(null);
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
            @Valid @RequestBody Reminder updatedReminder) {

        String error = validateReminder(updatedReminder);

        if (error != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", error));
        }

        return reminderRepository.findById(id)
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
            @PathVariable Long id) {

        if (!reminderRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        reminderRepository.deleteById(id);

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
