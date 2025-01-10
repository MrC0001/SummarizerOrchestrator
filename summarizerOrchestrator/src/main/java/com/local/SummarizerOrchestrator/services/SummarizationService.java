package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for managing summarization operations.
 */
public interface SummarizationService {

    /**
     * Retrieves summaries for a given transcript.
     *
     * @param transcriptId The transcript ID.
     * @return A {@link ResponseEntity} with summaries or an error message.
     */
    ResponseEntity<?> getSummariesResponse(Long transcriptId);

    /**
     * Compares existing summaries with newly generated ones for a transcript.
     *
     * @param request The summarization request.
     * @return A map of old and new summaries.
     */
    Map<String, Object> compareSummaries(SummarizationRequestDTO request);

    /**
     * Overwrites summaries for a given transcript.
     *
     * @param transcriptId The transcript ID.
     * @param newSummaries The new summaries to save.
     */
    void overwriteSummaries(Long transcriptId, List<SummarizationResponseDTO> newSummaries);

    /**
     * Checks if summaries exist for a transcript.
     *
     * @param transcriptId The transcript ID.
     * @return {@code true} if summaries exist, otherwise {@code false}.
     */
    boolean summariesExistForTranscript(Long transcriptId);

    /**
     * Asynchronously processes a summarization request and saves valid results.
     *
     * @param request The summarization request.
     * @return A {@link CompletableFuture} with summaries and errors.
     */
    CompletableFuture<Map<String, Object>> summarizeAndSaveAsync(@Valid @NotNull SummarizationRequestDTO request);

    /**
     * Distributes a summarization request across providers and aggregates results.
     *
     * @param request The summarization request.
     * @return A list of {@link SummarizationResponseDTO} with provider-specific results.
     */
    List<SummarizationResponseDTO> summarizeAcrossProviders(SummarizationRequestDTO request);
}
