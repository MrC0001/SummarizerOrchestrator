package com.local.SummarizerOrchestrator.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptWithSummariesDTO {
    private Long id;
    private String scenario;
    private String transcript;
    private List<SummaryDTO> summaries;
}
