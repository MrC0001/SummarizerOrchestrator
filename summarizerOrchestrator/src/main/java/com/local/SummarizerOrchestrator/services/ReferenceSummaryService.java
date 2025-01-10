package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.models.ReferenceSummary;

/**
 * Service interface for managing {@link ReferenceSummary} entities.
 *
 * <p>This service provides methods to handle operations related to reference summaries,
 * which serve as baseline summaries for evaluating generated summaries.</p>
 */
public interface ReferenceSummaryService {

    /**
     * Retrieves the reference summary associated with the specified transcript ID.
     *
     * <p>The reference summary is used as a baseline for comparison with generated summaries
     * during metrics calculations and evaluations.</p>
     *
     * @param transcriptId The ID of the transcript whose reference summary is to be retrieved.
     *                     Must not be {@code null}.
     * @return The {@link ReferenceSummary} entity associated with the given transcript ID.
     * @throws IllegalArgumentException If the {@code transcriptId} is {@code null} or invalid.
     * @throws RuntimeException If no reference summary is found for the specified transcript ID.
     */
    ReferenceSummary getReferenceSummary(Long transcriptId);
}
