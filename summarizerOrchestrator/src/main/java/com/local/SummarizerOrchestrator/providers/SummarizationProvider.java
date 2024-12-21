package com.local.SummarizerOrchestrator.providers;

import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;

public interface SummarizationProvider {
    SummarizationResponseDTO summarize(SummarizationRequestDTO request);
    String getProviderName();
}
