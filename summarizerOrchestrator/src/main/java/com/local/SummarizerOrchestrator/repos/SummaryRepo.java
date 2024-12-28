package com.local.SummarizerOrchestrator.repos;

import com.local.SummarizerOrchestrator.models.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SummaryRepo extends JpaRepository<Summary, Long> {
    List<Summary> findByTranscriptId(Long transcriptId);
    boolean existsByTranscriptId(Long transcriptId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Summary s WHERE s.transcript.id = :transcriptId")
    void deleteByTranscriptId(Long transcriptId);
}
