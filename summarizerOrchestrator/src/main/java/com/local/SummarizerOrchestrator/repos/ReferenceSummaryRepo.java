package com.local.SummarizerOrchestrator.repos;

import com.local.SummarizerOrchestrator.models.ReferenceSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing {@link ReferenceSummary} entities.
 *
 * <p>This interface provides methods for CRUD operations and custom query
 * methods to retrieve reference summaries associated with transcripts.</p>
 */
public interface ReferenceSummaryRepo extends JpaRepository<ReferenceSummary, Long> {

    /**
     * Finds a {@link ReferenceSummary} by the associated transcript ID.
     *
     * <p>Unlike a list-based query, this method returns an {@link Optional} to
     * explicitly indicate the presence or absence of a result.</p>
     *
     * @param transcriptId The ID of the transcript associated with the reference summary.
     * @return An {@link Optional} containing the {@link ReferenceSummary} if found, or empty if not found.
     */
    Optional<ReferenceSummary> findByTranscriptId(Long transcriptId);
}
