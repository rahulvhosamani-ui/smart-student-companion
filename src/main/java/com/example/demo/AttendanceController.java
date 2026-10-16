package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceRepository attendanceRepository;

    public AttendanceController(
            AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @GetMapping
    public List<Attendance> getAttendance() {
        return attendanceRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> saveAttendance(
            @RequestBody Attendance attendance) {

        ResponseEntity<?> validation = validate(attendance);

        if (validation != null) {
            return validation;
        }

        attendance.setId(null);
        attendance.setSubject(attendance.getSubject().trim());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attendanceRepository.save(attendance));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAttendance(
            @PathVariable Long id,
            @RequestBody Attendance updated) {

        ResponseEntity<?> validation = validate(updated);

        if (validation != null) {
            return validation;
        }

        return attendanceRepository.findById(id)
                .map(existing -> {
                    existing.setSubject(updated.getSubject().trim());
                    existing.setPercentage(updated.getPercentage());

                    // Preserve the existing month if none was provided.
                    if (updated.getMonth() != null) {
                        existing.setMonth(updated.getMonth());
                    }

                    return ResponseEntity.ok(
                            attendanceRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAttendance(
            @PathVariable Long id) {

        if (!attendanceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        attendanceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<?> validate(Attendance attendance) {

        if (attendance.getSubject() == null ||
                attendance.getSubject().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Subject is required."));
        }

        if (attendance.getPercentage() == null ||
                !Double.isFinite(attendance.getPercentage()) ||
                attendance.getPercentage() < 0 ||
                attendance.getPercentage() > 100) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "Percentage must be between 0 and 100."
                    ));
        }

        return null;
    }
}