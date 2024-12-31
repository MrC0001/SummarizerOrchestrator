package com.local.SummarizerOrchestrator.providers;

import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;

/**
 * Defines the contract for summarization providers.
 * Implementing classes must handle summarization requests and provide details about the provider.
 */
public interface SummarizationProvider {

    /**
     * Processes a summarization request and returns the generated summary.
     *
     * @param request The summarization request DTO containing input data.
     * @return A summarization response DTO containing the generated summary or error details.
     */
    SummarizationResponseDTO summarize(SummarizationRequestDTO request);

    /**
     * Returns the name of the summarization provider.
     *
     * @return A string representing the provider name.
     */
    String getProviderName();
}
