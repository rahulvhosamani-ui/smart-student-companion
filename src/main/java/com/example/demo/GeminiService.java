package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String askGemini(String question) {

        try {

            String url =
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key="
                            + apiKey;

            String requestBody =
                    """
                    {
                      "contents": [
                        {
                          "parts": [
                            {
                              "text": "%s"
                            }
                          ]
                        }
                      ]
                    }
                    """.formatted(question.replace("\"", "\\\""));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity =
                    new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            entity,
                            String.class
                    );

            String body = response.getBody();

            if (body == null) {
                return "No response from Gemini.";
            }

            int start =
                    body.indexOf("\"text\": \"");

            if (start != -1) {

                start += 9;

                int end =
                        body.indexOf("\"", start);

                if (end > start) {
                    return body.substring(start, end)
                            .replace("\\n", "\n");
                }
            }

            return body;

        } catch (Exception e) {

            e.printStackTrace();

            return "Error: " + e.getMessage();
        }
    }
}