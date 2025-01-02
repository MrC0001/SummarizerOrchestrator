package com.local.SummarizerOrchestrator.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the metrics calculated for a single summary.
 */
@Getter
@Setter
@NoArgsConstructor
public class MetricsResponseDTO {

    private Long summaryId;
    private String providerName;

    private Float rouge1;
    private Float rouge2;
    private Float rougeL;
    private Float bertPrecision;
    private Float bertRecall;
    private Float bertF1;
    private Float bleu;
    private Float meteor;
    private Float lengthRatio;
    private Float redundancy;
}
