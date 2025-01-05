package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.models.ReferenceSummary;

public interface ReferenceSummaryService {
    /**
     * Retrieves the reference summary for a given transcript ID.
     *
     * @param transcriptId The ID of the transcript.
     * @return The associated reference summary.
     */
    ReferenceSummary getReferenceSummary(Long transcriptId);
}
