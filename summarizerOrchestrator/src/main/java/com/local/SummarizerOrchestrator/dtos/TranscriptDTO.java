package com.local.SummarizerOrchestrator.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a transcript data transfer object.
 *
 * Validation Rules:
 * - `@NotNull`: Ensures fields are not null.
 * - `@NotBlank`: Ensures fields are not blank (applies to strings).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptDTO {

    /**
     * The unique identifier for the transcript.
     * Must not be null when updating an existing transcript.
     */
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
}
