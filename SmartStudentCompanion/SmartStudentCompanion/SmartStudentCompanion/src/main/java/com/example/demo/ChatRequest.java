package com.example.demo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatRequest {

    @NotBlank(message = "Question cannot be empty")
    @Size(
        max = 10000,
        message = "Question cannot exceed 10000 characters"
    )
    private String question;

    public ChatRequest() {
    }

    public ChatRequest(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}