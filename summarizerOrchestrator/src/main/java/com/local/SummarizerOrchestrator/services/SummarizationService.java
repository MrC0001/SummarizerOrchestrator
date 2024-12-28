package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface SummarizationService {

    List<SummarizationResponseDTO> summarizeAndSave(SummarizationRequestDTO request);

    ResponseEntity<?> getSummariesResponse(Long transcriptId);

    Map<String, Object> compareSummaries(SummarizationRequestDTO request);

    void overwriteSummaries(Long transcriptId, List<SummarizationResponseDTO> newSummaries);

    boolean summariesExistForTranscript(Long transcriptId);
}
