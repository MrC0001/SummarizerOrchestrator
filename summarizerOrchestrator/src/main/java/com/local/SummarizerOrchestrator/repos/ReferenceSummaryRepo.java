package com.local.SummarizerOrchestrator.repos;

import com.local.SummarizerOrchestrator.models.ReferenceSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReferenceSummaryRepo extends JpaRepository<ReferenceSummary, Long> {
    Optional<ReferenceSummary> findByTranscriptId(Long transcriptId); // optional is an interesting caveat for nulls where in other repo find by id was a list which already handles nulls as a list can be empty.
}

