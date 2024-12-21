package com.local.SummarizerOrchestrator.providers;

import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GKECustomModelProvider implements SummarizationProvider {

    @Value("${custom.model.url}")
    private String modelUrl;

    @Value("${custom.model.auth-token}")
    private String authToken;

    private final RestTemplate restTemplate;

    public GKECustomModelProvider() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public SummarizationResponseDTO summarize(SummarizationRequestDTO request) {
        SummarizationResponseDTO responseDTO = new SummarizationResponseDTO();
        responseDTO.setProviderName(getProviderName());

        try {
            // Prepare request payload
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authToken); // Add authentication if required

            HttpEntity<SummarizationRequestDTO> httpEntity = new HttpEntity<>(request, headers);

            // Call the model endpoint
            ResponseEntity<String> response = restTemplate.exchange(
                    modelUrl, HttpMethod.POST, httpEntity, String.class
            );

            // Set response in DTO
            responseDTO.setSummary(response.getBody());
        } catch (Exception e) {
            responseDTO.setSummary("Error: " + e.getMessage());
        }

        return responseDTO;
    }

    @Override
    public String getProviderName() {
        return "GKE/Custom Deployed Model";
    }
}
