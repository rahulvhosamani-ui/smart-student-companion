package com.example.demo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GpaRepository extends JpaRepository<Gpa, Long> {

    // Find GPA record by semester
    Optional<Gpa> findBySemester(Integer semester);

    // Retrieve GPA records in semester order
    List<Gpa> findAllByOrderBySemesterAsc();

    // Check whether a semester already has a GPA record
    boolean existsBySemester(Integer semester);

    // Delete a GPA record by semester
    void deleteBySemester(Integer semester);
}