package com.local.SummarizerOrchestrator.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents a transcript along with its associated summaries.
 *
 * Validation Rules:
 * - `@NotNull`: Ensures fields are not null.
 * - `@NotBlank`: Ensures fields are not blank (applies to strings).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptWithSummariesDTO {

    /**
     * The unique identifier for the transcript.
     * Must not be null when updating or retrieving an existing transcript.
     */
    @NotNull(message = "Transcript ID must not be null.")
    private Long id;

    /**
     * The scenario or context of the transcript.
     * Must not be blank.
     */
    @NotBlank(message = "Scenario must not be blank.")
    private String scenario;

    /**
     * The transcript text.
     * Must not be blank.
     */
    @NotBlank(message = "Transcript must not be blank.")
    private String transcript;

    /**
     * The list of summaries associated with the transcript.
     * Must not be null but can be an empty list if there are no summaries.
     */
    @NotNull(message = "Summaries list must not be null.")
    private List<SummaryDTO> summaries;
}
