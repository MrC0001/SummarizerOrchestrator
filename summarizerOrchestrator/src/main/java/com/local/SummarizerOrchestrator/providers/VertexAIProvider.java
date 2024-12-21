package com.local.SummarizerOrchestrator.providers;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import com.local.SummarizerOrchestrator.utils.JSONCleaner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class VertexAIProvider implements SummarizationProvider {

    @Value("${spring.cloud.gcp.project-id}")
    private String projectId;

    @Value("${vertex.region}")
    private String location;

    @Value("${vertex.model-name}")
    private String modelName;

    @Override
    public SummarizationResponseDTO summarize(SummarizationRequestDTO request) {
        SummarizationResponseDTO responseDTO = new SummarizationResponseDTO();
        responseDTO.setProviderName(getProviderName());

        // Assume sanitized input
        String inputText = request.getPrompt() + "\n" + request.getContext();

        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);

            GenerateContentResponse response = model.generateContent(inputText);
            String rawText = ResponseHandler.getText(response);

            System.out.println("VertexAIProvider Raw Text:\n" + rawText);

            String cleanedText = JSONCleaner.cleanGeneratedText(rawText);
            System.out.println("VertexAIProvider Cleaned Text:\n" + cleanedText);

            responseDTO.setSummary(cleanedText);
        } catch (IOException e) {
            responseDTO.setSummary("Error: " + e.getMessage());
        }

        return responseDTO;
    }

    @Override
    public String getProviderName() {
        return "Vertex AI";
    }
}
