package com.example.demo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentRepository
        extends JpaRepository<Assignment, Long> {

    List<Assignment> findAllByStudent_IdOrderByDueDateAsc(Long studentId);
    java.util.Optional<Assignment> findByIdAndStudent_Id(Long id, Long studentId);
    boolean existsByIdAndStudent_Id(Long id, Long studentId);
    void deleteByIdAndStudent_Id(Long id, Long studentId);

    // Find assignments by subject
    List<Assignment> findBySubjectIgnoreCase(String subject);

    // Find assignments by completion status
    List<Assignment> findByCompleted(boolean completed);

    // Find assignments by status
    List<Assignment> findByStatusIgnoreCase(String status);

    // Find assignments by priority
    List<Assignment> findByPriorityIgnoreCase(String priority);
}
