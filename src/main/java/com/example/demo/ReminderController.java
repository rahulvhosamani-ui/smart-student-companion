package com.example.demo;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@CrossOrigin("*")
public class ReminderController {

    private final ReminderRepository reminderRepository;

    public ReminderController(
            ReminderRepository reminderRepository) {

        this.reminderRepository = reminderRepository;
    }

    @GetMapping
    public List<Reminder> getAllReminders() {
        return reminderRepository.findAll();
    }

    @PostMapping
    public Reminder saveReminder(
            @RequestBody Reminder reminder) {

        return reminderRepository.save(reminder);
    }

    @DeleteMapping("/{id}")
    public void deleteReminder(
            @PathVariable Long id) {

        reminderRepository.deleteById(id);
    }
}