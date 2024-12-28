package com.local.SummarizerOrchestrator.controllers;

import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import com.local.SummarizerOrchestrator.models.Summary;
import com.local.SummarizerOrchestrator.models.Transcript;
import com.local.SummarizerOrchestrator.repos.SummaryRepo;
import com.local.SummarizerOrchestrator.repos.TranscriptRepo;
import com.local.SummarizerOrchestrator.services.SummarizationService;
import com.local.SummarizerOrchestrator.utils.JSONCleaner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SummarizationController {

    private final SummarizationService summarizationService;
    private final TranscriptRepo transcriptRepo;
    private final SummaryRepo summaryRepo;

    @Autowired
    public SummarizationController(SummarizationService summarizationService, TranscriptRepo transcriptRepo, SummaryRepo summaryRepo) {
        this.summarizationService = summarizationService;
        this.transcriptRepo = transcriptRepo;
        this.summaryRepo = summaryRepo;
    }


    /**
     * Handle summarization requests for both first-time and existing summaries.
     */
    @PostMapping("/summarize")
    public ResponseEntity<?> summarize(@RequestBody SummarizationRequestDTO request) {
        try {
            System.out.println("Request received: " + request); // Log request payload

            request = JSONCleaner.sanitizeRequest(request);

            if (summarizationService.summariesExistForTranscript(request.getTranscriptId())) {
                Map<String, Object> response = summarizationService.compareSummaries(request);
                System.out.println("Returning response with old and new summaries: " + response);
                return ResponseEntity.ok(response);
            } else {
                List<SummarizationResponseDTO> newSummaries = summarizationService.summarizeAndSave(request);
                Map<String, Object> response = Map.of("newSummaries", newSummaries, "oldSummaries", List.of());
                System.out.println("Returning response for first-time summarization: " + response);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            System.err.println("Error processing summarize request: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error summarizing transcript");
        }
    }


    /**
     * Retrieve summaries for a specific transcript.
     */
    @GetMapping("/{transcriptId}")
    public ResponseEntity<?> getSummariesWithTranscript(@PathVariable Long transcriptId) {
        try {
            System.out.println("Fetching summaries and transcript for transcript ID: " + transcriptId);

            // Fetch transcript
            Transcript transcript = transcriptRepo.findById(transcriptId)
                    .orElseThrow(() -> new RuntimeException("Transcript not found"));
            System.out.println("Transcript found: " + transcript.getTranscript());

            // Fetch summaries
            List<Summary> summaries = summaryRepo.findByTranscriptId(transcriptId);

            // Map summaries to DTOs
            List<SummarizationResponseDTO> summaryDTOs = summaries.stream()
                    .map(summary -> new SummarizationResponseDTO(summary.getProviderName(), summary.getSummaryText()))
                    .collect(Collectors.toList());

            // Prepare combined response
            Map<String, Object> response = Map.of(
                    "transcript", transcript.getTranscript(),
                    "summaries", summaryDTOs
            );

            System.out.println("Returning transcript and summaries: " + response);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.err.println("Error fetching data for transcript ID " + transcriptId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error fetching data: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error fetching transcript and summaries: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching transcript and summaries: " + e.getMessage());
        }
    }


    /**
     * Overwrite old summaries with new ones for a specific transcript.
     */
    @PostMapping("/overwrite/{transcriptId}")
    public ResponseEntity<String> overwriteSummaries(
            @PathVariable Long transcriptId,
            @RequestBody List<SummarizationResponseDTO> newSummaries) {
        try {
            System.out.println("Received overwrite request for transcript ID: " + transcriptId);

            newSummaries.forEach(summary ->
                    System.out.println("New summary to overwrite: Provider - " + summary.getProviderName() + ", Summary - " + summary.getSummary())
            );

            summarizationService.overwriteSummaries(transcriptId, newSummaries);

            System.out.println("Summaries overwritten successfully for transcript ID: " + transcriptId);
            return ResponseEntity.ok("Summaries overwritten successfully.");
        } catch (Exception e) {
            System.err.println("Error overwriting summaries for transcript ID " + transcriptId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error overwriting summaries: " + e.getMessage());
        }
    }




}
