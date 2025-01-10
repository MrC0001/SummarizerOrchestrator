package com.local.SummarizerOrchestrator.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a reference summary associated with a transcript.
 *
 * <p>A reference summary provides a ground truth or baseline summary for
 * comparison and evaluation against generated summaries.</p>
 */
@Entity
@Table(name = "reference_summaries")
@Getter
@Setter
@NoArgsConstructor
public class ReferenceSummary {

    /**
     * The unique identifier for the reference summary.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The associated transcript for which this reference summary was created.
     *
     * <p><strong>Validation:</strong> Must not be null.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transcript_id", nullable = false)
    @NotNull(message = "Transcript must not be null.")
    private Transcript transcript;

    /**
     * The text content of the reference summary.
     *
     * <p><strong>Validation:</strong> Must not be blank.</p>
     */
    @NotBlank
    @Column(name = "summary_text", nullable = false, columnDefinition = "TEXT")
    private String summaryText;
}
