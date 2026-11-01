package com.example.demo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    // Find notes by subject
    List<Note> findBySubjectIgnoreCase(String subject);

    // Search notes by title
    List<Note> findByTitleContainingIgnoreCase(String title);

    // Find notes by subject, ordered by most recently created
    List<Note> findBySubjectIgnoreCaseOrderByCreatedAtDesc(
            String subject);

    // Search notes by title or content
    List<Note> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String title,
            String content);
}