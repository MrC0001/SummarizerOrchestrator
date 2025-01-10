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
 * Integration provider for Mistral AI summarization tasks.
 *
 * <p>This class handles interaction with Mistral AI by initializing the chat model,
 * sending summarization requests, and processing the responses.</p>
 */
@Component
public class MistralAIProvider implements SummarizationProvider {

    private static final Logger logger = LoggerFactory.getLogger(MistralAIProvider.class);

    @Value("${mistral.api.key}")
    private String apiKey;

    @Value("${mistral.model.name}")
    private String modelName;

    /**
     * Processes a summarization request using Mistral AI and returns the response.
     *
     * @param request The summarization request containing input text and parameters.
     * @return A {@link SummarizationResponseDTO} containing the summary or an error message.
     */
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
            String inputText = buildInputText(request.getPrompt(), request.getContext());
            logger.debug("Mistral AI Input Text: {}", inputText);

            // Generate response
            String rawResponse = model.generate(inputText);

            // Set the processed response
            responseDTO.setSummary(rawResponse);
            logger.info("Successfully completed summarization with Mistral AI for transcript ID: {}", request.getTranscriptId());
        } catch (Exception e) {
            logger.error("Error during Mistral AI summarization: {}", e.getMessage(), e);
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
        return "Mistral AI";
    }

    /**
     * Constructs the input text for Mistral AI from the provided prompt and context.
     *
     * @param prompt  The prompt to guide the summarization.
     * @param context The context or transcript to be summarized.
     * @return A concatenated string of prompt and context.
     */
    private String buildInputText(String prompt, String context) {
        return (prompt != null ? prompt : "") + "\n" + (context != null ? context : "");
    }
}
