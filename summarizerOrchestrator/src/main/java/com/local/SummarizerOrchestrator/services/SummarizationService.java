package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing summarization operations.
 * Defines methods for handling summarization requests, responses, and storage.
 */
public interface SummarizationService {

    /**
     * Processes a summarization request, generates summaries using providers,
     * and saves the results to the database.
     *
     * @param request The summarization request DTO containing the input data.
     * @return A list of summarization response DTOs containing the generated summaries.
     */
    List<SummarizationResponseDTO> summarizeAndSave(SummarizationRequestDTO request);

    /**
     * Retrieves summaries for a specific transcript and formats the response.
     *
     * @param transcriptId The ID of the transcript whose summaries are being retrieved.
     * @return A ResponseEntity containing the summaries and transcript data.
     */
    ResponseEntity<?> getSummariesResponse(Long transcriptId);

    /**
     * Compares old and new summaries for a given transcript.
     *
     * @param request The summarization request DTO containing the input data for comparison.
     * @return A map containing old and new summaries for comparison.
     */
    Map<String, Object> compareSummaries(SummarizationRequestDTO request);

    /**
     * Overwrites existing summaries for a specific transcript with new summaries.
     *
     * @param transcriptId The ID of the transcript whose summaries are being overwritten.
     * @param newSummaries The new summaries to save.
     */
    void overwriteSummaries(Long transcriptId, List<SummarizationResponseDTO> newSummaries);

    /**
     * Checks if summaries exist for a specific transcript.
     *
     * @param transcriptId The ID of the transcript.
     * @return {@code true} if summaries exist, {@code false} otherwise.
     */
    boolean summariesExistForTranscript(Long transcriptId);
}
