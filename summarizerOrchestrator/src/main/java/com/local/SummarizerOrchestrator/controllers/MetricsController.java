package com.local.SummarizerOrchestrator.controllers;

import com.local.SummarizerOrchestrator.dtos.MetricsBatchResponseDTO;
import com.local.SummarizerOrchestrator.dtos.MetricsRequestDTO;
import com.local.SummarizerOrchestrator.models.ReferenceSummary;
import com.local.SummarizerOrchestrator.services.MetricsService;
import com.local.SummarizerOrchestrator.services.ReferenceSummaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * REST controller for managing metrics calculations.
 * Provides endpoints for calculating and retrieving metrics for summaries.
 */
@RestController
@RequestMapping("/api/metrics")
@Validated
public class MetricsController {

    private static final Logger logger = LoggerFactory.getLogger(MetricsController.class);
    private final MetricsService metricsService;
    private final ReferenceSummaryService referenceSummaryService;

    /**
     * Constructs the MetricsController.
     *
     * @param metricsService          The service to handle metrics-related operations.
     * @param referenceSummaryService The service to manage reference summaries.
     */
    public MetricsController(MetricsService metricsService, ReferenceSummaryService referenceSummaryService) {
        this.metricsService = metricsService;
        this.referenceSummaryService = referenceSummaryService;
    }

    /**
     * Calculates metrics for all summaries linked to a specific transcript.
     *
     * @param request The metrics calculation request containing the transcript ID and control summary.
     * @return A ResponseEntity containing the batch metrics response or an error message.
     */
    @PostMapping
    public ResponseEntity<MetricsBatchResponseDTO> calculateMetrics(@Valid @RequestBody MetricsRequestDTO request) {
        logger.info("Received metrics calculation request for transcript ID: {}", request.getTranscriptId());

        try {
            MetricsBatchResponseDTO response = metricsService.calculateMetricsForTranscript(request);
            logger.info("Metrics calculation completed for transcript ID: {}", request.getTranscriptId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Failed to calculate metrics for transcript ID: {}", request.getTranscriptId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Returning null to match MetricsBatchResponseDTO
        }
    }

    /**
     * Retrieves the control summary for a specific transcript.
     *
     * @param transcriptId The ID of the transcript.
     * @return A ResponseEntity containing the control summary or an error message if not found.
     */
    @GetMapping("/control-summary/{transcriptId}")
    public ResponseEntity<?> getControlSummary(@PathVariable Long transcriptId) {
        logger.info("Fetching control summary for transcript ID: {}", transcriptId);

        try {
            ReferenceSummary referenceSummary = referenceSummaryService.getReferenceSummary(transcriptId);
            return ResponseEntity.ok(Map.of("summaryText", referenceSummary.getSummaryText()));
        } catch (RuntimeException e) {
            logger.error("Control summary not found for transcript ID: {}", transcriptId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Control summary not found for transcript ID: " + transcriptId);
        }
    }
}
