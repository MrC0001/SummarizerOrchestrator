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

/**
 * REST controller for managing summarization operations.
 * Provides endpoints for summarizing text, retrieving summaries, and managing summary data.
 */
@RestController
@RequestMapping("/api")
@Validated
public class SummarizationController {

    private static final Logger logger = LoggerFactory.getLogger(SummarizationController.class);

    private final SummarizationService summarizationService;
    private final TranscriptRepo transcriptRepo;
    private final SummaryRepo summaryRepo;
    private final MetricsRepo metricsRepo;

    /**
     * Constructs the SummarizationController.
     *
     * @param summarizationService The service handling summarization logic.
     * @param transcriptRepo       Repository for managing transcript entities.
     * @param summaryRepo          Repository for managing summary entities.
     * @param metricsRepo          Repository for managing metrics entities.
     */
    public SummarizationController(SummarizationService summarizationService, TranscriptRepo transcriptRepo,
                                   SummaryRepo summaryRepo, MetricsRepo metricsRepo) {
        this.summarizationService = summarizationService;
        this.transcriptRepo = transcriptRepo;
        this.summaryRepo = summaryRepo;
        this.metricsRepo = metricsRepo;
    }

    /**
     * Processes a summarization request for all providers dynamically.
     *
     * @param request The summarization request containing the transcript ID and other parameters.
     * @return A ResponseEntity containing the summaries or an error message.
     */
    @PostMapping("/summarizeAll")
    public ResponseEntity<?> summarizeAll(@Valid @RequestBody SummarizationRequestDTO request) {
        logger.info("Received summarization request for transcript ID: {}", request.getTranscriptId());

        try {
            Transcript transcript = transcriptRepo.findById(request.getTranscriptId())
                    .orElseThrow(() -> new RuntimeException("Transcript not found for ID: " + request.getTranscriptId()));

            String sanitizedContext = JSONCleaner.removeControlCharacters(transcript.getTranscript());

            SummarizationRequestDTO baseRequest = new SummarizationRequestDTO(
                    request.getTranscriptId(),
                    null,  // Provider dynamically assigned
                    null,  // Model dynamically assigned
                    "Summarize this text:",
                    sanitizedContext,
                    null,
                    Map.of(),  // Dynamic parameters
                    false
            );

            CompletableFuture<Map<String, Object>> futureResult = summarizationService.summarizeAndSaveAsync(baseRequest);
            Map<String, Object> result = futureResult.join();

            logger.info("Summarization completed for transcript ID: {}", request.getTranscriptId());
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
     * Retrieves summaries and associated metrics for a specific transcript.
     *
     * @param transcriptId The ID of the transcript.
     * @return A ResponseEntity containing the transcript text, summaries, and metrics.
     */
    @GetMapping("/{transcriptId}")
    public ResponseEntity<?> getSummariesAndMetricsWithTranscript(
            @PathVariable @NotNull(message = "Transcript ID must not be null.")
            @Min(value = 1, message = "Transcript ID must be greater than or equal to 1.") Long transcriptId) {
        logger.info("Fetching summaries and metrics for transcript ID: {}", transcriptId);

        try {
            Transcript transcript = transcriptRepo.findById(transcriptId)
                    .orElseThrow(() -> new RuntimeException("Transcript not found"));

            List<Summary> summaries = summaryRepo.findByTranscriptId(transcriptId);

            List<Map<String, Object>> summariesWithMetrics = summaries.stream().map(summary -> {
                Map<String, Object> summaryData = new HashMap<>();
                summaryData.put("providerName", summary.getProviderName());
                summaryData.put("summary", summary.getSummaryText());

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
            }).collect(Collectors.toList());

            Map<String, Object> response = Map.of(
                    "transcript", transcript.getTranscript(),
                    "summaries", summariesWithMetrics
            );

            logger.info("Returning summaries and metrics for transcript ID: {}", transcriptId);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("Transcript not found for ID {}: {}", transcriptId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching summaries for ID {}: {}", transcriptId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Overwrites existing summaries for a specific transcript.
     *
     * @param transcriptId The ID of the transcript.
     * @param newSummaries The new summaries to save.
     * @return A ResponseEntity indicating the success or failure of the operation.
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
