package com.local.SummarizerOrchestrator.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import com.local.SummarizerOrchestrator.utils.JSONCleaner;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Integration provider for Anthropic API summarization tasks.
 *
 * <p>This class handles the communication with the Anthropic API, sending summarization requests
 * and processing responses to extract and clean generated summaries.</p>
 */
@Component
public class AnthropicProvider implements SummarizationProvider {

    private static final Logger logger = LoggerFactory.getLogger(AnthropicProvider.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${anthropic.api.url}")
    @NotBlank(message = "Anthropic API URL must not be blank.")
    private String apiUrl;

    @Value("${anthropic.api.key}")
    @NotBlank(message = "Anthropic API Key must not be blank.")
    private String apiKey;

    @Value("${anthropic.api.model}")
    @NotBlank(message = "Anthropic Model Name must not be blank.")
    private String modelName;

    /**
     * Sends a summarization request to Anthropic's API and processes the response.
     *
     * @param request The validated summarization request DTO.
     * @return A {@link SummarizationResponseDTO} containing the summary or an error message.
     */
    @Override
    public SummarizationResponseDTO summarize(@Valid @NotNull SummarizationRequestDTO request) {
        logger.info("Starting summarization request for transcript ID: {}", request.getTranscriptId());

        SummarizationResponseDTO responseDTO = new SummarizationResponseDTO();
        responseDTO.setProviderName(getProviderName());

        String userMessage = buildUserMessage(request);
        String jsonPayload = buildJsonPayload(userMessage, request.getParameters());

        logger.debug("AnthropicProvider JSON Payload: {}", jsonPayload);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost postRequest = buildHttpPostRequest(jsonPayload);

            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                logger.debug("AnthropicProvider Raw JSON Response Body: {}", responseBody);

                String rawText = extractAssistantText(responseBody);
                String cleanedText = JSONCleaner.cleanGeneratedText(rawText);

                responseDTO.setSummary(cleanedText);
                logger.info("Successfully completed summarization request for transcript ID: {}", request.getTranscriptId());
            }
        } catch (IOException e) {
            logger.error("Error during summarization request: {}", e.getMessage(), e);
            responseDTO.setSummary("Error: " + e.getMessage());
        }

        return responseDTO;
    }

    /**
     * Retrieves the name of the summarization provider.
     *
     * @return The provider name.
     */
    @Override
    public String getProviderName() {
        return "Anthropic";
    }

    /**
     * Builds the user message from the provided summarization request.
     *
     * @param request The summarization request DTO.
     * @return The constructed user message.
     */
    private String buildUserMessage(SummarizationRequestDTO request) {
        return request.getPrompt() + "\n" + request.getContext();
    }

    /**
     * Constructs the JSON payload for the summarization request.
     *
     * @param userMessage The user message to include in the payload.
     * @param parameters  Additional parameters for customization.
     * @return The JSON payload as a string.
     */
    private String buildJsonPayload(String userMessage, Map<String, Object> parameters) {
        Map<String, Object> params = parameters != null ? parameters : Map.of();
        int maxTokens = (int) params.getOrDefault("max_tokens", 200);
        double temperature = ((Number) params.getOrDefault("temperature", 0.7)).doubleValue();

        return String.format(
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
    }

    /**
     * Constructs the HTTP POST request for the Anthropic API.
     *
     * @param jsonPayload The JSON payload to include in the request body.
     * @return A fully configured {@link HttpPost} instance.
     */
    private HttpPost buildHttpPostRequest(String jsonPayload) {
        HttpPost postRequest = new HttpPost(apiUrl);
        postRequest.setHeader("Content-Type", "application/json");
        postRequest.setHeader("x-api-key", apiKey);
        postRequest.setHeader("anthropic-version", "2023-06-01");
        postRequest.setEntity(new StringEntity(jsonPayload, StandardCharsets.UTF_8));
        return postRequest;
    }

    /**
     * Extracts the assistant text from the Anthropic API's JSON response.
     *
     * @param responseBody The raw JSON response body.
     * @return The extracted assistant text or an error message if parsing fails.
     */
    private String extractAssistantText(String responseBody) {
        try {
            JsonNode root = MAPPER.readTree(responseBody);
            JsonNode contentNode = root.get("content");
            if (contentNode != null && contentNode.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode node : contentNode) {
                    if ("text".equals(node.get("type").asText())) {
                        sb.append(node.get("text").asText());
                    }
                }
                return sb.toString();
            }
        } catch (Exception e) {
            logger.error("Error parsing assistant text from response: {}", e.getMessage(), e);
            return "Error parsing assistant text: " + e.getMessage();
        }
        return "";
    }

    /**
     * Escapes JSON string values for safe inclusion in payloads.
     *
     * @param text The text to escape.
     * @return The escaped text.
     */
    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
