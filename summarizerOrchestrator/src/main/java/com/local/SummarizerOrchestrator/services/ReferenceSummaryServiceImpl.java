package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.models.ReferenceSummary;
import com.local.SummarizerOrchestrator.repos.ReferenceSummaryRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service implementation for managing {@link ReferenceSummary} entities.
 *
 * <p>This service handles operations related to reference summaries, which are
 * used as baselines for evaluating generated summaries.</p>
 */
@Service
public class ReferenceSummaryServiceImpl implements ReferenceSummaryService {

    private static final Logger logger = LoggerFactory.getLogger(ReferenceSummaryServiceImpl.class);

    private final ReferenceSummaryRepo referenceSummaryRepo;

    /**
     * Constructs a new instance of {@link ReferenceSummaryServiceImpl}.
     *
     * @param referenceSummaryRepo The repository for managing {@link ReferenceSummary} entities.
     */
    public ReferenceSummaryServiceImpl(ReferenceSummaryRepo referenceSummaryRepo) {
        this.referenceSummaryRepo = referenceSummaryRepo;
    }

    /**
     * Retrieves the reference summary associated with the specified transcript ID.
     *
     * @param transcriptId The ID of the transcript whose reference summary is to be retrieved.
     * @return The {@link ReferenceSummary} entity associated with the given transcript ID.
     * @throws RuntimeException If no reference summary is found for the specified transcript ID.
     */
    @Override
    public ReferenceSummary getReferenceSummary(Long transcriptId) {
        logger.info("Fetching reference summary for transcript ID: {}", transcriptId);

        return referenceSummaryRepo.findByTranscriptId(transcriptId)
                .orElseThrow(() -> new RuntimeException(
                        "Reference summary not found for transcript ID: " + transcriptId
                ));
    }
}
