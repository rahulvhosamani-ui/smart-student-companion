package com.example.demo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ainotes")
public class AINoteController {

    private final AINoteRepository ainoteRepository;
    private final StudentRepository studentRepository;
    private final GeminiService geminiService;

    public AINoteController(
            AINoteRepository ainoteRepository,
            StudentRepository studentRepository,
            GeminiService geminiService) {

        this.ainoteRepository = ainoteRepository;
        this.studentRepository = studentRepository;
        this.geminiService = geminiService;
    }

    // Request received from the AI Notes page.
    public record NoteRequest(
            String title,
            String subject,
            String topic,
            String content,
            String level,
            String style) {
    }

    // Response sent to the frontend without exposing the Student entity.
    public record NoteResponse(
            Long id,
            String title,
            String subject,
            String topic,
            String content,
            String createdAt,
            String updatedAt) {
    }

    private Student currentStudent(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || "anonymousUser".equals(authentication.getName())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in to access your notes.");
        }

        return studentRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Student account not found."));
    }

    private String cleanOptional(String value) {
        return value == null ? "" : value.trim();
    }

    private NoteResponse toResponse(AINote note) {
        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getSubject(),
                note.getTopic(),
                note.getContent(),
                note.getCreatedAt() == null
                        ? null : note.getCreatedAt().toString(),
                note.getUpdatedAt() == null
                        ? null : note.getUpdatedAt().toString());
    }

    private void validateNote(
            String title,
            String subject,
            String topic,
            String content) {

        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Title is required.");
        }

        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Content is required.");
        }

        if (title.trim().length() > 200) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Title cannot exceed 200 characters.");
        }

        if (subject != null && subject.trim().length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Subject cannot exceed 100 characters.");
        }

        if (topic != null && topic.trim().length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Topic cannot exceed 100 characters.");
        }

        if (content.length() > 500_000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Content is too long.");
        }
    }

    // GET /api/ainotes
    // List only the logged-in student's notes.
    @GetMapping
    @Transactional(readOnly = true)
    public List<NoteResponse> getNotes(
            Authentication authentication) {

        Student student = currentStudent(authentication);

        return ainoteRepository
                .findAllByStudent_IdOrderByUpdatedAtDesc(student.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // POST /api/ainotes/generate
    // Generate AI notes and save them to the database.
    @PostMapping("/generate")
    public NoteResponse generateNotes(
            @RequestBody NoteRequest request,
            Authentication authentication) {

        if (request == null
                || request.topic() == null
                || request.topic().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Please enter a topic.");
        }

        Student student = currentStudent(authentication);

        String topic = request.topic().trim();
        String subject = cleanOptional(request.subject());
        String title = cleanOptional(request.title());
        String level = cleanOptional(request.level());
        String style = cleanOptional(request.style());

        if (topic.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Topic cannot exceed 100 characters.");
        }

        if (subject.isBlank()) {
            subject = "General";
        }

        if (title.isBlank()) {
            title = topic;
        }

        if (title.length() > 200 || subject.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Title or subject is too long.");
        }

        if (level.length() > 50 || style.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Level or style is too long.");
        }

        String prompt = """
                Create clear, accurate, well-organized study notes
                for a college student.

                Subject: %s
                Topic: %s
                Student level: %s
                Preferred style: %s

                Include:
                1. Introduction
                2. Important definitions
                3. Key concepts explained simply
                4. Examples where useful
                5. Important points for exams
                6. A short summary

                Use readable headings, numbered sections and bullet points.
                Do not invent facts. Make the notes useful for revision.
                """.formatted(
                subject,
                topic,
                level.isBlank() ? "College" : level,
                style.isBlank() ? "Detailed notes" : style);

        String content;
        try {
            content = geminiService.askGemini(prompt);
        } catch (IllegalStateException exception) {

                exception.printStackTrace();

                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        exception.getMessage());
        } catch (Exception exception) {

                exception.printStackTrace();

                return null;
        }

        if (content == null
                || content.isBlank()
                || content.startsWith("Error:")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "AI generation failed. Please check the Gemini API "
                            + "configuration and try again.");
        }

        AINote note = new AINote();
        note.setTitle(title);
        note.setSubject(subject);
        note.setTopic(topic);
        note.setContent(content);
        note.setStudent(student);

        return toResponse(ainoteRepository.save(note));
    }

    // POST /api/ainotes
    // Save a manually written or edited note.
    @PostMapping
    public NoteResponse saveNote(
            @RequestBody NoteRequest request,
            Authentication authentication) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Note data is required.");
        }

        Student student = currentStudent(authentication);

        String title = cleanOptional(request.title());
        String subject = cleanOptional(request.subject());
        String topic = cleanOptional(request.topic());

        validateNote(title, subject, topic, request.content());

        AINote note = new AINote();
        note.setTitle(title);
        note.setSubject(subject.isBlank() ? "General" : subject);
        note.setTopic(topic);
        note.setContent(request.content());
        note.setStudent(student);

        return toResponse(ainoteRepository.save(note));
    }

    // PUT /api/ainotes/{id}
    // Update only a note owned by the logged-in student.
    @PutMapping("/{id}")
    public NoteResponse updateNote(
            @PathVariable Long id,
            @RequestBody NoteRequest request,
            Authentication authentication) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Note data is required.");
        }

        Student student = currentStudent(authentication);

        AINote note = ainoteRepository
                .findByIdAndStudent_Id(id, student.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Note not found."));

        String title = cleanOptional(request.title());
        String subject = cleanOptional(request.subject());
        String topic = cleanOptional(request.topic());

        validateNote(title, subject, topic, request.content());

        note.setTitle(title);
        note.setSubject(subject.isBlank() ? "General" : subject);
        note.setTopic(topic);
        note.setContent(request.content());

        return toResponse(ainoteRepository.save(note));
    }

    // DELETE /api/ainotes/{id}
    // Delete only a note owned by the logged-in student.
    @DeleteMapping("/{id}")
    public void deleteNote(
            @PathVariable Long id,
            Authentication authentication) {

        Student student = currentStudent(authentication);

        AINote note = ainoteRepository
                .findByIdAndStudent_Id(id, student.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Note not found."));

        ainoteRepository.delete(note);
    }
}
