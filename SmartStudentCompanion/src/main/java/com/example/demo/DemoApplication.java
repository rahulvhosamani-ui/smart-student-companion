package com.example.demo;

import java.util.Locale;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.demo")
@EntityScan(basePackages = "com.example.demo")
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            String configuredEmail =
                    System.getenv("INITIAL_STUDENT_EMAIL");

            String configuredPassword =
                    System.getenv("INITIAL_STUDENT_PASSWORD");

            // Skip initialization if credentials are not configured.
            if (configuredEmail == null
                    || configuredEmail.isBlank()
                    || configuredPassword == null
                    || configuredPassword.isBlank()) {

                System.out.println(
                        "Initial student account setup skipped. "
                        + "Configure INITIAL_STUDENT_EMAIL and "
                        + "INITIAL_STUDENT_PASSWORD if required.");

                return;
            }

            String email = configuredEmail.trim()
                    .toLowerCase(Locale.ROOT);

            if (configuredPassword.length() < 8) {
                throw new IllegalStateException(
                        "Initial student password must contain "
                        + "at least 8 characters.");
            }

            if (studentRepository.findByEmail(email).isPresent()) {
                System.out.println(
                        "Initial student account already exists.");
                return;
            }

            Student student = new Student(
                    "Rahul",
                    email,
                    passwordEncoder.encode(configuredPassword)
            );

            studentRepository.save(student);

            System.out.println(
                    "Initial student account created successfully.");
        };
    }
}