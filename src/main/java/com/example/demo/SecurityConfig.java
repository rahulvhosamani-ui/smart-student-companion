package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                        "/",
                        "/index.html",
                        "/manifest.json",
                        "/sw.js",
                        "/icon-192.png",
                        "/icon-512.png",
                         "/css/**",
                        "/js/**"
                ).permitAll()

                .requestMatchers(
                        "/dashboard.html",
                        "/notes.html",
                        "/assignments.html",
                        "/attendance.html",
                        "/gpa.html",
                        "/reminders.html",
                        "/ainotes.html",
                        "/flashcards.html"
                ).authenticated()

                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/index.html")
                .loginProcessingUrl("/perform_login")
                .defaultSuccessUrl("/dashboard.html", true)
                .failureUrl("/index.html?error=true")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/index.html?logout")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}