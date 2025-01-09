package com.local.SummarizerOrchestrator.providers;

import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModelName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Provides integration with Mistral AI for summarization tasks.
 */
@Component
public class MistralAIProvider implements SummarizationProvider {

    private static final Logger logger = LoggerFactory.getLogger(MistralAIProvider.class);

    @Value("${mistral.api.key}")
    private String apiKey;

    @Value("${mistral.model.name}")
    private String modelName;

    @Override
    public SummarizationResponseDTO summarize(SummarizationRequestDTO request) {
        logger.info("Starting summarization with Mistral AI for transcript ID: {}", request.getTranscriptId());

        SummarizationResponseDTO responseDTO = new SummarizationResponseDTO();
        responseDTO.setProviderName(getProviderName());

        try {
            // Initialize Mistral AI Chat Model
            ChatLanguageModel model = MistralAiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(MistralAiChatModelName.valueOf(modelName))
                    .build();

            // Construct input text
            String inputText = request.getPrompt() + "\n" + request.getContext();
            logger.debug("Mistral AI Input Text: {}", inputText);

            // Generate response
            String rawResponse = model.generate(inputText);

            // Process and set the response
            responseDTO.setSummary(rawResponse);
            logger.info("Successfully completed summarization with Mistral AI for transcript ID: {}", request.getTranscriptId());
        } catch (Exception e) {
            logger.error("Error during Mistral AI summarization: {}", e.getMessage(), e);
            responseDTO.setSummary("Error: " + e.getMessage());
        }

        return responseDTO;
    }

    @Override
    public String getProviderName() {
        return "Mistral AI";
    }
}
