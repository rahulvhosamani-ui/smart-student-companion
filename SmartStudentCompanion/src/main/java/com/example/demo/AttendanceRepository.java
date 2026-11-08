package com.example.demo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepository
        extends JpaRepository<Attendance, Long> {

    List<Attendance> findAllByStudent_IdOrderByMonthDesc(Long studentId);
    java.util.Optional<Attendance> findByIdAndStudent_Id(Long id, Long studentId);
    boolean existsByIdAndStudent_Id(Long id, Long studentId);
    void deleteByIdAndStudent_Id(Long id, Long studentId);

    // Find attendance records by subject
    List<Attendance> findBySubjectIgnoreCase(String subject);

    // Find attendance records by month
    List<Attendance> findByMonthIgnoreCase(String month);

    // Find attendance records by subject and month
    List<Attendance> findBySubjectIgnoreCaseAndMonthIgnoreCase(
            String subject,
            String month);

    // Find records with attendance at or above a percentage
    List<Attendance> findByPercentageGreaterThanEqual(
            Double percentage);

    // Find records with attendance below a percentage
    List<Attendance> findByPercentageLessThan(
            Double percentage);
}
