package com.example.demo;

import java.util.List;
import java.util.Map;
import java.time.YearMonth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceRepository attendanceRepository;

    public AttendanceController(
            AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    // Get all attendance records
    @GetMapping
    public ResponseEntity<List<Attendance>> getAttendance() {
        return ResponseEntity.ok(attendanceRepository.findAll());
    }

    // Add a new attendance record
    @PostMapping
    public ResponseEntity<?> saveAttendance(
            @RequestBody Attendance attendance) {

        if (attendance != null
                && (attendance.getMonth() == null
                    || attendance.getMonth().isBlank())) {
            attendance.setMonth(YearMonth.now().toString());
        }

        String error = validate(attendance);

        if (error != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", error));
        }

        attendance.setId(null);
        attendance.setSubject(attendance.getSubject().trim());
        attendance.setMonth(attendance.getMonth().trim());

        Attendance saved = attendanceRepository.save(attendance);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Update an existing attendance record
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAttendance(
            @PathVariable Long id,
            @RequestBody Attendance updated) {

        if (updated != null
                && (updated.getMonth() == null
                    || updated.getMonth().isBlank())) {
            updated.setMonth(YearMonth.now().toString());
        }

        String error = validate(updated);

        if (error != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", error));
        }

        return attendanceRepository.findById(id)
                .map(existing -> {

                    existing.setSubject(updated.getSubject().trim());
                    existing.setPercentage(updated.getPercentage());

                    if (updated.getMonth() != null
                            && !updated.getMonth().isBlank()) {
                        existing.setMonth(updated.getMonth().trim());
                    }

                    Attendance saved =
                            attendanceRepository.save(existing);

                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete an attendance record
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttendance(
            @PathVariable Long id) {

        if (!attendanceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        attendanceRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    // Validate attendance input
    private String validate(Attendance attendance) {

        if (attendance == null) {
            return "Attendance data is required.";
        }

        if (attendance.getSubject() == null
                || attendance.getSubject().isBlank()) {
            return "Subject is required.";
        }

        if (attendance.getSubject().trim().length() > 100) {
            return "Subject cannot exceed 100 characters.";
        }

        if (attendance.getPercentage() == null
                || !Double.isFinite(attendance.getPercentage())
                || attendance.getPercentage() < 0
                || attendance.getPercentage() > 100) {
            return "Percentage must be between 0 and 100.";
        }

        if (attendance.getMonth() == null
                || attendance.getMonth().isBlank()) {
            return "Month is required.";
        }

        if (attendance.getMonth().trim().length() > 20) {
            return "Month cannot exceed 20 characters.";
        }

        return null;
    }
}
