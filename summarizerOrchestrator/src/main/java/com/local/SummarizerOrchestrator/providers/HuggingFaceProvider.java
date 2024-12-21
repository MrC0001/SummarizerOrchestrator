package com.local.SummarizerOrchestrator.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import com.local.SummarizerOrchestrator.utils.JSONCleaner;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.config.RequestConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class HuggingFaceProvider implements SummarizationProvider {

    @Value("${huggingface.api.url}")
    private String apiUrl;

    @Value("${huggingface.api.token}")
    private String apiToken;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public SummarizationResponseDTO summarize(SummarizationRequestDTO request) {
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

            System.out.println("HuggingFaceProvider JSON Payload:\n" + jsonPayload);

            // Create a new CloseableHttpClient for each request
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000) // Connection timeout in milliseconds
                    .setSocketTimeout(100000) // Read timeout in milliseconds
                    .build();

            try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                    .setDefaultRequestConfig(requestConfig)
                    .build()) {

                HttpPost postRequest = new HttpPost(apiUrl);
                postRequest.setHeader("Authorization", "Bearer " + apiToken);
                postRequest.setHeader("Content-Type", "application/json");

                postRequest.setEntity(new StringEntity(jsonPayload, StandardCharsets.UTF_8));

                try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    System.out.println("HuggingFaceProvider Raw JSON Response Body:\n" + responseBody);

                    String rawText = JSONCleaner.extractGeneratedText(responseBody);
                    String cleanedText = JSONCleaner.cleanGeneratedText(rawText);

                    responseDTO.setSummary(cleanedText);
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            responseDTO.setSummary("Error: " + e.getMessage());
            System.err.println("HuggingFaceProvider Error:\n" + e.getMessage());
        }

        return responseDTO;
    }

    @Override
    public String getProviderName() {
        return "Hugging Face";
    }

    private String escapeJson(String text) {
        return text
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
