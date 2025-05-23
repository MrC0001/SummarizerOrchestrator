package com.local.SummarizerOrchestrator.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the response of a summarization operation.
 *
 * Validation Rules:
 * - `@NotBlank`: Ensures fields are not blank (applies to strings).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SummarizationResponseDTO {

    /**
     * The name of the summarization provider.
     * Must not be blank.
     */
    @NotBlank(message = "Provider name must not be blank.")
    private String providerName;

    /**
     * The summary text generated by the provider.
     * Must not be blank.
     */
    @NotBlank(message = "Summary text must not be blank.")
    private String summary;
}
