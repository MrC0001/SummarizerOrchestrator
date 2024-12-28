package com.local.SummarizerOrchestrator.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SummarizationRequestDTO {
    private Long transcriptId;
    private String prompt;
    private String context;
    private Map<String, Object> parameters;
}
