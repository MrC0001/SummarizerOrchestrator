package com.local.SummarizerOrchestrator.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import com.local.SummarizerOrchestrator.utils.JSONCleaner;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Provides integration with Hugging Face's API for summarization tasks.
 */
@Component
public class HuggingFaceProvider implements SummarizationProvider {

    private static final Logger logger = LoggerFactory.getLogger(HuggingFaceProvider.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${huggingface.api.url}")
    @NotBlank(message = "HuggingFace API URL must not be blank.")
    private String apiUrl;

    @Value("${huggingface.api.token}")
    @NotBlank(message = "HuggingFace API Token must not be blank.")
    private String apiToken;

    /**
     * Sends a summarization request to Hugging Face's API.
     *
     * @param request The validated summarization request DTO.
     * @return A summarization response DTO containing the summary or error information.
     */
    @Override
    public SummarizationResponseDTO summarize(@Valid SummarizationRequestDTO request) {
        logger.info("Starting summarization request for transcript ID: {}", request.getTranscriptId());

        SummarizationResponseDTO responseDTO = new SummarizationResponseDTO();
        responseDTO.setProviderName(getProviderName());

        try {
            // Ensure inputs are sanitized
            String sanitizedPrompt = JSONCleaner.removeControlCharacters(request.getPrompt());
            String sanitizedContext = JSONCleaner.removeControlCharacters(request.getContext());

            // Ensure parameters are serializable and sanitized
            Map<String, Object> params = request.getParameters() != null ? request.getParameters() : Map.of();
            String parametersJson = MAPPER.writeValueAsString(params);

            // Construct the payload
            String jsonPayload = String.format(
                    "{\"inputs\": \"%s\\n%s\", \"parameters\": %s}",
                    escapeJson(sanitizedPrompt),
                    escapeJson(sanitizedContext),
                    parametersJson
            );
            logger.debug("HuggingFaceProvider JSON Payload: {}", jsonPayload);

            // Create a new CloseableHttpClient for each request
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(30000) // Connection timeout in milliseconds
                    .setSocketTimeout(30000) // Read timeout in milliseconds
                    .build();

            try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                    .setDefaultRequestConfig(requestConfig)
                    .build()) {

                HttpPost postRequest = new HttpPost(apiUrl);
                postRequest.setHeader("Authorization", "Bearer " + apiToken);
                postRequest.setHeader("Content-Type", "application/json");

                postRequest.setEntity(new StringEntity(jsonPayload, StandardCharsets.UTF_8));

                try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                    logger.debug("HuggingFaceProvider response status code: {}", response.getStatusLine().getStatusCode());
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    logger.debug("HuggingFaceProvider Raw JSON Response Body: {}", responseBody);

                    String rawText = JSONCleaner.extractGeneratedText(responseBody);
                    String cleanedText = JSONCleaner.cleanGeneratedText(rawText);

                    responseDTO.setSummary(cleanedText);
                    logger.info("Successfully completed summarization request for transcript ID: {}", request.getTranscriptId());
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            logger.error("Error during summarization request: {}", e.getMessage(), e);
            responseDTO.setSummary("Error: " + e.getMessage());
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
        return "Hugging Face";
    }

    /**
     * Escapes JSON string values for safe inclusion in payloads.
     *
     * @param text The text to escape.
     * @return The escaped text.
     */
    private String escapeJson(String text) {
        return text
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
