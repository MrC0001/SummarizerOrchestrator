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
 * Represents a summarization request payload.
 *
 * Validation Rules:
 * - `@NotNull`: Ensures the field is not null.
 * - `@NotBlank`: Ensures the field is not blank (applies to strings).
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SummarizationRequestDTO {

    @NotNull(message = "Transcript ID must not be null.")
    private Long transcriptId;

    private String provider;

    private String model;

    private String prompt;

    private String context;

    private List<Map<String, String>> messages;

    private Map<String, Object> parameters;

    private boolean stream;


}
