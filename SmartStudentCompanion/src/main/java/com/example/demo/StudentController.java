package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // Get all students
    @GetMapping
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // Get student by ID
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {

        return studentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create student
    @PostMapping
    public Student createStudent(@RequestBody Student student) {

        return studentRepository.save(student);
    }

    // Update student
    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(
            @PathVariable Long id,
            @RequestBody Student updatedStudent) {

        return studentRepository.findById(id)
                .map(student -> {

                    student.setName(updatedStudent.getName());
                    student.setEmail(updatedStudent.getEmail());

                    if (updatedStudent.getPassword() != null) {
                        student.setPassword(updatedStudent.getPassword());
                    }

                    return ResponseEntity.ok(
                            studentRepository.save(student)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete student
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStudent(
            @PathVariable Long id) {

        return studentRepository.findById(id)
                .map(student -> {

                    studentRepository.delete(student);

                    return ResponseEntity.ok(
                            "Student deleted successfully"
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Get student by email
    @GetMapping("/profile/{email}")
    public ResponseEntity<Student> getProfile(
            @PathVariable String email) {

        return studentRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}