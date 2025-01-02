package com.local.SummarizerOrchestrator.controllers;

import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import com.local.SummarizerOrchestrator.models.Summary;
import com.local.SummarizerOrchestrator.models.Transcript;
import com.local.SummarizerOrchestrator.repos.SummaryRepo;
import com.local.SummarizerOrchestrator.repos.TranscriptRepo;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * REST controller for handling summarization-related operations.
 * Includes endpoints for generating, retrieving, and overwriting summaries.
 *
 * Validation Rules:
 * - `@NotNull`: Ensures parameters are not null.
 * - `@Min`: Ensures numerical values meet the specified minimum.
 * - `@Valid`: Enforces validation for request bodies containing DTOs.
 */
@RestController
@RequestMapping("/api")
@Validated // Enables validation for @PathVariable and @RequestParam
public class SummarizationController {

    private static final Logger logger = LoggerFactory.getLogger(SummarizationController.class);

    private final SummarizationService summarizationService;
    private final TranscriptRepo transcriptRepo;
    private final SummaryRepo summaryRepo;

    /**
     * Constructor for SummarizationController.
     *
     * @param summarizationService The service for summarization operations.
     * @param transcriptRepo       The repository for transcript data.
     * @param summaryRepo          The repository for summary data.
     */
    public SummarizationController(SummarizationService summarizationService, TranscriptRepo transcriptRepo, SummaryRepo summaryRepo) {
        this.summarizationService = summarizationService;
        this.transcriptRepo = transcriptRepo;
        this.summaryRepo = summaryRepo;
    }

    /**
     * Handles summarization requests for both first-time and existing summaries.
     *
     * <p>
     * For existing summaries:
     * <ul>
     *   <li>Fetches and compares old summaries with newly generated ones.</li>
     * </ul>
     * For new summarization requests:
     * <ul>
     *   <li>Processes the summarization asynchronously using multiple providers.</li>
     *   <li>Waits synchronously for the results to ensure a complete response.</li>
     *   <li>Filters and validates the summaries before returning them.</li>
     * </ul>
     * </p>
     *
     * <p>This method ensures:
     * <ul>
     *   <li>Graceful handling of provider failures by including only valid summaries.</li>
     *   <li>Logging and returning detailed error messages if the summarization process fails.</li>
     *   <li>Returns empty lists for missing summaries instead of null values.</li>
     * </ul>
     * </p>
     *
     * @param request The summarization request payload, including the transcript ID, prompt, context, and parameters.
     *                Must pass validation rules defined in {@link SummarizationRequestDTO}.
     * @return A ResponseEntity containing:
     *         <ul>
     *           <li><b>HTTP 200:</b> If the summarization succeeds, returns a JSON response with "newSummaries" and "oldSummaries".</li>
     *           <li><b>HTTP 500:</b> If an error occurs, returns a structured error message with details.</li>
     *         </ul>
     * @throws jakarta.validation.ConstraintViolationException If validation fails for the request body.
     */

    @PostMapping("/summarize")
    public ResponseEntity<?> summarize(@Valid @RequestBody SummarizationRequestDTO request) {
        logger.info("Received summarization request: {}", request);

        try {
            // Sanitize the request payload
            SummarizationRequestDTO sanitizedRequest = JSONCleaner.sanitizeRequest(request);

            if (summarizationService.summariesExistForTranscript(sanitizedRequest.getTranscriptId())) {
                // Handle case where summaries already exist
                Map<String, Object> response = summarizationService.compareSummaries(sanitizedRequest);
                logger.info("Returning response with old and new summaries for transcript ID: {}", sanitizedRequest.getTranscriptId());
                return ResponseEntity.ok(response);
            } else {
                // Process summarization and save asynchronously
                CompletableFuture<List<SummarizationResponseDTO>> futureSummaries = summarizationService.summarizeAndSaveAsync(sanitizedRequest);
                List<SummarizationResponseDTO> summaries = futureSummaries.join(); // Wait for results synchronously

                // Final validation for JSON response
                Map<String, Object> response = Map.of(
                        "newSummaries", summaries,
                        "oldSummaries", Collections.emptyList()
                );
                logger.info("Returning new summaries for transcript ID: {}", sanitizedRequest.getTranscriptId());
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            logger.error("Error processing summarization request for transcript ID {}: {}", request.getTranscriptId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Error summarizing transcript",
                    "details", e.getMessage()
            ));
        }
    }





    /**
     * Retrieve summaries for a specific transcript.
     *
     * @param transcriptId The ID of the transcript. Must be greater than or equal to 1 and not null.
     * @return A ResponseEntity containing the transcript and summaries or an error message.
     * @throws jakarta.validation.ConstraintViolationException if validation fails for the ID.
     */
    @GetMapping("/{transcriptId}")
    public ResponseEntity<?> getSummariesWithTranscript(@PathVariable @NotNull(message = "Transcript ID must not be null.") @Min(value = 1, message = "Transcript ID must be greater than or equal to 1.") Long transcriptId) {
        logger.info("Fetching summaries and transcript for transcript ID: {}", transcriptId);
        try {
            Transcript transcript = transcriptRepo.findById(transcriptId)
                    .orElseThrow(() -> new RuntimeException("Transcript not found"));
            logger.info("Transcript found for ID {}: {}", transcriptId, transcript.getTranscript());

            List<Summary> summaries = summaryRepo.findByTranscriptId(transcriptId);
            List<SummarizationResponseDTO> summaryDTOs = summaries.stream()
                    .map(summary -> new SummarizationResponseDTO(summary.getProviderName(), summary.getSummaryText()))
                    .collect(Collectors.toList());

            Map<String, Object> response = Map.of(
                    "transcript", transcript.getTranscript(),
                    "summaries", summaryDTOs
            );

            logger.info("Returning transcript and summaries for transcript ID: {}", transcriptId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Transcript not found for ID {}: {}", transcriptId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error fetching data: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching transcript and summaries for ID {}: {}", transcriptId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching transcript and summaries");
        }
    }

    /**
     * Overwrite old summaries with new ones for a specific transcript.
     *
     * @param transcriptId The ID of the transcript to overwrite summaries for. Must be greater than or equal to 1 and not null.
     * @param newSummaries The new summaries to save. Each summary must pass validation rules defined in SummarizationResponseDTO.
     * @return A ResponseEntity containing a success message or an error message.
     * @throws jakarta.validation.ConstraintViolationException if validation fails for the ID or request body.
     */
    @PostMapping("/overwrite/{transcriptId}")
    public ResponseEntity<String> overwriteSummaries(
            @PathVariable @NotNull(message = "Transcript ID must not be null.") @Min(value = 1, message = "Transcript ID must be greater than or equal to 1.") Long transcriptId,
            @Valid @RequestBody List<SummarizationResponseDTO> newSummaries) {
        logger.info("Received overwrite request for transcript ID: {}", transcriptId);
        try {
            newSummaries.forEach(summary ->
                    logger.debug("New summary to overwrite: Provider - {}, Summary - {}", summary.getProviderName(), summary.getSummary())
            );

            summarizationService.overwriteSummaries(transcriptId, newSummaries);
            logger.info("Summaries overwritten successfully for transcript ID: {}", transcriptId);
            return ResponseEntity.ok("Summaries overwritten successfully.");
        } catch (Exception e) {
            logger.error("Error overwriting summaries for transcript ID {}: {}", transcriptId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error overwriting summaries: " + e.getMessage());
        }
    }
}
