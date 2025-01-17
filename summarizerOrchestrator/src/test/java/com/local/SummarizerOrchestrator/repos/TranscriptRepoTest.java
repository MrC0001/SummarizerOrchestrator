package com.local.SummarizerOrchestrator.repos;

import com.local.SummarizerOrchestrator.models.Transcript;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test") // Ensure you have a test profile configured for H2 or in-memory database
class TranscriptRepoTest {

    @Autowired
    private TranscriptRepo transcriptRepo;

    @Autowired
    private ReferenceSummaryRepo referenceSummaryRepo;

    @BeforeEach
    void setUp() {
        referenceSummaryRepo.deleteAll();
        transcriptRepo.deleteAll();
    }

    @Test
    void testSaveAndFindById() {
        Transcript transcript = new Transcript();
        transcript.setScenario("TestScenario");
        transcript.setTranscript("This is a test transcript."); // Set the transcript field
        transcript = transcriptRepo.save(transcript);

        Optional<Transcript> found = transcriptRepo.findById(transcript.getId());
        assertTrue(found.isPresent());
        assertEquals("TestScenario", found.get().getScenario());
    }

    @Test
    void testDeleteById() {
        Transcript transcript = new Transcript();
        transcript.setScenario("TestScenario");
        transcript.setTranscript("This is a test transcript."); // Set the transcript field
        transcript = transcriptRepo.save(transcript);

        transcriptRepo.deleteById(transcript.getId());
        Optional<Transcript> found = transcriptRepo.findById(transcript.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        Transcript transcript1 = new Transcript();
        transcript1.setScenario("Scenario1");
        transcript1.setTranscript("This is the first test transcript."); // Set the transcript field
        Transcript transcript2 = new Transcript();
        transcript2.setScenario("Scenario2");
        transcript2.setTranscript("This is the second test transcript."); // Set the transcript field

        transcriptRepo.save(transcript1);
        transcriptRepo.save(transcript2);

        List<Transcript> transcripts = transcriptRepo.findAll();
        assertEquals(2, transcripts.size());
    }
}