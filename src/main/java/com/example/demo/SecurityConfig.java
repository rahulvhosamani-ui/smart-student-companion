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

    public SecurityConfig(
            CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            // Keep CSRF protection enabled.
            .authorizeHttpRequests(auth -> auth

                // Public pages and resources
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/register",
                    "/perform_login",
                    "/manifest.json",
                    "/sw.js",
                    "/favicon.ico",
                    "/icon-192.png",
                    "/icon-512.png",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/webjars/**"
                ).permitAll()

                // Student pages
                .requestMatchers(
                    "/dashboard.html",
                    "/notes.html",
                    "/assignments.html",
                    "/attendance.html",
                    "/gpa.html",
                    "/reminders.html",
                    "/ainotes.html",
                    "/flashcards.html",
                    "/chatbot.html"
                ).authenticated()

                // API endpoints require authentication
                .requestMatchers("/api/**").authenticated()

                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/perform_login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard.html", true)
                .failureUrl("/index.html?error=true")
            .permitAll()
)

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/index.html?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
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