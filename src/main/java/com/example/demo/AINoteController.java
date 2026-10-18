package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/ainotes")
public class AinoteController {

    private final AinoteRepository ainoteRepository;
    private final StudentRepository studentRepository;
    private final GeminiService geminiService;

    public AinoteController(
            AinoteRepository ainoteRepository,
            StudentRepository studentRepository,
            GeminiService geminiService) {
        this.ainoteRepository = ainoteRepository;
        this.studentRepository = studentRepository;
        this.geminiService = geminiService;
    }

    public record NoteRequest(
            String title,
            String subject,
            String topic,
            String content) {
    }

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
                || authentication.getName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Please log in.");
        }

        return studentRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Student account not found."));
    }

    private NoteResponse toResponse(AInote note) {
        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getSubject(),
                note.getTopic(),
                note.getContent(),
                note.getCreatedAt() == null
                        ? null : note.getCreatedAt().toString(),
                note.getUpdatedAt() == null
                        ? null : note.getUpdatedAt().toString()
        );
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<NoteResponse> getNotes(Authentication authentication) {
        Student student = currentStudent(authentication);

        return ainoteRepository
                .findAllByStudent_IdOrderByUpdatedAtDesc(student.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Generate AI notes and save them to MySQL.
    @PostMapping("/generate")
    public NoteResponse generateNotes(
            @RequestBody NoteRequest request,
            Authentication authentication) {

        Student student = currentStudent(authentication);

        if (request.topic() == null || request.topic().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Enter a topic.");
        }

        String topic = request.topic().trim();

        if (topic.length() > 200) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Topic is too long.");
        }

        String subject = request.subject() == null
                || request.subject().isBlank()
                ? "General" : request.subject().trim();

        if (subject.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Subject is too long.");
        }

        String title = request.title() == null
                || request.title().isBlank()
                ? topic : request.title().trim();

        if (title.length() > 200) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Title is too long.");
        }

        String prompt = """
                Create clear, well-structured study notes for a college student.
                Subject: %s
                Topic: %s

                Include an introduction, key concepts, definitions,
                examples, important points and a short summary.
                Use plain text headings and bullet points.
                """.formatted(subject, topic);

        String content = geminiService.askGemini(prompt);

        if (content == null || content.isBlank()
                || content.startsWith("Error:")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "AI generation failed. Please try again.");
        }

        AInote note = new AInote();
        note.setTitle(title);
        note.setSubject(subject);
        note.setTopic(topic);
        note.setContent(content);
        note.setStudent(student);

        return toResponse(ainoteRepository.save(note));
    }

    // Save a note written or edited by the student.
    @PostMapping
    public NoteResponse saveNote(
            @RequestBody NoteRequest request,
            Authentication authentication) {

        Student student = currentStudent(authentication);

        if (request.title() == null || request.title().isBlank()
                || request.content() == null
                || request.content().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Title and content are required.");
        }

        if (request.title().trim().length() > 200
                || (request.subject() != null
                    && request.subject().trim().length() > 100)
                || (request.topic() != null
                    && request.topic().trim().length() > 200)
                || request.content().length() > 500_000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "One or more fields exceed the allowed length.");
        }

        AInote note = new AInote();
        note.setTitle(request.title().trim());
        note.setSubject(request.subject() == null
                || request.subject().isBlank()
                ? "General" : request.subject().trim());
        note.setTopic(request.topic() == null
                ? "" : request.topic().trim());
        note.setContent(request.content());
        note.setStudent(student);

        return toResponse(ainoteRepository.save(note));
    }

    // Update only a note owned by the logged-in student.
    @PutMapping("/{id}")
    public NoteResponse updateNote(
            @PathVariable Long id,
            @RequestBody NoteRequest request,
            Authentication authentication) {

        Student student = currentStudent(authentication);

        AInote note = ainoteRepository
                .findByIdAndStudent_Id(id, student.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Note not found."));

        if (request.title() == null || request.title().isBlank()
                || request.content() == null
                || request.content().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Title and content are required.");
        }

        if (request.title().trim().length() > 200
                || (request.subject() != null
                    && request.subject().trim().length() > 100)
                || (request.topic() != null
                    && request.topic().trim().length() > 200)
                || request.content().length() > 500_000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "One or more fields exceed the allowed length.");
        }

        note.setTitle(request.title().trim());
        note.setSubject(request.subject() == null
                || request.subject().isBlank()
                ? "General" : request.subject().trim());
        note.setTopic(request.topic() == null
                ? "" : request.topic().trim());
        note.setContent(request.content());

        return toResponse(ainoteRepository.save(note));
    }

    // Delete only a note owned by the logged-in student.
    @DeleteMapping("/{id}")
    public void deleteNote(
            @PathVariable Long id,
            Authentication authentication) {

        Student student = currentStudent(authentication);

        AInote note = ainoteRepository
                .findByIdAndStudent_Id(id, student.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Note not found."));

        ainoteRepository.delete(note);
    }
}