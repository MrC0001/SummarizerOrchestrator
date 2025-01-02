package com.local.SummarizerOrchestrator.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents the batch metrics response for all summaries linked to a transcript.
 */
@Getter
@Setter
@NoArgsConstructor
public class MetricsBatchResponseDTO {

    /**
     * The unique identifier for the transcript.
     */
    private Long transcriptId;

    /**
     * The scenario or context of the transcript.
     */
    private String scenario;

    /**
     * The metrics for each summary associated with the transcript.
     */
    private List<MetricsResponseDTO> summaryMetrics;
}
