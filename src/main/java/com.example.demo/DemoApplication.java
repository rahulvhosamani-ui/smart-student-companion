package com.example.demo;

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

    @Bean
    CommandLineRunner initDatabase(
            StudentRepository repository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (repository.findByEmail(
                    "rahulvhosamani@gmail.com").isEmpty()) {

                Student student = new Student(
                        "Rahul",
                        "rahulvhosamani@gmail.com",
                        passwordEncoder.encode("Rahul@022805")
                );

                repository.save(student);

                System.out.println("Student account created successfully!");
            }
        };
    }
}