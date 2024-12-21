package com.local.SummarizerOrchestrator.controllers;

import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import com.local.SummarizerOrchestrator.services.SummarizationService;
import com.local.SummarizerOrchestrator.utils.JSONCleaner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SummarizationController {

    private final SummarizationService summarizationService;

    @Autowired
    public SummarizationController(SummarizationService summarizationService) {
        this.summarizationService = summarizationService;
    }

    @PostMapping("/summarize")
    public ResponseEntity<List<SummarizationResponseDTO>> summarize(@RequestBody SummarizationRequestDTO request) {
        // Centralized input sanitization
        request = JSONCleaner.sanitizeRequest(request);

        List<SummarizationResponseDTO> responses = summarizationService.summarizeAcrossProviders(request);

        return ResponseEntity.ok(responses);
    }

}
