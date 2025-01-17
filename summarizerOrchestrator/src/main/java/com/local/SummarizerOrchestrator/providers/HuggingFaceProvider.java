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
 * Integration provider for Hugging Face API summarization tasks.
 *
 * <p>This class handles communication with the Hugging Face API by constructing
 * summarization requests, sending them, and processing the results.</p>
 */
@Component
public class HuggingFaceProvider implements SummarizationProvider {

    private static final Logger logger = LoggerFactory.getLogger(HuggingFaceProvider.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${huggingface.api.url}")
    @NotBlank(message = "Hugging Face API URL must not be blank.")
    private String apiUrl;

    @Value("${huggingface.api.token}")
    @NotBlank(message = "Hugging Face API Token must not be blank.")
    private String apiToken;

    /**
     * Sends a summarization request to Hugging Face's API and processes the response.
     *
     * @param request The validated summarization request DTO.
     * @return A {@link SummarizationResponseDTO} containing the summary or an error message.
     */
    @Override
    public SummarizationResponseDTO summarize(@Valid SummarizationRequestDTO request) {
        logger.info("Starting summarization request for transcript ID: {}", request.getTranscriptId());

        SummarizationResponseDTO responseDTO = new SummarizationResponseDTO();
        responseDTO.setProviderName(getProviderName());

        try {
            // Sanitize inputs
            String sanitizedPrompt = JSONCleaner.removeControlCharacters(request.getPrompt());
            String sanitizedContext = JSONCleaner.removeControlCharacters(request.getContext());

            // Construct payload
            String jsonPayload = buildJsonPayload(sanitizedPrompt, sanitizedContext, request.getParameters());
            logger.debug("HuggingFaceProvider JSON Payload: {}", jsonPayload);

            // Configure HTTP client
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(30000)  // Connection timeout in milliseconds
                    .setSocketTimeout(300000)  // Read timeout in milliseconds
                    .build();

            try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                    .setDefaultRequestConfig(requestConfig)
                    .build()) {

                HttpPost postRequest = buildHttpPostRequest(jsonPayload);

                try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                    logger.debug("HuggingFaceProvider response status code: {}", response.getStatusLine().getStatusCode());
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    logger.debug("HuggingFaceProvider Raw JSON Response Body: {}", responseBody);

                    // Process and clean response
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
     * Builds the JSON payload for the Hugging Face API request.
     *
     * @param prompt      The sanitized prompt to guide summarization.
     * @param context     The sanitized transcript context for summarization.
     * @param parameters  Additional parameters for the request.
     * @return The constructed JSON payload as a string.
     */
    private String buildJsonPayload(String prompt, String context, Map<String, Object> parameters) throws IOException {
        Map<String, Object> params = parameters != null ? parameters : Map.of();
        String parametersJson = MAPPER.writeValueAsString(params);

        return String.format(
                "{\"inputs\": \"%s\\n%s\", \"parameters\": %s}",
                escapeJson(prompt),
                escapeJson(context),
                parametersJson
        );
    }

    /**
     * Constructs the HTTP POST request for the Hugging Face API.
     *
     * @param jsonPayload The JSON payload to send in the request body.
     * @return A configured {@link HttpPost} object.
     */
    private HttpPost buildHttpPostRequest(String jsonPayload) {
        HttpPost postRequest = new HttpPost(apiUrl);
        postRequest.setHeader("Authorization", "Bearer " + apiToken);
        postRequest.setHeader("Content-Type", "application/json");
        postRequest.setEntity(new StringEntity(jsonPayload, StandardCharsets.UTF_8));
        return postRequest;
    }

    /**
     * Escapes special characters in a string for safe inclusion in JSON payloads.
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
