package com.local.SummarizerOrchestrator.providers;

import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Provides integration with a custom deployed model on GKE.
 */
@Component
public class GKECustomModelProvider implements SummarizationProvider {

    private static final Logger logger = LoggerFactory.getLogger(GKECustomModelProvider.class);

    @Value("${custom.model.url}")
    @NotBlank(message = "Custom model URL must not be blank.")
    private String modelUrl;

    @Value("${custom.model.auth-token}")
    @NotBlank(message = "Custom model auth-token must not be blank.")
    private String authToken;

    private final RestTemplate restTemplate;

    /**
     * Default constructor initializing RestTemplate.
     */
    public GKECustomModelProvider() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Sends a summarization request to the custom GKE model.
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
            // Prepare request payload
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authToken); // Add authentication if required

            HttpEntity<SummarizationRequestDTO> httpEntity = new HttpEntity<>(request, headers);
            logger.debug("Sending request to GKE Custom Model: {}", modelUrl);

            // Call the model endpoint
            ResponseEntity<String> response = restTemplate.exchange(
                    modelUrl, HttpMethod.POST, httpEntity, String.class
            );

            logger.debug("Received response from GKE Custom Model: {}", response.getBody());

            // Set response in DTO
            responseDTO.setSummary(response.getBody());
            logger.info("Successfully completed summarization request for transcript ID: {}", request.getTranscriptId());
        } catch (Exception e) {
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
        return "GKE/Custom Deployed Model";
    }
}
