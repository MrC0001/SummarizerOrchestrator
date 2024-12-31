package com.local.SummarizerOrchestrator.repos;

import com.local.SummarizerOrchestrator.models.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing {@link Transcript} entities.
 * Provides CRUD operations and query methods for the Transcript entity.
 */
public interface TranscriptRepo extends JpaRepository<Transcript, Long> {
}
