package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String askGemini(String question) {
        try {
            String url =
                "https://generativelanguage.googleapis.com/v1beta/models/"
                + "gemini-1.5-flash:generateContent?key=" + apiKey;

            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", question)
                    ))
                )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String json = objectMapper.writeValueAsString(requestBody);

            HttpEntity<String> entity =
                new HttpEntity<>(json, headers);

            ResponseEntity<String> response =
                restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
                );

            JsonNode root =
                objectMapper.readTree(response.getBody());

            JsonNode textNode = root.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");

            if (!textNode.isMissingNode()
                    && !textNode.isNull()
                    && !textNode.asText().isBlank()) {
                return textNode.asText();
            }

            String error = root.path("error").path("message").asText();

            if (!error.isBlank()) {
                return "Error: " + error;
            }

            return "Error: Gemini returned no generated text.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}