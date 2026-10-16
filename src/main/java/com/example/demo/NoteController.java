package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteRepository noteRepository;

    public NoteController(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @GetMapping
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> saveNote(@RequestBody Note note) {
        if (note.getTitle() == null ||
                note.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Note title is required.");
        }

        if (note.getContent() == null ||
                note.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Note content is required.");
        }

        note.setTitle(note.getTitle().trim());

        if (note.getSubject() == null ||
                note.getSubject().trim().isEmpty()) {
            note.setSubject("General");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(noteRepository.save(note));
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
                                .body((Object) "Note title is required.");
                    }

                    if (updatedNote.getContent() == null ||
                            updatedNote.getContent().trim().isEmpty()) {
                        return ResponseEntity.badRequest()
                                .body((Object) "Note content is required.");
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