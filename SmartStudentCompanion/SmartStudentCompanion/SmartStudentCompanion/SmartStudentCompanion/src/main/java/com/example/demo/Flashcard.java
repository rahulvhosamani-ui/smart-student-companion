package com.example.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "flashcards")
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Question is required.")
    @Size(max = 1000, message = "Question cannot exceed 1000 characters.")
    @Column(nullable = false, length = 1000)
    private String question;

    @NotBlank(message = "Answer is required.")
    @Size(max = 2000, message = "Answer cannot exceed 2000 characters.")
    @Column(nullable = false, length = 2000)
    private String answer;

    @Column(nullable = false)
    private boolean known;

    @PrePersist
    @PreUpdate
    void normalize() {
        if (question != null) question = question.trim();
        if (answer != null) answer = answer.trim();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public boolean isKnown() { return known; }
    public void setKnown(boolean known) { this.known = known; }
}
