package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private static final Logger log =
            LoggerFactory.getLogger(NoteController.class);

    private final NoteRepository noteRepository;

    public NoteController(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @GetMapping
    public List<Note> getAllNotes() {
        log.info("Loading notes");
        return noteRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> saveNote(@RequestBody Note note) {
        log.info("Save note request received");

        if (note.getTitle() == null ||
                note.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Note title is required."));
        }

        if (note.getContent() == null ||
                note.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Note content is required."));
        }

        try {
            note.setId(null);
            note.setTitle(note.getTitle().trim());

            if (note.getSubject() == null ||
                    note.getSubject().trim().isEmpty()) {
                note.setSubject("General");
            } else {
                note.setSubject(note.getSubject().trim());
            }

            if (note.getTags() != null &&
                    note.getTags().length() > 200) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Tags cannot exceed 200 characters."));
            }

            log.info("Saving note to database");

            Note saved = noteRepository.saveAndFlush(note);

            log.info("Note saved successfully, id={}", saved.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception ex) {
            log.error("Failed to save note", ex);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to save note. Check server logs."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(
            @PathVariable Long id,
            @RequestBody Note updatedNote) {

        return noteRepository.findById(id)
                .map(note -> {
                    if (updatedNote.getTitle() == null ||
                            updatedNote.getTitle().trim().isEmpty()) {
                        return ResponseEntity.badRequest()
                                .body((Object) Map.of("error", "Title is required."));
                    }

                    if (updatedNote.getContent() == null ||
                            updatedNote.getContent().trim().isEmpty()) {
                        return ResponseEntity.badRequest()
                                .body((Object) Map.of("error", "Content is required."));
                    }

                    note.setTitle(updatedNote.getTitle().trim());
                    note.setContent(updatedNote.getContent());
                    note.setSubject(
                            updatedNote.getSubject() == null ||
                            updatedNote.getSubject().trim().isEmpty()
                                    ? "General"
                                    : updatedNote.getSubject().trim()
                    );
                    note.setTags(updatedNote.getTags());

                    return ResponseEntity.ok(noteRepository.save(note));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        if (!noteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        noteRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}