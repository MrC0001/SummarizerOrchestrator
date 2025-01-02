package com.local.SummarizerOrchestrator.controllers;

import com.local.SummarizerOrchestrator.dtos.MetricsBatchResponseDTO;
import com.local.SummarizerOrchestrator.dtos.MetricsRequestDTO;
import com.local.SummarizerOrchestrator.services.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST controller for managing metrics calculations.
 * Includes endpoints for calculating and retrieving metrics for summaries.
 */
@RestController
@RequestMapping("/api/metrics")
@Validated
public class MetricsController {

    private static final Logger logger = LoggerFactory.getLogger(MetricsController.class);
    private final MetricsService metricsService;

    /**
     * Constructor for MetricsController.
     *
     * @param metricsService The service to handle metrics-related operations.
     */
    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Calculate metrics for all summaries linked to a specific transcript.
     *
     * @param request The metrics calculation request containing the transcript ID and control summary.
     * @return A ResponseEntity containing the batch metrics response.
     */
    @PostMapping
    public ResponseEntity<MetricsBatchResponseDTO> calculateMetrics(@Valid @RequestBody MetricsRequestDTO request) {
        logger.info("Received metrics calculation request for transcript ID: {}", request.getTranscriptId());

        MetricsBatchResponseDTO response = metricsService.calculateMetricsForTranscript(request);
        logger.info("Metrics calculation completed for transcript ID: {}", request.getTranscriptId());

        return ResponseEntity.ok(response);
    }
}
