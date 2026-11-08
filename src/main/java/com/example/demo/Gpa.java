package com.example.demo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
    name = "gpa",
    indexes = {
        @Index(
            name = "idx_gpa_student_semester",
            columnList = "student_id, semester"
        )
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_gpa_student_semester",
            columnNames = {"student_id", "semester"}
        )
    }
)
public class Gpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Semester must be at least 1")
    @Max(value = 12, message = "Semester cannot exceed 12")
    @Column(nullable = false)
    private Integer semester;

    @NotNull(message = "SGPA is required")
    @DecimalMin(value = "0.0", message = "SGPA cannot be below 0")
    @DecimalMax(value = "10.0", message = "SGPA cannot exceed 10")
    @Column(nullable = false)
    private Double sgpa;

    @DecimalMin(value = "0.0", message = "CGPA cannot be below 0")
    @DecimalMax(value = "10.0", message = "CGPA cannot exceed 10")
    @Column
    private Double cgpa;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "student_id", nullable = true)
    private Student student;

    @JsonIgnore
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Gpa() {
    }

    public Gpa(Integer semester, Double sgpa, Double cgpa) {
        this.semester = semester;
        setSgpa(sgpa);
        setCgpa(cgpa);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public Double getSgpa() {
        return sgpa;
    }

    public void setSgpa(Double sgpa) {
        validateGpa(sgpa, "SGPA");
        this.sgpa = sgpa;
    }

    public Double getCgpa() {
        return cgpa;
    }

    public void setCgpa(Double cgpa) {
        validateGpa(cgpa, "CGPA");
        this.cgpa = cgpa;
    }

    private void validateGpa(Double value, String fieldName) {
        if (value != null
                && (!Double.isFinite(value)
                || value < 0.0
                || value > 10.0)) {
            throw new IllegalArgumentException(
                    fieldName + " must be between 0 and 10");
        }
    }
}
