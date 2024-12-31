package com.local.SummarizerOrchestrator.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents a transcript entity.
 *
 * Validation Rules:
 * - `@NotBlank`: Ensures string fields are not blank.
 * - `@Size`: Limits the length of string fields.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transcripts")
public class Transcript extends BaseEntity {

    /**
     * The scenario or context of the transcript.
     * Must not be blank.
     */
    @NotBlank(message = "Scenario must not be blank.")
    @Size(max = 255, message = "Scenario must not exceed 255 characters.")
    private String scenario;

    /**
     * The full text of the transcript.
     * Must not be blank.
     */
    @Lob
    @Column(name = "transcript", length = 10_000, nullable = false)
    @NotBlank(message = "Transcript must not be blank.")
    private String transcript;

    /**
     * The list of summaries associated with this transcript.
     * Managed with cascade operations and orphan removal.
     */
    @OneToMany(mappedBy = "transcript", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Prevents circular references in bidirectional relationships
    private List<Summary> summaries;
}
