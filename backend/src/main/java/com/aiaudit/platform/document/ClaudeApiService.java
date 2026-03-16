package com.aiaudit.platform.document;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ClaudeApiService {

    private final WebClient webClient;
    private final String model;

    public ClaudeApiService(
            @Value("${claude.api-key:}") String apiKey,
            @Value("${claude.model:claude-sonnet-4-20250514}") String model) {
        this.model = model;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.anthropic.com/v1")
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String generateContent(String prompt) {
        try {
            Map<String, Object> request = Map.of(
                    "model", model,
                    "max_tokens", 8192,
                    "messages", List.of(Map.of("role", "user", "content", prompt))
            );

            Map response = webClient.post()
                    .uri("/messages")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("content")) {
                List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
                if (!content.isEmpty()) {
                    return (String) content.get(0).get("text");
                }
            }
            throw new RuntimeException("Empty response from Claude API");
        } catch (Exception e) {
            log.error("Claude API call failed", e);
            throw new RuntimeException("Failed to generate document content: " + e.getMessage(), e);
        }
    }
}
