package com.local.SummarizerOrchestrator.repos;

import com.local.SummarizerOrchestrator.models.Metrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface MetricsRepo extends JpaRepository<Metrics, Long> {
    /**
     * Retrieves metrics associated with a specific summary ID.
     *
     * @param summaryId The ID of the summary.
     * @return The metrics associated with the summary.
     */
    Optional<Metrics> findBySummaryId(Long summaryId);

    /**
     * Deletes all metrics associated with the given transcript ID.
     *
     * @param transcriptId The ID of the transcript.
     */
    @Modifying
    @Query("DELETE FROM Metrics m WHERE m.summary.id IN (SELECT s.id FROM Summary s WHERE s.transcript.id = :transcriptId)")
    void deleteByTranscriptId(@Param("transcriptId") Long transcriptId);

}
