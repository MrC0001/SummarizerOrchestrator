package com.local.SummarizerOrchestrator.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a metrics calculation request for all summaries linked to a transcript.
 */
@Getter
@Setter
@NoArgsConstructor
public class MetricsRequestDTO {

    /**
     * The unique identifier for the transcript.
     * Must not be null.
     */
    @NotNull(message = "Transcript ID must not be null.")
    private Long transcriptId;

    /**
     * The control summary (reference) to evaluate against.
     * Must not be blank.
     */
    @NotBlank(message = "Control summary must not be blank.")
    private String controlSummary;
}
