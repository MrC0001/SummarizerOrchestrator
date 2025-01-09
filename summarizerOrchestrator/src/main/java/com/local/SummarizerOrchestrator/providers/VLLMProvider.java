package com.local.SummarizerOrchestrator.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import com.local.SummarizerOrchestrator.utils.JSONCleaner;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
import java.util.List;
import java.util.Map;

/**
 * Provides integration with a locally deployed vLLM server for summarization tasks.
 */
@Component
public class VLLMProvider implements SummarizationProvider {

    private static final Logger logger = LoggerFactory.getLogger(VLLMProvider.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${vllm.model.url}")
    @NotBlank(message = "vLLM API URL must not be blank.")
    private String apiUrl;

    @Value("${vllm.model.name}")
    @NotBlank(message = "vLLM Model Name must not be blank.")
    private String modelName;

    /**
     * Sends a summarization request to the vLLM server.
     *
     * @param request The validated summarization request DTO.
     * @return A summarization response DTO containing the summary or error information.
     */
    @Override
    public SummarizationResponseDTO summarize(@Valid SummarizationRequestDTO request) {
        logger.info("Starting summarization request for transcript ID: {}", request.getTranscriptId());

        SummarizationResponseDTO responseDTO = new SummarizationResponseDTO();
        responseDTO.setProviderName(getProviderName());

        // Prepare the input text
        String userMessage = request.getPrompt() + "\n" + request.getContext();
        Map<String, Object> params = request.getParameters() != null ? request.getParameters() : Map.of();
        int maxTokens = (int) params.getOrDefault("max_tokens", 512);
        double temperature = ((Number) params.getOrDefault("temperature", 0.7)).doubleValue();
        double topP = ((Number) params.getOrDefault("top_p", 0.8)).doubleValue();

        // Build JSON payload
        String jsonPayload = String.format(
                "{" +
                        "\"model\":\"%s\"," +
                        "\"max_tokens\":%d," +
                        "\"temperature\":%.1f," +
                        "\"top_p\":%.1f," +
                        "\"messages\":[{" +
                        "\"role\":\"system\",\"content\":\"You are a helpful assistant.\"}," +
                        "{" +
                        "\"role\":\"user\",\"content\":\"%s\"}" +
                        "]" +
                        "}",
                escapeJson(modelName),
                maxTokens,
                temperature,
                topP,
                escapeJson(userMessage)
        );
        logger.debug("vLLMProvider JSON Payload: {}", jsonPayload);

        // Send request to vLLM API
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost postRequest = new HttpPost(apiUrl);
            postRequest.setHeader("Content-Type", "application/json");
            postRequest.setEntity(new StringEntity(jsonPayload, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                logger.debug("vLLMProvider Raw JSON Response Body: {}", responseBody);

                // Extract and clean response text
                String rawText = extractGeneratedText(responseBody);
                String cleanedText = JSONCleaner.cleanGeneratedText(rawText);

                responseDTO.setSummary(cleanedText);
                logger.info("Successfully completed summarization request for transcript ID: {}", request.getTranscriptId());
            }
        } catch (IOException e) {
            responseDTO.setSummary("Error: " + e.getMessage());
            logger.error("Error during summarization request: {}", e.getMessage(), e);
        }

        return responseDTO;
    }

    /**
     * Returns the name of the summarization provider.
     *
     * @return The provider name.
     */
    @Override
    public String getProviderName() {
        return "vLLM";
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

    /**
     * Extracts the generated text from the vLLM API response.
     *
     * @param responseBody The raw JSON response from the API.
     * @return The extracted generated text or an error message if parsing fails.
     */
    private String extractGeneratedText(String responseBody) {
        try {
            Map<String, Object> responseMap = MAPPER.readValue(responseBody, Map.class);
            Map<String, Object> choice = ((List<Map<String, Object>>) responseMap.get("choices")).get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            logger.error("Error extracting generated text from response: {}", e.getMessage(), e);
            return "Error parsing response: " + e.getMessage();
        }
    }
}