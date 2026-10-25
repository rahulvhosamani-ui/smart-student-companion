package com.example.demo;

import jakarta.persistence.*;

@Entity
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String dueDate;

    public Reminder() {
    }

    public Reminder(String title, String dueDate) {
        this.title = title;
        this.dueDate = dueDate;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
}