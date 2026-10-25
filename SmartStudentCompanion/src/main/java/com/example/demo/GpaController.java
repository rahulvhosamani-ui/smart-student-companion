package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/gpa")
@CrossOrigin("*")
public class GpaController {

    private final GpaRepository gpaRepository;

    public GpaController(GpaRepository gpaRepository) {
        this.gpaRepository = gpaRepository;
    }

    @GetMapping
    public List<Gpa> getGpa() {
        return gpaRepository.findAll();
    }

    @PostMapping
    public Gpa saveGpa(@RequestBody Gpa gpa) {
        return gpaRepository.save(gpa);
    }
}