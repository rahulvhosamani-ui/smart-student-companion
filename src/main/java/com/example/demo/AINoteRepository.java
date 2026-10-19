package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AINoteRepository extends JpaRepository<AINote, Long> {

    List<AINote> findAllByStudent_IdOrderByUpdatedAtDesc(Long studentId);

    Optional<AINote> findByIdAndStudent_Id(Long id, Long studentId);

    List<AINote> findAllByStudent_IdAndSubjectIgnoreCaseOrderByUpdatedAtDesc(
            Long studentId, String subject);

    List<AINote> findAllByStudent_IdAndTopicContainingIgnoreCaseOrderByUpdatedAtDesc(
            Long studentId, String topic);

    long countByStudent_Id(Long studentId);
}