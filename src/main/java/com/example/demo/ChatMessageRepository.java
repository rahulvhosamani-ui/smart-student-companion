package com.example.demo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, Long> {

    // Retrieve a student's chat history in chronological order
    List<ChatMessage> findByStudentEmailOrderByCreatedAtAsc(
            String studentEmail);

    // Retrieve the most recent conversations first
    List<ChatMessage> findByStudentEmailOrderByCreatedAtDesc(
            String studentEmail);

    // Count the messages belonging to a student
    long countByStudentEmail(String studentEmail);

    // Delete only the messages belonging to a specific student
    void deleteByStudentEmail(String studentEmail);
}