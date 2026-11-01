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

@RestController
@RequestMapping("/api/flashcards")
public class FlashcardController {

    private final FlashcardRepository repository;

    public FlashcardController(FlashcardRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Flashcard> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Flashcard> getOne(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Flashcard> create(
            @Valid @RequestBody Flashcard flashcard) {
        flashcard.setId(null);
        Flashcard saved = repository.saveAndFlush(flashcard);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Flashcard> update(
            @PathVariable Long id,
            @Valid @RequestBody Flashcard updated) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setQuestion(updated.getQuestion());
                    existing.setAnswer(updated.getAnswer());
                    existing.setKnown(updated.isKnown());
                    return ResponseEntity.ok(repository.saveAndFlush(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
