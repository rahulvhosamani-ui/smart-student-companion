package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderRepository
        extends JpaRepository<Reminder, Long> {
}