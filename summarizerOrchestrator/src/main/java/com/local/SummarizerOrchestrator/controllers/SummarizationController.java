package com.local.SummarizerOrchestrator.controllers;

import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import com.local.SummarizerOrchestrator.models.Metrics;
import com.local.SummarizerOrchestrator.models.Summary;
import com.local.SummarizerOrchestrator.models.Transcript;
import com.local.SummarizerOrchestrator.repos.SummaryRepo;
import com.local.SummarizerOrchestrator.repos.TranscriptRepo;
import com.local.SummarizerOrchestrator.repos.MetricsRepo;
import com.local.SummarizerOrchestrator.services.SummarizationService;
import com.local.SummarizerOrchestrator.utils.JSONCleaner;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Validated
public class SummarizationController {

    private static final Logger logger = LoggerFactory.getLogger(SummarizationController.class);

    private final SummarizationService summarizationService;
    private final TranscriptRepo transcriptRepo;
    private final SummaryRepo summaryRepo;
    private final MetricsRepo metricsRepo;

    public SummarizationController(SummarizationService summarizationService, TranscriptRepo transcriptRepo, SummaryRepo summaryRepo, MetricsRepo metricsRepo) {
        this.summarizationService = summarizationService;
        this.transcriptRepo = transcriptRepo;
        this.summaryRepo = summaryRepo;
        this.metricsRepo = metricsRepo;
    }

    /**
     * Handles summarization requests for all providers dynamically.
     * Ensures new summaries are saved correctly and integrates with backend processing.
     */
    @PostMapping("/summarizeAll")
    public ResponseEntity<?> summarizeAll(@Valid @RequestBody SummarizationRequestDTO request) {
        logger.info("Received summarization request for transcript ID: {}", request.getTranscriptId());

        try {
            // Fetch transcript from the database
            Transcript transcript = transcriptRepo.findById(request.getTranscriptId())
                    .orElseThrow(() -> new RuntimeException("Transcript not found for ID: " + request.getTranscriptId()));

            // Sanitize transcript context
            String sanitizedContext = JSONCleaner.removeControlCharacters(transcript.getTranscript());

            // Prepare the summarization request
            SummarizationRequestDTO baseRequest = new SummarizationRequestDTO(
                    request.getTranscriptId(),
                    null,  // Provider dynamically assigned per payload
                    null,  // Model dynamically assigned per payload
                    "Summarize this text:",
                    sanitizedContext,
                    null,
                    Map.of(), // Dynamic parameters set in service logic
                    false
            );

            // Process summarization and save asynchronously
            CompletableFuture<Map<String, Object>> futureResult =
                    summarizationService.summarizeAndSaveAsync(baseRequest);

            // Wait for summarization results
            Map<String, Object> result = futureResult.join();

            // Return old and new summaries in the response
            logger.info("Summarization completed and saved for transcript ID: {}", request.getTranscriptId());
            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            logger.error("Transcript not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing summarization request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Error summarizing transcript",
                    "details", e.getMessage()
            ));
        }
    }


    /**
     * Retrieves summaries and their associated metrics for a given transcript.
     */
    @GetMapping("/{transcriptId}")
    public ResponseEntity<?> getSummariesAndMetricsWithTranscript(
            @PathVariable @NotNull(message = "Transcript ID must not be null.")
            @Min(value = 1, message = "Transcript ID must be greater than or equal to 1.") Long transcriptId) {
        logger.info("Fetching summaries and transcript for transcript ID: {}", transcriptId);

        try {
            // Fetch transcript
            Transcript transcript = transcriptRepo.findById(transcriptId)
                    .orElseThrow(() -> new RuntimeException("Transcript not found"));

            // Fetch summaries
            List<Summary> summaries = summaryRepo.findByTranscriptId(transcriptId);

            // Map summaries with metrics
            List<Map<String, Object>> summariesWithMetrics = summaries.stream()
                    .map(summary -> {
                        Map<String, Object> summaryData = new HashMap<>();
                        summaryData.put("providerName", summary.getProviderName());
                        summaryData.put("summary", summary.getSummaryText());

                        // Fetch metrics for each summary
                        Metrics metrics = metricsRepo.findBySummaryId(summary.getId()).orElse(null);
                        if (metrics != null) {
                            Map<String, Object> metricsMap = new HashMap<>();
                            metricsMap.put("rouge1", metrics.getRouge1());
                            metricsMap.put("rouge2", metrics.getRouge2());
                            metricsMap.put("rougeL", metrics.getRougeL());
                            metricsMap.put("bertPrecision", metrics.getBertPrecision());
                            metricsMap.put("bertRecall", metrics.getBertRecall());
                            metricsMap.put("bertF1", metrics.getBertF1());
                            metricsMap.put("bleu", metrics.getBleu());
                            metricsMap.put("meteor", metrics.getMeteor());
                            metricsMap.put("lengthRatio", metrics.getLengthRatio());
                            metricsMap.put("redundancy", metrics.getRedundancy());
                            metricsMap.put("createdAt", metrics.getCreatedAt());
                            summaryData.put("metrics", metricsMap);
                        } else {
                            summaryData.put("metrics", null);
                        }

                        return summaryData;
                    })
                    .collect(Collectors.toList());

            // Build response
            Map<String, Object> response = Map.of(
                    "transcript", transcript.getTranscript(),
                    "summaries", summariesWithMetrics
            );

            logger.info("Returning transcript, summaries, and metrics for transcript ID: {}", transcriptId);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("Transcript not found for ID {}: {}", transcriptId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching transcript and summaries for ID {}: {}", transcriptId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Overwrites old summaries with new ones for a specific transcript.
     */
    @PostMapping("/overwrite/{transcriptId}")
    public ResponseEntity<String> overwriteSummaries(
            @PathVariable @NotNull(message = "Transcript ID must not be null.") @Min(value = 1, message = "Transcript ID must be greater than or equal to 1.") Long transcriptId,
            @Valid @RequestBody List<SummarizationResponseDTO> newSummaries) {
        logger.info("Received overwrite request for transcript ID: {}", transcriptId);

        try {
            summarizationService.overwriteSummaries(transcriptId, newSummaries);
            logger.info("Summaries overwritten successfully for transcript ID: {}", transcriptId);
            return ResponseEntity.ok("Summaries overwritten successfully.");
        } catch (Exception e) {
            logger.error("Error overwriting summaries for transcript ID {}: {}", transcriptId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

}
