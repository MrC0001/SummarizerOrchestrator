package com.local.SummarizerOrchestrator.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object (DTO) representing a summarization request payload.
 *
 * <p>This class encapsulates all the necessary parameters for a summarization request,
 * including transcript ID, provider, model, prompt, and additional options.</p>
 *
 * <p><strong>Validation Rules:</strong>
 * <ul>
 *     <li>{@code @NotNull}: Ensures fields are not null.</li>
 *     <li>{@code @NotBlank}: Ensures string fields are not blank (if applicable).</li>
 * </ul>
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SummarizationRequestDTO {

    /**
     * The unique identifier of the transcript to be summarized.
     * <p><strong>Validation:</strong> Must not be null.</p>
     */
    @NotNull(message = "Transcript ID must not be null.")
    private Long transcriptId;

    /**
     * The name of the summarization provider to use.
     */
    private String provider;

    /**
     * The model to be used for summarization.
     */
    private String model;

    /**
     * The prompt to guide the summarization process.
     */
    private String prompt;

    /**
     * The context or content of the transcript to be summarized.
     */
    private String context;

    /**
     * A list of additional messages, typically used for interactive or multi-turn summarization.
     */
    private List<Map<String, String>> messages;

    /**
     * A map of additional parameters to customize the summarization process.
     */
    private Map<String, Object> parameters;

    /**
     * Indicates whether the response should be streamed.
     */
    private boolean stream;
}
