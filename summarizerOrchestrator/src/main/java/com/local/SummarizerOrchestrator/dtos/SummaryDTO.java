package com.local.SummarizerOrchestrator.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a summary data transfer object.
 *
 * Validation Rules:
 * - `@NotNull`: Ensures fields are not null.
 * - `@NotBlank`: Ensures fields are not blank (applies to strings).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SummaryDTO {

    /**
     * The unique identifier for the summary.
     * Must not be null.
     */
    @NotNull(message = "ID must not be null.")
    private Long id;

    /**
     * The name of the summarization provider.
     * Must not be blank.
     */
    @NotBlank(message = "Provider name must not be blank.")
    private String providerName;

    /**
     * The summary text.
     * Must not be blank.
     */
    @NotBlank(message = "Summary text must not be blank.")
    private String summaryText;
}
