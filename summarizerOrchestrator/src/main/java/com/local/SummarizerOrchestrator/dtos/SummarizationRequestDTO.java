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
    private String prompt; // Instruction for summarization
    private String context; // Text or transcript to summarize
    private Map<String, Object> parameters; // Optional parameters like max_tokens, temperature
}
