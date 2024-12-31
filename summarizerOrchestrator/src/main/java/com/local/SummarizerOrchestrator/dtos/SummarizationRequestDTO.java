package com.local.SummarizerOrchestrator.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Represents a summarization request payload.
 *
 * Validation Rules:
 * - `@NotNull`: Ensures the field is not null.
 * - `@NotBlank`: Ensures the field is not blank (applies to strings).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SummarizationRequestDTO {

    /**
     * The ID of the transcript to summarize.
     * Must not be null.
     */
    @NotNull(message = "Transcript ID must not be null.")
    private Long transcriptId;

    /**
     * The prompt to guide the summarization.
     * Must not be blank.
     */
    @NotBlank(message = "Prompt must not be blank.")
    private String prompt;

    /**
     * Additional context information for the summarization.
     */
    private String context;

    /**
     * Optional parameters for customizing the summarization request.
     * Examples include temperature, max tokens, etc.
     */
    private Map<String, Object> parameters;
}
