package com.example.demo;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final GeminiService geminiService;
    private final ChatMessageRepository repository;

    public AIController(
            GeminiService geminiService,
            ChatMessageRepository repository) {

        this.geminiService = geminiService;
        this.repository = repository;
    }

    // Response returned to the chatbot frontend.
    public record ChatResponse(String answer) {
    }

    // POST /api/ai/chat
    // Generate an answer and save the conversation.
    @PostMapping("/chat")
    public ResponseEntity<?> chat(
            @RequestBody(required = false) ChatRequest request,
            Principal principal) {

        // Require an authenticated user.
        if (principal == null
                || principal.getName() == null
                || principal.getName().isBlank()
                || "anonymousUser".equals(principal.getName())) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in to use the AI assistant.");
        }

        // Validate the incoming question.
        if (request == null
                || request.getQuestion() == null
                || request.getQuestion().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Question cannot be empty.");
        }

        String question = request.getQuestion().trim();

        if (question.length() > 10000) {
            return ResponseEntity.badRequest()
                    .body("Question must not exceed 10000 characters.");
        }

        try {
            // Ask Gemini to generate the answer.
            String answer = geminiService.askGemini(question);

            if (answer == null || answer.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body("The AI service returned an empty answer.");
            }

            // GeminiService returns errors as strings beginning with Error:.
            if (answer.startsWith("Error:")) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body("The AI service failed. Please try again.");
            }

            // Save the conversation against the logged-in student's email.
            ChatMessage chat = new ChatMessage();
            chat.setStudentEmail(principal.getName());
            chat.setQuestion(question);
            chat.setAnswer(answer);

            ChatMessage savedChat = repository.save(chat);

            return ResponseEntity.ok(
                    new ChatResponse(savedChat.getAnswer()));

        } catch (Exception e) {
            // Avoid exposing internal server details to the browser.
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to process your question. Please try again.");
        }
    }

    // GET /api/ai/history
    // Return only the logged-in student's chat history.
    @GetMapping("/history")
    public ResponseEntity<?> history(Principal principal) {

        if (principal == null
                || principal.getName() == null
                || principal.getName().isBlank()
                || "anonymousUser".equals(principal.getName())) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in to view chat history.");
        }

        try {
            List<ChatMessage> history =
                    repository.findByStudentEmailOrderByCreatedAtAsc(
                            principal.getName());

            return ResponseEntity.ok(history);

        } catch (Exception e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to load chat history.");
        }
    }
}