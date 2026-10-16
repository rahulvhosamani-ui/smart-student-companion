package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ainotes")
public class AiNotesController {

    private final GeminiService geminiService;
    private final NoteRepository noteRepository;

    public AiNotesController(
            GeminiService geminiService,
            NoteRepository noteRepository) {
        this.geminiService = geminiService;
        this.noteRepository = noteRepository;
    }

    @PostMapping
    public ResponseEntity<?> generateNotes(
            @RequestBody Map<String, String> request) {

        String topic = request.get("topic");

        if (topic == null || topic.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Please enter a topic."));
        }

        String level = request.getOrDefault(
                "level", "College level");

        String style = request.getOrDefault(
                "style", "Detailed notes");

        try {
            String prompt =
                    "Create accurate study notes for this topic: "
                    + topic.trim()
                    + "\nStudy level: " + level
                    + "\nPreferred format: " + style
                    + "\nUse clear headings, definitions, examples "
                    + "and key points where appropriate.";

            // This call must match the method in GeminiService.java.
            String notes = geminiService.generateNotes(prompt);

            if (notes == null || notes.trim().isEmpty()) {
                return ResponseEntity.status(
                        HttpStatus.BAD_GATEWAY)
                        .body(Map.of(
                                "error",
                                "The AI service returned empty notes."
                        ));
            }

            // Save generated notes to the MySQL database.
            Note savedNote = new Note();
            savedNote.setTitle(
                    topic.trim().substring(
                            0, Math.min(topic.trim().length(), 150)));
            savedNote.setContent(notes);
            savedNote.setSubject("General");
            savedNote.setTags("AI Generated");

            noteRepository.save(savedNote);

            return ResponseEntity.ok(
                    Map.of(
                            "notes", notes,
                            "saved", true,
                            "message", "AI notes generated and saved."
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error",
                            "Unable to generate or save notes. "
                                    + "Check the server logs."
                    ));
        }
    }
}