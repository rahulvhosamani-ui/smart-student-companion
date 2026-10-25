package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // Send visitors to the server-rendered login form.
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    // Open the login page
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
