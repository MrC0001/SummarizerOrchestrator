package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import com.local.SummarizerOrchestrator.models.Summary;
import com.local.SummarizerOrchestrator.models.Transcript;
import com.local.SummarizerOrchestrator.providers.SummarizationProvider;
import com.local.SummarizerOrchestrator.repos.SummaryRepo;
import com.local.SummarizerOrchestrator.repos.TranscriptRepo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link SummarizationService} interface.
 * Handles operations related to summarization, including generating, saving, retrieving, and comparing summaries.
 */
@Service
public class SummarizationServiceImpl implements SummarizationService {

    private static final Logger logger = LoggerFactory.getLogger(SummarizationServiceImpl.class);

    private final SummaryRepo summaryRepo;
    private final TranscriptRepo transcriptRepo;
    private final List<SummarizationProvider> providers;

    /**
     * Constructor for SummarizationServiceImpl.
     *
     * @param summaryRepo    Repository for managing summaries.
     * @param transcriptRepo Repository for managing transcripts.
     * @param providers      List of summarization providers.
     */
    public SummarizationServiceImpl(SummaryRepo summaryRepo, TranscriptRepo transcriptRepo, List<SummarizationProvider> providers) {
        this.summaryRepo = summaryRepo;
        this.transcriptRepo = transcriptRepo;
        this.providers = providers;
    }

    /**
     * Checks if summaries exist for a given transcript ID.
     *
     * @param transcriptId The ID of the transcript.
     * @return {@code true} if summaries exist, {@code false} otherwise.
     */
    @Override
    public boolean summariesExistForTranscript(@NotNull(message = "Transcript ID must not be null.") Long transcriptId) {
        logger.info("Checking if summaries exist for transcript ID: {}", transcriptId);
        return summaryRepo.existsByTranscriptId(transcriptId);
    }

    /**
     * Processes a summarization request, generates summaries using providers,
     * and saves the results to the database.
     *
     * @param request The summarization request DTO.
     * @return A list of summarization response DTOs containing the generated summaries.
     */
    @Override
    public List<SummarizationResponseDTO> summarizeAndSave(@Valid @NotNull SummarizationRequestDTO request) {
        logger.info("Starting summarization and save for transcript ID: {}", request.getTranscriptId());

        Transcript transcript = transcriptRepo.findById(request.getTranscriptId())
                .orElseThrow(() -> {
                    logger.error("Transcript not found for ID: {}", request.getTranscriptId());
                    return new RuntimeException("Transcript not found");
                });

        logger.debug("Found transcript for summarization: {}", transcript.getId());

        List<SummarizationResponseDTO> responses = summarizeAcrossProviders(request);

        logger.debug("Generated summaries: {}", responses);

        responses.stream()
                .filter(response -> !response.getSummary().startsWith("Error"))
                .forEach(response -> {
                    Summary summary = new Summary();
                    summary.setTranscript(transcript);
                    summary.setProviderName(response.getProviderName());
                    summary.setSummaryText(response.getSummary());
                    summaryRepo.save(summary);
                    logger.info("Saved summary for provider: {}", response.getProviderName());
                });

        return responses;
    }

    /**
     * Retrieves summaries for a specific transcript and formats the response.
     *
     * @param transcriptId The ID of the transcript.
     * @return A ResponseEntity containing the summaries or an error message if no summaries are found.
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getSummariesResponse(@NotNull(message = "Transcript ID must not be null.") Long transcriptId) {
        logger.info("Fetching summaries for transcript ID: {}", transcriptId);

        List<Summary> summaries = summaryRepo.findByTranscriptId(transcriptId);
        if (summaries.isEmpty()) {
            logger.warn("No summaries found for transcript ID: {}", transcriptId);
            return ResponseEntity.status(404).body("No summaries found for the given transcript.");
        }

        logger.info("Found {} summaries for transcript ID: {}", summaries.size(), transcriptId);
        return ResponseEntity.ok(summaries);
    }

    /**
     * Compares old and new summaries for a given transcript.
     *
     * @param request The summarization request DTO.
     * @return A map containing old and new summaries for comparison.
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> compareSummaries(@Valid @NotNull SummarizationRequestDTO request) {
        logger.info("Comparing old and new summaries for transcript ID: {}", request.getTranscriptId());

        List<Summary> oldSummaries = summaryRepo.findByTranscriptId(request.getTranscriptId());
        List<SummarizationResponseDTO> newSummaries = summarizeAcrossProviders(request);

        logger.debug("Old Summaries: {}", oldSummaries);
        logger.debug("New Summaries: {}", newSummaries);

        Map<String, Object> result = new HashMap<>();
        result.put("oldSummaries", oldSummaries.stream()
                .map(summary -> new SummarizationResponseDTO(summary.getProviderName(), summary.getSummaryText()))
                .collect(Collectors.toList()));
        result.put("newSummaries", newSummaries);

        return result;
    }

    /**
     * Overwrites existing summaries for a specific transcript with new summaries.
     *
     * @param transcriptId The ID of the transcript.
     * @param newSummaries The new summaries to save.
     */
    @Override
    @Transactional
    public void overwriteSummaries(@NotNull(message = "Transcript ID must not be null.") Long transcriptId,
                                   @Valid @NotNull List<SummarizationResponseDTO> newSummaries) {
        logger.info("Overwriting summaries for transcript ID: {}", transcriptId);

        Transcript transcript = transcriptRepo.findById(transcriptId)
                .orElseThrow(() -> {
                    logger.error("Transcript not found for ID: {}", transcriptId);
                    return new RuntimeException("Transcript not found");
                });

        logger.debug("Found transcript for overwrite: {}", transcript.getId());

        summaryRepo.deleteByTranscriptId(transcriptId);
        logger.info("Deleted old summaries for transcript ID: {}", transcriptId);

        List<Summary> summariesToSave = newSummaries.stream()
                .map(dto -> {
                    Summary summary = new Summary();
                    summary.setTranscript(transcript);
                    summary.setProviderName(dto.getProviderName());
                    summary.setSummaryText(dto.getSummary());
                    return summary;
                }).collect(Collectors.toList());

        summaryRepo.saveAll(summariesToSave);
        logger.info("Saved new summaries for transcript ID: {}", transcriptId);
    }

    /**
     * Distributes a summarization request across all available providers.
     *
     * @param request The summarization request DTO.
     * @return A list of summarization response DTOs containing results from all providers.
     */
    private List<SummarizationResponseDTO> summarizeAcrossProviders(@Valid SummarizationRequestDTO request) {
        logger.info("Distributing summarization request across providers.");
        return providers.stream()
                .map(provider -> {
                    try {
                        return provider.summarize(request);
                    } catch (Exception e) {
                        logger.error("Error summarizing with provider {}: {}", provider.getProviderName(), e.getMessage(), e);
                        return new SummarizationResponseDTO(provider.getProviderName(), "Error: " + e.getMessage());
                    }
                }).collect(Collectors.toList());
    }
}
