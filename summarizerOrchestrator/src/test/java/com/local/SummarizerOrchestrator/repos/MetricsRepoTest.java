package com.local.SummarizerOrchestrator.repos;

import com.local.SummarizerOrchestrator.models.Metrics;
import com.local.SummarizerOrchestrator.models.Summary;
import com.local.SummarizerOrchestrator.models.Transcript;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test") // Ensure you have a test profile configured for H2 or in-memory database
@Transactional
class MetricsRepoTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MetricsRepo metricsRepo;

    @Autowired
    private TranscriptRepo transcriptRepo;

    @Autowired
    private SummaryRepo summaryRepo;

    private Transcript transcript;
    private Summary summary;

    @BeforeEach
    void setUp() {
        transcript = new Transcript();
        transcript.setScenario("TestScenario");
        transcript.setTranscript("This is a test transcript.");
        transcript = transcriptRepo.save(transcript);

        summary = new Summary();
        summary.setTranscript(transcript);
        summary.setProviderName("TestProvider");
        summary.setSummaryText("This is a test summary.");
        summary = summaryRepo.save(summary);
    }

    @Test
    void testFindBySummaryId() {
        Metrics metrics = new Metrics();
        metrics.setTranscript(transcript);
        metrics.setSummary(summary);
        metrics.setBleu(0.85f);
        metrics.setRouge1(0.65f);
        metricsRepo.save(metrics);

        Optional<Metrics> found = metricsRepo.findBySummaryId(summary.getId());
        assertTrue(found.isPresent());
        assertEquals(0.85f, found.get().getBleu());
        assertEquals(0.65f, found.get().getRouge1());
    }

    @Test
    void testSaveAndFindById() {
        Metrics metrics = new Metrics();
        metrics.setTranscript(transcript);
        metrics.setSummary(summary);
        metrics.setBleu(0.85f);
        metrics.setRouge1(0.65f);
        metrics = metricsRepo.save(metrics);

        Optional<Metrics> found = metricsRepo.findById(metrics.getId());
        assertTrue(found.isPresent());
        assertEquals(0.85f, found.get().getBleu());
        assertEquals(0.65f, found.get().getRouge1());
    }

    @Test
    void testDeleteByTranscriptId() {
        // Persist the transcript and summary first
        transcriptRepo.save(transcript);
        summaryRepo.save(summary);

        // Create and save the metrics
        Metrics metrics = new Metrics();
        metrics.setTranscript(transcript);
        metrics.setSummary(summary);
        metrics.setBleu(0.85f);
        metrics.setRouge1(0.65f);
        metricsRepo.save(metrics);

        // Flush and clear the EntityManager to ensure entities are persisted
        entityManager.flush();
        entityManager.clear();

        // Perform the delete operation
        metricsRepo.deleteByTranscriptId(transcript.getId());

        // Verify that the metrics are deleted
        Optional<Metrics> found = metricsRepo.findById(metrics.getId());
        assertFalse(found.isPresent(), "Metrics should be deleted when transcript is deleted");
    }
}