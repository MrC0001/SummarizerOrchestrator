package com.local.SummarizerOrchestrator.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for representing metrics calculated for a single summary.
 *
 * <p>This class encapsulates all evaluation metrics used to assess the quality of a summary,
 * along with the associated summary and provider details.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class MetricsResponseDTO {

    /**
     * The unique identifier of the summary.
     */
    private Long summaryId;

    /**
     * The name of the summarization provider.
     */
    private String providerName;

    /**
     * ROUGE-1 score for the summary.
     */
    private Float rouge1;

    /**
     * ROUGE-2 score for the summary.
     */
    private Float rouge2;

    /**
     * ROUGE-L score for the summary.
     */
    private Float rougeL;

    /**
     * BERT precision score for the summary.
     */
    private Float bertPrecision;

    /**
     * BERT recall score for the summary.
     */
    private Float bertRecall;

    /**
     * BERT F1 score for the summary.
     */
    private Float bertF1;

    /**
     * BLEU score for the summary.
     */
    private Float bleu;

    /**
     * METEOR score for the summary.
     */
    private Float meteor;

    /**
     * Length ratio of the summary compared to the reference text.
     */
    private Float lengthRatio;

    /**
     * Redundancy score for the summary, indicating repetitive content.
     */
    private Float redundancy;
}
