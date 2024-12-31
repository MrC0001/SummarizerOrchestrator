package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.dtos.TranscriptDTO;

import java.util.List;

/**
 * Service interface for managing transcript operations.
 * Defines methods for CRUD operations on transcripts.
 */
public interface TranscriptService {

    /**
     * Retrieves a list of all transcripts.
     *
     * @return A list of TranscriptDTO objects representing all stored transcripts.
     */
    List<TranscriptDTO> getAll();

    /**
     * Retrieves a specific transcript by its ID.
     *
     * @param id The ID of the transcript to retrieve.
     * @return A TranscriptDTO object representing the requested transcript.
     */
    TranscriptDTO getOne(Long id);

    /**
     * Creates a new transcript.
     *
     * @param dto The data transfer object containing the details of the new transcript.
     * @return The created TranscriptDTO object.
     */
    TranscriptDTO create(TranscriptDTO dto);

    /**
     * Updates an existing transcript.
     *
     * @param id  The ID of the transcript to update.
     * @param dto The data transfer object containing the updated details of the transcript.
     * @return The updated TranscriptDTO object.
     */
    TranscriptDTO update(Long id, TranscriptDTO dto);

    /**
     * Deletes a transcript by its ID.
     *
     * @param id The ID of the transcript to delete.
     */
    void delete(Long id);
}
