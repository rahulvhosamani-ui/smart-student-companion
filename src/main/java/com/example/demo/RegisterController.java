package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {

    private final StudentService studentService;
    private final StudentRepository studentRepository;

    public RegisterController(
            StudentService studentService,
            StudentRepository studentRepository) {
        this.studentService = studentService;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerStudent(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            model.addAttribute("error", "Please fill in all fields.");
            return "register";
        }

        if (password.length() < 8) {
            model.addAttribute(
                "error", "Password must contain at least 8 characters.");
            return "register";
        }

        if (studentRepository.findByEmail(email.trim()).isPresent()) {
            model.addAttribute(
                "error", "This email is already registered. Please log in.");
            return "register";
        }
       
        try {
            Student student = new Student();
            student.setName(name.trim());
            student.setEmail(email.trim());
            student.setPassword(password);

            // StudentService must BCrypt-encode the password
            // before saving it.
            studentService.registerStudent(student);

            redirectAttributes.addFlashAttribute(
                "registered", "Registration successful. Please log in.");
                return "redirect:/index.html?registered=true";

        } catch (Exception e) {
            model.addAttribute(
                "error", "Registration failed. Please try again.");
            return "register";
        }
    }
}