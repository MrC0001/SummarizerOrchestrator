package com.local.SummarizerOrchestrator.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import com.local.SummarizerOrchestrator.utils.JSONCleaner;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class AnthropicProvider implements SummarizationProvider {

    @Value("${anthropic.api.url}")
    private String apiUrl;

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.api.model}")
    private String modelName;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public SummarizationResponseDTO summarize(SummarizationRequestDTO request) {
        SummarizationResponseDTO responseDTO = new SummarizationResponseDTO();
        responseDTO.setProviderName(getProviderName());

        // Assume sanitized input
        String userMessage = request.getPrompt() + "\n" + request.getContext();
        Map<String, Object> params = request.getParameters() != null ? request.getParameters() : Map.of();
        int maxTokens = (int) params.getOrDefault("max_tokens", 200);
        double temperature = ((Number) params.getOrDefault("temperature", 0.7)).doubleValue();

        String jsonPayload = String.format(
                "{" +
                        "\"model\":\"%s\"," +
                        "\"max_tokens\":%d," +
                        "\"temperature\":%.1f," +
                        "\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]" +
                        "}",
                escapeJson(modelName),
                maxTokens,
                temperature,
                escapeJson(userMessage)
        );

        System.out.println("AnthropicProvider JSON Payload:\n" + jsonPayload);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost postRequest = new HttpPost(apiUrl);
            postRequest.setHeader("Content-Type", "application/json");
            postRequest.setHeader("x-api-key", apiKey);
            postRequest.setHeader("anthropic-version", "2023-06-01");

            // Explicitly set UTF-8 encoding for the request body
            postRequest.setEntity(new StringEntity(jsonPayload, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                System.out.println("AnthropicProvider Raw JSON Response Body:\n" + responseBody);

                String rawText = extractAssistantText(responseBody);
                String cleanedText = JSONCleaner.cleanGeneratedText(rawText);

                responseDTO.setSummary(cleanedText);
            }
        } catch (IOException e) {
            responseDTO.setSummary("Error: " + e.getMessage());
            System.err.println("AnthropicProvider Error:\n" + e.getMessage());
        }

        return responseDTO;
    }

    @Override
    public String getProviderName() {
        return "Anthropic";
    }

    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String extractAssistantText(String responseBody) {
        try {
            JsonNode root = MAPPER.readTree(responseBody);
            JsonNode contentArray = root.get("content");
            if (contentArray != null && contentArray.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode node : contentArray) {
                    if (node.get("type") != null && "text".equals(node.get("type").asText())) {
                        sb.append(node.get("text").asText());
                    }
                }
                return sb.toString();
            }
        } catch (Exception e) {
            return "Error parsing assistant text: " + e.getMessage();
        }
        return "";
    }
}
