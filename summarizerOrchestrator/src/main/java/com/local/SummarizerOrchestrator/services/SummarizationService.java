package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;

import java.util.List;

public interface SummarizationService {
    List<SummarizationResponseDTO> summarizeAcrossProviders(SummarizationRequestDTO request);
}
