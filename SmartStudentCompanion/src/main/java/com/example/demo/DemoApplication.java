package com.example.demo;

import java.util.Locale;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    /** Optional first account, configured only through deployment environment variables. */
    @Bean
    CommandLineRunner initDatabase(StudentRepository repository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            String configuredEmail = System.getenv("INITIAL_STUDENT_EMAIL");
            String configuredPassword = System.getenv("INITIAL_STUDENT_PASSWORD");

            if (configuredEmail == null || configuredEmail.isBlank()
                    || configuredPassword == null || configuredPassword.isBlank()) {
                return;
            }
            if (configuredPassword.length() < 8 || configuredPassword.length() > 72) {
                throw new IllegalStateException("INITIAL_STUDENT_PASSWORD must be 8 to 72 characters.");
            }

            String email = configuredEmail.trim().toLowerCase(Locale.ROOT);
            if (repository.findByEmail(email).isPresent()) return;

            Student student = new Student("Student", email,
                    passwordEncoder.encode(configuredPassword));
            repository.save(student);
        };
    }
}
