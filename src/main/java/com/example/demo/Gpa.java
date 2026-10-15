package com.example.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "gpa")
public class Gpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer semester;

    private Double sgpa;

    private Double cgpa;

    public Gpa() {
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

        if (sgpa != null && (sgpa < 0 || sgpa > 10)) {
            throw new IllegalArgumentException(
                    "SGPA must be between 0 and 10");
        }

        this.sgpa = sgpa;
    }

    public Double getCgpa() {
        return cgpa;
    }

    public void setCgpa(Double cgpa) {

        if (cgpa != null && (cgpa < 0 || cgpa > 10)) {
            throw new IllegalArgumentException(
                    "CGPA must be between 0 and 10");
        }

        this.cgpa = cgpa;
    }
}