package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // Redirect the root URL to the homepage
    @GetMapping("/")
    public String home() {
        return "redirect:/index.html";
    }

    // Open the login page
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Open the registration page
    @GetMapping("/login/register")
    public String register() {
        return "register";
    }
}