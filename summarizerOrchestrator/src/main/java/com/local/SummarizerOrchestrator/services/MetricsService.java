package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.dtos.MetricsRequestDTO;
import com.local.SummarizerOrchestrator.dtos.MetricsBatchResponseDTO;

public interface MetricsService {

    /**
     * Calculates metrics for all summaries linked to a transcript and stores them.
     *
     * @param request The metrics request containing the transcript ID and control summary.
     * @return A batch response containing metrics for all summaries linked to the transcript.
     */
    MetricsBatchResponseDTO calculateMetricsForTranscript(MetricsRequestDTO request);
}
