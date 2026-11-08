package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashcardRepository
        extends JpaRepository<Flashcard, Long> {
    java.util.List<Flashcard> findAllByStudent_IdOrderByIdDesc(Long studentId);
    java.util.Optional<Flashcard> findByIdAndStudent_Id(Long id, Long studentId);
    boolean existsByIdAndStudent_Id(Long id, Long studentId);
    void deleteByIdAndStudent_Id(Long id, Long studentId);
}
