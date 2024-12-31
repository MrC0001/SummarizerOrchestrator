package com.local.SummarizerOrchestrator.repos;

import com.local.SummarizerOrchestrator.models.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository interface for managing {@link Summary} entities.
 * Provides methods to perform CRUD operations and custom queries.
 */
public interface SummaryRepo extends JpaRepository<Summary, Long> {

    /**
     * Retrieves a list of summaries associated with the given transcript ID.
     *
     * @param transcriptId The ID of the transcript.
     * @return A list of summaries.
     */
    List<Summary> findByTranscriptId(Long transcriptId);

    /**
     * Checks if any summaries exist for the given transcript ID.
     *
     * @param transcriptId The ID of the transcript.
     * @return {@code true} if summaries exist, {@code false} otherwise.
     */
    boolean existsByTranscriptId(Long transcriptId);

    /**
     * Deletes all summaries associated with the given transcript ID.
     *
     * @param transcriptId The ID of the transcript.
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM Summary s WHERE s.transcript.id = :transcriptId")
    void deleteByTranscriptId(Long transcriptId);
}
