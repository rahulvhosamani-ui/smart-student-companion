package com.example.demo;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private static final Logger log =
            LoggerFactory.getLogger(NoteController.class);

    private final NoteRepository noteRepository;
    private final CurrentStudent currentStudent;

    public NoteController(NoteRepository noteRepository, CurrentStudent currentStudent) {
        this.noteRepository = noteRepository;
        this.currentStudent = currentStudent;
    }

    // Retrieve all notes
    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes(Authentication auth) {
        log.info("Loading notes");

        return ResponseEntity.ok(noteRepository.findAllByStudent_IdOrderByCreatedAtDesc(currentStudent.require(auth).getId()));
    }

    // Retrieve one note
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(
            @PathVariable Long id, Authentication auth) {

        return noteRepository.findByIdAndStudent_Id(id, currentStudent.require(auth).getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create a new note
    @PostMapping
    public ResponseEntity<?> saveNote(
            @Valid @RequestBody Note note, Authentication auth) {

        log.info("Save note request received");

        String error = validateNote(note);

        if (error != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", error));
        }

        try {
            // Ensure the database generates a new ID.
            note.setId(null);
            note.setStudent(currentStudent.require(auth));
            normalizeNote(note);

            Note saved = noteRepository.saveAndFlush(note);

            log.info("Note saved successfully, id={}", saved.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(saved);

        } catch (Exception ex) {
            log.error("Failed to save note", ex);

            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error",
                            "Failed to save note. Please try again."
                    ));
        }
    }

    // Update an existing note
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody Note updatedNote, Authentication auth) {

        String error = validateNote(updatedNote);

        if (error != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", error));
        }

        return noteRepository.findByIdAndStudent_Id(id, currentStudent.require(auth).getId())
                .map(note -> {
                    normalizeNote(updatedNote);

                    note.setTitle(updatedNote.getTitle());
                    note.setContent(updatedNote.getContent());
                    note.setSubject(updatedNote.getSubject());
                    note.setTags(updatedNote.getTags());

                    try {
                        Note saved =
                                noteRepository.saveAndFlush(note);

                        log.info(
                                "Note updated successfully, id={}",
                                saved.getId());

                        return ResponseEntity.ok(saved);

                    } catch (Exception ex) {
                        log.error(
                                "Failed to update note id={}", id, ex);

                        return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of(
                                        "error",
                                        "Failed to update note."
                                ));
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete a note
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long id, Authentication auth) {

        Long studentId = currentStudent.require(auth).getId();
        if (!noteRepository.existsByIdAndStudent_Id(id, studentId)) {
            return ResponseEntity.notFound().build();
        }

        noteRepository.deleteByIdAndStudent_Id(id, studentId);

        log.info("Note deleted successfully, id={}", id);

        return ResponseEntity.noContent().build();
    }

    // Validate incoming note data
    private String validateNote(Note note) {

        if (note == null) {
            return "Note data is required.";
        }

        if (note.getTitle() == null
                || note.getTitle().isBlank()) {
            return "Note title is required.";
        }

        if (note.getTitle().trim().length() > 150) {
            return "Title cannot exceed 150 characters.";
        }

        if (note.getContent() == null
                || note.getContent().isBlank()) {
            return "Note content is required.";
        }

        if (note.getSubject() != null
                && note.getSubject().trim().length() > 100) {
            return "Subject cannot exceed 100 characters.";
        }

        if (note.getTags() != null
                && note.getTags().trim().length() > 200) {
            return "Tags cannot exceed 200 characters.";
        }

        return null;
    }

    // Normalize fields before saving
    private void normalizeNote(Note note) {
        note.setTitle(note.getTitle().trim());
        note.setContent(note.getContent().trim());

        if (note.getSubject() == null
                || note.getSubject().isBlank()) {
            note.setSubject("General");
        } else {
            note.setSubject(note.getSubject().trim());
        }

        if (note.getTags() != null) {
            note.setTags(note.getTags().trim());
        }
    }
}
