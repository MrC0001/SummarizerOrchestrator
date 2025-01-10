package com.local.SummarizerOrchestrator.providers;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import com.local.SummarizerOrchestrator.utils.JSONCleaner;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Integration provider for Google Cloud's Vertex AI summarization tasks.
 *
 * <p>This class handles communication with Vertex AI by constructing summarization requests,
 * sending them, and processing the responses.</p>
 */
@Component
public class VertexAIProvider implements SummarizationProvider {

    private static final Logger logger = LoggerFactory.getLogger(VertexAIProvider.class);

    @Value("${spring.cloud.gcp.project-id}")
    @NotBlank(message = "GCP Project ID must not be blank.")
    private String projectId;

    @Value("${vertex.gemini.region}")
    @NotBlank(message = "Vertex AI Region must not be blank.")
    private String location;

    @Value("${vertex.model-name}")
    @NotBlank(message = "Vertex AI Model Name must not be blank.")
    private String modelName;

    /**
     * Processes a summarization request using Vertex AI and returns the response.
     *
     * @param request The validated summarization request DTO.
     * @return A {@link SummarizationResponseDTO} containing the summary or an error message.
     */
    @Override
    public SummarizationResponseDTO summarize(@Valid SummarizationRequestDTO request) {
        logger.info("Starting summarization request for transcript ID: {}", request.getTranscriptId());

        SummarizationResponseDTO responseDTO = new SummarizationResponseDTO();
        responseDTO.setProviderName(getProviderName());

        // Combine prompt and context
        String inputText = buildInputText(request.getPrompt(), request.getContext());
        logger.debug("VertexAIProvider Input Text: {}", inputText);

        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            // Initialize generative model
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);

            // Generate content
            GenerateContentResponse response = model.generateContent(inputText);
            String rawText = ResponseHandler.getText(response);
            logger.debug("VertexAIProvider Raw Text: {}", rawText);

            // Clean and process the generated text
            String cleanedText = JSONCleaner.cleanGeneratedText(rawText);
            logger.debug("VertexAIProvider Cleaned Text: {}", cleanedText);

            responseDTO.setSummary(cleanedText);
            logger.info("Successfully completed summarization request for transcript ID: {}", request.getTranscriptId());
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
        return "Vertex AI";
    }

    /**
     * Builds the input text for the Vertex AI request by combining the prompt and context.
     *
     * @param prompt  The prompt to guide summarization.
     * @param context The context or transcript to be summarized.
     * @return A concatenated string of prompt and context.
     */
    private String buildInputText(String prompt, String context) {
        return (prompt != null ? prompt : "") + "\n" + (context != null ? context : "");
    }
}
