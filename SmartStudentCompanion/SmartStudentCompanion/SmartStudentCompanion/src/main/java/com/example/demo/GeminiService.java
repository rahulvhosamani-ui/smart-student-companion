package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    // Allow the app to start when AI is not configured. AI requests report a
    // clear setup error from askGemini instead of preventing Spring startup.
    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-2.5-flash}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(10000);
        factory.setReadTimeout(60000);

        this.restTemplate = new RestTemplate(factory);
    }

    public String askGemini(String question) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Gemini API key is not configured.");
        }

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question cannot be empty.");
        }

        if (question.length() > 10000) {
            throw new IllegalArgumentException(
                    "Question cannot exceed 10000 characters.");
        }

        String url =
                "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent";

        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "role", "user",
                                    "parts", List.of(
                                            Map.of(
                                                    "text",
                                                    question.trim()
                                            )
                                    )
                            )
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.7,
                            "maxOutputTokens", 2048
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey.trim());

            String json =
                    objectMapper.writeValueAsString(requestBody);

            HttpEntity<String> entity =
                    new HttpEntity<>(json, headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            entity,
                            String.class
                    );

            String responseBody = response.getBody();

            if (responseBody == null || responseBody.isBlank()) {
                throw new IllegalStateException(
                        "Gemini returned an empty response.");
            }

            JsonNode root =
                    objectMapper.readTree(responseBody);

            // Check whether Gemini returned an API error.
            JsonNode errorNode = root.path("error");

            if (!errorNode.isMissingNode()
                    && !errorNode.isNull()) {
                String errorMessage =
                        errorNode.path("message").asText(
                                "Unknown Gemini API error.");

                throw new IllegalStateException(
                        "Gemini API error: " + errorMessage);
            }

            JsonNode candidates = root.path("candidates");

            if (!candidates.isArray()
                    || candidates.isEmpty()) {
                throw new IllegalStateException(
                        "Gemini did not return any response candidates.");
            }

            JsonNode candidate = candidates.get(0);

            JsonNode content = candidate.path("content");
            JsonNode parts = content.path("parts");

            List<String> generatedTexts = new ArrayList<>();

            if (parts.isArray()) {
                for (JsonNode part : parts) {
                    JsonNode textNode = part.path("text");

                    if (textNode.isTextual()
                            && !textNode.asText().isBlank()) {
                        generatedTexts.add(textNode.asText());
                    }
                }
            }

            if (generatedTexts.isEmpty()) {
                String finishReason =
                        candidate.path("finishReason").asText("");

                if ("SAFETY".equalsIgnoreCase(finishReason)) {
                    throw new IllegalStateException(
                            "Gemini could not answer this question "
                            + "because it was blocked by safety filters.");
                }

                throw new IllegalStateException(
                        "Gemini returned no generated text. "
                        + "Finish reason: "
                        + (finishReason.isBlank()
                                ? "unknown"
                                : finishReason));
            }

            return String.join("\n", generatedTexts);

        } catch (HttpStatusCodeException e) {
            int status = e.getStatusCode().value();
            String reason = switch (status) {
                case 400, 401, 403 -> "Gemini rejected this API key or request. Check that the key is valid and allowed to use the Gemini API.";
                case 404 -> "The configured Gemini model was not found. Check GEMINI_API_MODEL.";
                case 429 -> "The Gemini API quota or rate limit has been reached. Check the project's Gemini quota and try again later.";
                default -> "Gemini could not generate notes (upstream HTTP " + status + "). Check the Gemini service configuration and try again.";
            };
            throw new IllegalStateException(reason);
        } catch (ResourceAccessException e) {
            throw new IllegalStateException(
                    "The app could not connect to Gemini. Check your internet connection or network restrictions and try again.");
        } catch (IllegalStateException
                | IllegalArgumentException e) {
            throw e;

        } catch (Exception e) {

            throw new IllegalStateException(
                    "An unexpected error occurred while processing "
                    + "the Gemini response.");
        }
    }
}
