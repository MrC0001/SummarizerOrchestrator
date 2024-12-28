package com.local.SummarizerOrchestrator.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SummaryDTO {
    private Long id;
    private String providerName;
    private String summaryText;
}
