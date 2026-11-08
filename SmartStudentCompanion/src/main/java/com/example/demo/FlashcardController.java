package com.example.demo;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/flashcards")
public class FlashcardController {

    private final FlashcardRepository repository;
    private final CurrentStudent currentStudent;

    public FlashcardController(FlashcardRepository repository, CurrentStudent currentStudent) {
        this.repository = repository;
        this.currentStudent = currentStudent;
    }

    @GetMapping
    public List<Flashcard> getAll(Authentication auth) {
        return repository.findAllByStudent_IdOrderByIdDesc(currentStudent.require(auth).getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Flashcard> getOne(@PathVariable Long id, Authentication auth) {
        return repository.findByIdAndStudent_Id(id, currentStudent.require(auth).getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Flashcard> create(
            @Valid @RequestBody Flashcard flashcard, Authentication auth) {
        flashcard.setId(null);
        flashcard.setStudent(currentStudent.require(auth));
        Flashcard saved = repository.saveAndFlush(flashcard);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Flashcard> update(
            @PathVariable Long id,
            @Valid @RequestBody Flashcard updated, Authentication auth) {
        return repository.findByIdAndStudent_Id(id, currentStudent.require(auth).getId())
                .map(existing -> {
                    existing.setQuestion(updated.getQuestion());
                    existing.setAnswer(updated.getAnswer());
                    existing.setKnown(updated.isKnown());
                    return ResponseEntity.ok(repository.saveAndFlush(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        Long studentId = currentStudent.require(auth).getId();
        if (!repository.existsByIdAndStudent_Id(id, studentId)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteByIdAndStudent_Id(id, studentId);
        return ResponseEntity.noContent().build();
    }
}
