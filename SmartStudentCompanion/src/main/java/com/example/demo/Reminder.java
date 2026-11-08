package com.example.demo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "reminder",
    indexes = {
        @Index(name = "idx_reminder_student_due", columnList = "student_id, due_date"),
        @Index(
            name = "idx_reminder_due_date",
            columnList = "due_date"
        )
    }
)
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Reminder title is required")
    @Size(
        max = 150,
        message = "Reminder title cannot exceed 150 characters"
    )
    @Column(nullable = false, length = 150)
    private String title;

    @JsonAlias("reminderDate")
    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "student_id", nullable = true)
    private Student student;

    @JsonIgnore
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Reminder() {
    }

    public Reminder(String title, LocalDate dueDate) {
        this.title = title;
        this.dueDate = dueDate;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        normalizeTitle();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        normalizeTitle();
    }

    private void normalizeTitle() {
        if (title != null) {
            title = title.trim();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
