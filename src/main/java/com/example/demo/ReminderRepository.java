package com.example.demo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReminderRepository
        extends JpaRepository<Reminder, Long> {

    List<Reminder> findAllByStudent_IdOrderByDueDateAsc(Long studentId);
    java.util.Optional<Reminder> findByIdAndStudent_Id(Long id, Long studentId);
    boolean existsByIdAndStudent_Id(Long id, Long studentId);
    void deleteByIdAndStudent_Id(Long id, Long studentId);

    // Find reminders by title
    List<Reminder> findByTitleContainingIgnoreCase(String title);

    // Find reminders due on a specific date
    List<Reminder> findByDueDate(LocalDate dueDate);

    // Find reminders due before a specific date
    List<Reminder> findByDueDateBefore(LocalDate date);

    // Find reminders due after a specific date
    List<Reminder> findByDueDateAfter(LocalDate date);

    // Retrieve reminders ordered by due date
    List<Reminder> findAllByOrderByDueDateAsc();

    // Retrieve reminders due today or later
    List<Reminder> findByDueDateGreaterThanEqualOrderByDueDateAsc(
            LocalDate date);
}
