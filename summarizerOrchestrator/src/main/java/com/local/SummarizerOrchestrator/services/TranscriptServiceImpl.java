package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.dtos.TranscriptDTO;
import com.local.SummarizerOrchestrator.models.Transcript;
import com.local.SummarizerOrchestrator.repos.TranscriptRepo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link TranscriptService} interface.
 * Handles CRUD operations for transcripts.
 */
@Service
public class TranscriptServiceImpl implements TranscriptService {

    private static final Logger logger = LoggerFactory.getLogger(TranscriptServiceImpl.class);

    private final TranscriptRepo transcriptRepo;

    /**
     * Constructor for TranscriptServiceImpl.
     *
     * @param transcriptRepo Repository for managing transcripts.
     */
    public TranscriptServiceImpl(TranscriptRepo transcriptRepo) {
        this.transcriptRepo = transcriptRepo;
    }

    /**
     * Retrieves all transcripts.
     *
     * @return A list of TranscriptDTO objects.
     */
    @Override
    public List<TranscriptDTO> getAll() {
        logger.info("Fetching all transcripts.");
        List<TranscriptDTO> transcripts = transcriptRepo.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        logger.info("Successfully fetched {} transcripts.", transcripts.size());
        return transcripts;
    }

    /**
     * Retrieves a single transcript by its ID.
     *
     * @param id The ID of the transcript.
     * @return A TranscriptDTO object.
     */
    @Override
    public TranscriptDTO getOne(@NotNull(message = "Transcript ID must not be null.") Long id) {
        logger.info("Fetching transcript with ID: {}", id);
        Transcript entity = transcriptRepo.findById(id)
                .orElseThrow(() -> {
                    logger.error("Transcript not found for ID: {}", id);
                    return new RuntimeException("Transcript not found");
                });
        logger.info("Successfully fetched transcript with ID: {}", id);
        return toDTO(entity);
    }

    /**
     * Creates a new transcript.
     *
     * @param dto The data transfer object containing transcript details.
     * @return The created TranscriptDTO.
     */
    @Override
    public TranscriptDTO create(@Valid @NotNull TranscriptDTO dto) {
        logger.info("Creating a new transcript.");
        Transcript entity = new Transcript();
        entity.setScenario(dto.getScenario());
        entity.setTranscript(dto.getTranscript());
        entity = transcriptRepo.save(entity);
        logger.info("Successfully created transcript with ID: {}", entity.getId());
        return toDTO(entity);
    }

    /**
     * Updates an existing transcript.
     *
     * @param id  The ID of the transcript to update.
     * @param dto The data transfer object containing updated details.
     * @return The updated TranscriptDTO.
     */
    @Override
    public TranscriptDTO update(@NotNull(message = "Transcript ID must not be null.") Long id,
                                @Valid @NotNull TranscriptDTO dto) {
        logger.info("Updating transcript with ID: {}", id);
        Transcript entity = transcriptRepo.findById(id)
                .orElseThrow(() -> {
                    logger.error("Transcript not found for ID: {}", id);
                    return new RuntimeException("Transcript not found");
                });

        entity.setScenario(dto.getScenario());
        entity.setTranscript(dto.getTranscript());
        entity = transcriptRepo.save(entity);
        logger.info("Successfully updated transcript with ID: {}", entity.getId());
        return toDTO(entity);
    }

    /**
     * Deletes a transcript by its ID.
     *
     * @param id The ID of the transcript to delete.
     */
    @Override
    public void delete(@NotNull(message = "Transcript ID must not be null.") Long id) {
        logger.info("Deleting transcript with ID: {}", id);
        if (!transcriptRepo.existsById(id)) {
            logger.error("Transcript not found for ID: {}", id);
            throw new RuntimeException("Transcript not found");
        }
        transcriptRepo.deleteById(id);
        logger.info("Successfully deleted transcript with ID: {}", id);
    }

    /**
     * Converts a Transcript entity to a TranscriptDTO.
     *
     * @param entity The Transcript entity.
     * @return A TranscriptDTO object.
     */
    private TranscriptDTO toDTO(Transcript entity) {
        TranscriptDTO dto = new TranscriptDTO();
        dto.setId(entity.getId());
        dto.setScenario(entity.getScenario());
        dto.setTranscript(entity.getTranscript());
        return dto;
    }
}
