package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin("*")
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
    public Attendance saveAttendance(
            @RequestBody Attendance attendance) {

        return attendanceRepository.save(attendance);
    }

    @DeleteMapping("/{id}")
    public void deleteAttendance(
            @PathVariable Long id) {

        attendanceRepository.deleteById(id);
    }
}