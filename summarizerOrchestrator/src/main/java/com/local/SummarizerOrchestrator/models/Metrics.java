package com.local.SummarizerOrchestrator.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing evaluation metrics for a specific summary.
 *
 * <p>This class captures various metrics used to assess the quality of a summary,
 * including ROUGE, BERTScore, BLEU, METEOR, and others. Each metrics record is
 * linked to a specific summary and transcript.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "metrics")
public class Metrics {

    /**
     * The unique identifier for the metrics record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The associated transcript for which the metrics were calculated.
     */
    @ManyToOne
    @JoinColumn(name = "transcript_id", nullable = false)
    private Transcript transcript;

    /**
     * The associated summary for which the metrics were calculated.
     */
    @ManyToOne
    @JoinColumn(name = "summary_id", nullable = false)
    private Summary summary;

    /**
     * ROUGE-1 score for the summary.
     */
    @Column(name = "rouge1")
    private Float rouge1;

    /**
     * ROUGE-2 score for the summary.
     */
    @Column(name = "rouge2")
    private Float rouge2;

    /**
     * ROUGE-L score for the summary.
     */
    @Column(name = "rougeL")
    private Float rougeL;

    /**
     * BERT precision score for the summary.
     */
    @Column(name = "bert_precision")
    private Float bertPrecision;

    /**
     * BERT recall score for the summary.
     */
    @Column(name = "bert_recall")
    private Float bertRecall;

    /**
     * BERT F1 score for the summary.
     */
    @Column(name = "bert_f1")
    private Float bertF1;

    /**
     * BLEU score for the summary.
     */
    @Column(name = "bleu")
    private Float bleu;

    /**
     * METEOR score for the summary.
     */
    @Column(name = "meteor")
    private Float meteor;

    /**
     * The length ratio of the summary compared to the reference text.
     */
    @Column(name = "length_ratio")
    private Float lengthRatio;

    /**
     * Redundancy score, indicating the amount of repetitive content in the summary.
     */
    @Column(name = "redundancy")
    private Float redundancy;

    /**
     * The timestamp when the metrics record was created.
     * <p>Defaults to the current timestamp and is immutable.</p>
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
