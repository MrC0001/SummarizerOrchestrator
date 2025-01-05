package com.local.SummarizerOrchestrator.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reference_summaries")
@Getter
@Setter
@NoArgsConstructor
public class ReferenceSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transcript_id", nullable = false)
    @NotNull(message = "Transcript must not be null.")
    private Transcript transcript;

    @NotBlank
    @Column(name = "summary_text", nullable = false, columnDefinition = "TEXT")
    private String summaryText;
}
