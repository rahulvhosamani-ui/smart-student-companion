package com.example.demo;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private ChatMessageRepository repository;

    @PostMapping("/chat")
    public ChatResponse chat(
            @RequestBody ChatRequest request,
            Principal principal) {

        String answer =
                geminiService.askGemini(
                        request.getQuestion());

        ChatMessage chat = new ChatMessage();

        chat.setStudentEmail(principal.getName());
        chat.setQuestion(request.getQuestion());
        chat.setAnswer(answer);

        repository.save(chat);

        return new ChatResponse(answer);
    }

    @GetMapping("/history")
    public java.util.List<ChatMessage> history(
            Principal principal) {

        return repository
                .findByStudentEmailOrderByCreatedAtAsc(
                        principal.getName());
    }
}