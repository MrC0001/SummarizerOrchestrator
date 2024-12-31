package com.local.SummarizerOrchestrator.controllers;

import com.local.SummarizerOrchestrator.dtos.TranscriptDTO;
import com.local.SummarizerOrchestrator.services.TranscriptService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing transcripts.
 * Provides endpoints to perform CRUD operations on transcripts.
 *
 * Validation Rules:
 * - `@NotNull`: Ensures parameters are not null.
 * - `@Min`: Ensures numerical values meet the specified minimum.
 * - `@Valid`: Enforces validation for request bodies containing DTOs.
 */
@RestController
@RequestMapping("/api/transcripts")
@Validated // Enables validation for @PathVariable and @RequestParam
public class TranscriptController {

    private static final Logger logger = LoggerFactory.getLogger(TranscriptController.class);
    private final TranscriptService transcriptService;

    /**
     * Constructor for TranscriptController.
     *
     * @param transcriptService The service to handle transcript-related operations.
     */
    public TranscriptController(TranscriptService transcriptService) {
        this.transcriptService = transcriptService;
    }

    /**
     * Retrieves all transcripts.
     *
     * @return A list of TranscriptDTO objects representing all transcripts.
     */
    @GetMapping
    public List<TranscriptDTO> getAll() {
        logger.info("Fetching all transcripts...");
        List<TranscriptDTO> transcripts = transcriptService.getAll();
        logger.info("Successfully fetched {} transcripts.", transcripts.size());
        return transcripts;
    }

    /**
     * Retrieves a transcript by its ID.
     *
     * @param id The ID of the transcript. Must be greater than or equal to 1 and not null.
     * @return The TranscriptDTO object representing the requested transcript.
     * @throws jakarta.validation.ConstraintViolationException if validation fails for the ID.
     */
    @GetMapping("/{id}")
    public TranscriptDTO getOne(@PathVariable @NotNull(message = "ID must not be null.") @Min(value = 1, message = "ID must be greater than or equal to 1.") Long id) {
        logger.info("Fetching transcript with ID: {}", id);
        TranscriptDTO transcript = transcriptService.getOne(id);
        logger.info("Successfully fetched transcript: {}", transcript);
        return transcript;
    }

    /**
     * Creates a new transcript.
     *
     * @param dto The data for the new transcript. Must pass validation rules defined in TranscriptDTO.
     * @return The created TranscriptDTO object.
     * @throws jakarta.validation.ConstraintViolationException if validation fails for the request body.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TranscriptDTO create(@Valid @RequestBody TranscriptDTO dto) {
        logger.info("Creating a new transcript: {}", dto);
        TranscriptDTO createdTranscript = transcriptService.create(dto);
        logger.info("Successfully created transcript: {}", createdTranscript);
        return createdTranscript;
    }

    /**
     * Updates an existing transcript.
     *
     * @param id  The ID of the transcript to update. Must be greater than or equal to 1 and not null.
     * @param dto The updated data for the transcript. Must pass validation rules defined in TranscriptDTO.
     * @return The updated TranscriptDTO object.
     * @throws jakarta.validation.ConstraintViolationException if validation fails for the ID or request body.
     */
    @PutMapping("/{id}")
    public TranscriptDTO update(
            @PathVariable @NotNull(message = "ID must not be null.") @Min(value = 1, message = "ID must be greater than or equal to 1.") Long id,
            @Valid @RequestBody TranscriptDTO dto) {
        logger.info("Updating transcript with ID: {}. New data: {}", id, dto);
        TranscriptDTO updatedTranscript = transcriptService.update(id, dto);
        logger.info("Successfully updated transcript: {}", updatedTranscript);
        return updatedTranscript;
    }

    /**
     * Deletes a transcript by its ID.
     *
     * @param id The ID of the transcript to delete. Must be greater than or equal to 1 and not null.
     * @throws jakarta.validation.ConstraintViolationException if validation fails for the ID.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @NotNull(message = "ID must not be null.") @Min(value = 1, message = "ID must be greater than or equal to 1.") Long id) {
        logger.info("Deleting transcript with ID: {}", id);
        transcriptService.delete(id);
        logger.info("Successfully deleted transcript with ID: {}", id);
    }
}
