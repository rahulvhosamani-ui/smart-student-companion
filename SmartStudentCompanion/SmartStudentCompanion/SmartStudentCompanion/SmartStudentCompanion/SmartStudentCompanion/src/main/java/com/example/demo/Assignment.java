package com.example.demo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
@Table(
    name = "assignment",
    indexes = {
        @Index(name = "idx_assignment_due_date", columnList = "due_date"),
        @Index(name = "idx_assignment_status", columnList = "status")
    }
)
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Assignment title is required")
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    @Column(nullable = false, length = 150)
    private String title;

    @Size(max = 100, message = "Subject cannot exceed 100 characters")
    @Column(length = 100)
    private String subject = "General";

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(length = 20)
    private String priority = "Medium";

    @Column(length = 20, nullable = false)
    private String status = "Pending";

    @Column(nullable = false)
    private boolean completed = false;

    public Assignment() {
    }

    public Assignment(String title) {
        this.title = title;
    }

    public Assignment(
            String title,
            String subject,
            LocalDate dueDate,
            String priority) {
        this.title = title;
        this.subject = subject;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = "Pending";
        this.completed = false;
    }

    @PrePersist
    @PreUpdate
    public void synchronizeStatus() {
        if (completed || "Completed".equalsIgnoreCase(status)) {
            completed = true;
            status = "Completed";
        } else {
            completed = false;
            status = "Pending";
        }

        if (subject == null || subject.isBlank()) {
            subject = "General";
        }

        if (priority == null || priority.isBlank()) {
            priority = "Medium";
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}