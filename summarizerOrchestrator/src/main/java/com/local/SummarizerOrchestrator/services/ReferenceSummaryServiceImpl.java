package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.models.ReferenceSummary;
import com.local.SummarizerOrchestrator.repos.ReferenceSummaryRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReferenceSummaryServiceImpl implements ReferenceSummaryService {

    private static final Logger logger = LoggerFactory.getLogger(ReferenceSummaryServiceImpl.class);

    private final ReferenceSummaryRepo referenceSummaryRepo;

    public ReferenceSummaryServiceImpl(ReferenceSummaryRepo referenceSummaryRepo) {
        this.referenceSummaryRepo = referenceSummaryRepo;
    }

    @Override
    public ReferenceSummary getReferenceSummary(Long transcriptId) {
        logger.info("Fetching reference summary for transcript ID: {}", transcriptId);
        Optional<ReferenceSummary> referenceSummary = referenceSummaryRepo.findByTranscriptId(transcriptId);

        return referenceSummary.orElseThrow(() ->
                new RuntimeException("Reference summary not found for transcript ID: " + transcriptId)
        );
    }
}
