package com.example.demo;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {

    private static final Logger log =
            LoggerFactory.getLogger(RegisterController.class);

    private final StudentService studentService;
    private final StudentRepository studentRepository;

    public RegisterController(
            StudentService studentService,
            StudentRepository studentRepository) {
        this.studentService = studentService;
        this.studentRepository = studentRepository;
    }

    // Display the registration page
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    // Register a new student
    @PostMapping("/register")
    public String registerStudent(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Validate required fields
        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()) {

            model.addAttribute(
                    "error", "Please fill in all fields.");
            return "register";
        }

        name = name.trim();
        email = email.trim().toLowerCase(Locale.ROOT);

        // Validate name
        if (name.length() > 100) {
            model.addAttribute(
                    "error", "Name cannot exceed 100 characters.");
            return "register";
        }

        // Basic email validation
        if (email.length() > 254
                || !email.matches(
                        "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+"
                        + "@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}"
                        + "[A-Za-z0-9])?"
                        + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}"
                        + "[A-Za-z0-9])?)+$")) {

            model.addAttribute(
                    "error", "Please enter a valid email address.");
            return "register";
        }

        // Validate password
        if (password.length() < 8) {
            model.addAttribute(
                    "error",
                    "Password must contain at least 8 characters.");
            return "register";
        }

        if (password.length() > 72) {
            model.addAttribute(
                    "error",
                    "Password is too long. Maximum 72 characters.");
            return "register";
        }

        // Check whether the email already exists
        if (studentRepository.findByEmail(email).isPresent()) {
            model.addAttribute(
                    "error",
                    "This email is already registered. Please log in.");
            return "register";
        }

        try {
            Student student = new Student();
            student.setName(name);
            student.setEmail(email);
            student.setPassword(password);

            // StudentService must encode the password with BCrypt
            // before saving the student.
            studentService.registerStudent(student);

            redirectAttributes.addFlashAttribute(
                    "registered",
                    "Registration successful. Please log in.");

            return "redirect:/login?registered=true";

        } catch (DataIntegrityViolationException e) {
            // Handles a duplicate-email race condition.
            log.warn("Registration rejected due to a database constraint.");

            model.addAttribute(
                    "error",
                    "This email may already be registered. "
                    + "Please try logging in.");
            return "register";

        } catch (Exception e) {
            log.error("Student registration failed.", e);

            model.addAttribute(
                    "error",
                    "Registration failed. Please try again.");
            return "register";
        }
    }
}