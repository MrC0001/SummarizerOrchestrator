package com.local.SummarizerOrchestrator.repos;

import com.local.SummarizerOrchestrator.models.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TranscriptRepo extends JpaRepository<Transcript, Long> {
}
