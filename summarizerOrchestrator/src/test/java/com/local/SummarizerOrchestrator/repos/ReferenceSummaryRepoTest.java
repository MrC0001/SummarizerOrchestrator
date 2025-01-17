package com.local.SummarizerOrchestrator.repos;

import com.local.SummarizerOrchestrator.models.ReferenceSummary;
import com.local.SummarizerOrchestrator.models.Transcript;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test") // Ensure you have a test profile configured for H2 or in-memory database
class ReferenceSummaryRepoTest {

    @Autowired
    private ReferenceSummaryRepo referenceSummaryRepo;

    @Autowired
    private TranscriptRepo transcriptRepo;

    private Transcript transcript;

    @BeforeEach
    void setUp() {
        transcript = new Transcript();
        transcript.setScenario("TestScenario");
        transcript.setTranscript("This is a test transcript.");
        transcript = transcriptRepo.save(transcript);
    }

    @Test
    void testFindByTranscriptId() {
        ReferenceSummary referenceSummary = new ReferenceSummary();
        referenceSummary.setTranscript(transcript);
        referenceSummary.setSummaryText("This is a reference summary text.");
        referenceSummaryRepo.save(referenceSummary);

        Optional<ReferenceSummary> found = referenceSummaryRepo.findByTranscriptId(transcript.getId());
        assertTrue(found.isPresent());
        assertEquals("This is a reference summary text.", found.get().getSummaryText());
    }

    @Test
    void testSaveAndFindById() {
        ReferenceSummary referenceSummary = new ReferenceSummary();
        referenceSummary.setTranscript(transcript);
        referenceSummary.setSummaryText("This is a reference summary text.");
        referenceSummary = referenceSummaryRepo.save(referenceSummary);

        Optional<ReferenceSummary> found = referenceSummaryRepo.findById(referenceSummary.getId());
        assertTrue(found.isPresent());
        assertEquals("This is a reference summary text.", found.get().getSummaryText());
    }

    @Test
    void testDeleteById() {
        ReferenceSummary referenceSummary = new ReferenceSummary();
        referenceSummary.setTranscript(transcript);
        referenceSummary.setSummaryText("This is a reference summary text.");
        referenceSummary = referenceSummaryRepo.save(referenceSummary);

        referenceSummaryRepo.deleteById(referenceSummary.getId());

        Optional<ReferenceSummary> found = referenceSummaryRepo.findById(referenceSummary.getId());
        assertFalse(found.isPresent());
    }
}