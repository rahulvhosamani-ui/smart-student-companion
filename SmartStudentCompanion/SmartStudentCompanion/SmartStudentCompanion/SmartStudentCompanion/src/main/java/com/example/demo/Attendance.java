package com.example.demo;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(
    name = "attendance",
    indexes = {
        @Index(
            name = "idx_attendance_subject",
            columnList = "subject"
        ),
        @Index(
            name = "idx_attendance_month",
            columnList = "month"
        )
    }
)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Subject is required")
    @Size(max = 100, message = "Subject cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String subject;

    @NotNull(message = "Attendance percentage is required")
    @DecimalMin(value = "0.0", message = "Percentage cannot be below 0")
    @DecimalMax(value = "100.0", message = "Percentage cannot exceed 100")
    @Column(nullable = false)
    private Double percentage = 0.0;

    @NotBlank(message = "Month is required")
    @Size(max = 20, message = "Month cannot exceed 20 characters")
    @Column(nullable = false, length = 20)
    private String month;

    public Attendance() {
    }

    public Attendance(String subject, Double percentage, String month) {
        this.subject = subject;
        this.percentage = percentage;
        this.month = month;
    }

    @PrePersist
    @PreUpdate
    public void validateAttendance() {
        if (subject != null) {
            subject = subject.trim();
        }

        if (month != null) {
            month = month.trim();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }
}