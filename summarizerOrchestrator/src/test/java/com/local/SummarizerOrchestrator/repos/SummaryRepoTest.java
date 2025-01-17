package com.local.SummarizerOrchestrator.repos;

import com.local.SummarizerOrchestrator.models.Summary;
import com.local.SummarizerOrchestrator.models.Transcript;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test") // Ensure you have a test profile configured for H2 or in-memory database
class SummaryRepoTest {

    @Autowired
    private SummaryRepo summaryRepo;

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
        Summary summary = new Summary();
        summary.setTranscript(transcript);
        summary.setProviderName("provider1");
        summary.setSummaryText("This is a summary text.");
        summaryRepo.save(summary);

        List<Summary> summaries = summaryRepo.findByTranscriptId(transcript.getId());
        assertEquals(1, summaries.size());
        assertEquals("provider1", summaries.get(0).getProviderName());
    }

    @Test
    void testExistsByTranscriptId() {
        Summary summary = new Summary();
        summary.setTranscript(transcript);
        summary.setProviderName("provider1");
        summary.setSummaryText("This is a summary text.");
        summaryRepo.save(summary);

        boolean exists = summaryRepo.existsByTranscriptId(transcript.getId());
        assertTrue(exists);
    }

    @Test
    void testDeleteByTranscriptId() {
        Summary summary = new Summary();
        summary.setTranscript(transcript);
        summary.setProviderName("provider1");
        summary.setSummaryText("This is a summary text.");
        summaryRepo.save(summary);

        summaryRepo.deleteByTranscriptId(transcript.getId());

        List<Summary> summaries = summaryRepo.findByTranscriptId(transcript.getId());
        assertTrue(summaries.isEmpty());
    }
}