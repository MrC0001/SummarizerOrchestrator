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
 *
 * <p>Provides endpoints to perform CRUD operations on transcripts, adhering to
 * Jakarta Validation rules for input validation.</p>
 */
@RestController
@RequestMapping("/api/transcripts")
@Validated
public class TranscriptController {

    private static final Logger logger = LoggerFactory.getLogger(TranscriptController.class);
    private final TranscriptService transcriptService;

    /**
     * Constructs the TranscriptController.
     *
     * @param transcriptService The service responsible for transcript-related operations.
     */
    public TranscriptController(TranscriptService transcriptService) {
        this.transcriptService = transcriptService;
    }

    /**
     * Retrieves all transcripts.
     *
     * @return A list of {@link TranscriptDTO} objects representing all transcripts.
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
     * @return A {@link TranscriptDTO} representing the requested transcript.
     */
    @GetMapping("/{id}")
    public TranscriptDTO getOne(
            @PathVariable @NotNull(message = "ID must not be null.")
            @Min(value = 1, message = "ID must be greater than or equal to 1.") Long id) {
        logger.info("Fetching transcript with ID: {}", id);
        TranscriptDTO transcript = transcriptService.getOne(id);
        logger.info("Successfully fetched transcript: {}", transcript);
        return transcript;
    }

    /**
     * Creates a new transcript.
     *
     * @param dto The data for the new transcript. Must pass validation rules defined in {@link TranscriptDTO}.
     * @return The created {@link TranscriptDTO}.
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
     * @param dto The updated data for the transcript. Must pass validation rules defined in {@link TranscriptDTO}.
     * @return The updated {@link TranscriptDTO}.
     */
    @PutMapping("/{id}")
    public TranscriptDTO update(
            @PathVariable @NotNull(message = "ID must not be null.")
            @Min(value = 1, message = "ID must be greater than or equal to 1.") Long id,
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
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable @NotNull(message = "ID must not be null.")
            @Min(value = 1, message = "ID must be greater than or equal to 1.") Long id) {
        logger.info("Deleting transcript with ID: {}", id);
        transcriptService.delete(id);
        logger.info("Successfully deleted transcript with ID: {}", id);
    }
}
