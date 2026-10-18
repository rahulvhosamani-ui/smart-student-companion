package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AinoteRepository extends JpaRepository<AInote, Long> {

    // Get all AI notes belonging to one student
    List<AInote> findAllByStudent_IdOrderByUpdatedAtDesc(Long studentId);

    // Find one AI note only if it belongs to the student
    Optional<AInote> findByIdAndStudent_Id(Long id, Long studentId);

    // Search notes by subject for one student
    List<AInote> findAllByStudent_IdAndSubjectIgnoreCaseOrderByUpdatedAtDesc(
            Long studentId, String subject);

    // Search notes by topic for one student
    List<AInote> findAllByStudent_IdAndTopicContainingIgnoreCaseOrderByUpdatedAtDesc(
            Long studentId, String topic);

    // Count notes belonging to one student
    long countByStudent_Id(Long studentId);
}