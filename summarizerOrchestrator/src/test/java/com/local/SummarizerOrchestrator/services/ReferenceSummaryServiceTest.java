package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.models.ReferenceSummary;
import com.local.SummarizerOrchestrator.repos.ReferenceSummaryRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class ReferenceSummaryServiceTest {

    @Mock
    private ReferenceSummaryRepo referenceSummaryRepo;

    @InjectMocks
    private ReferenceSummaryServiceImpl referenceSummaryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetReferenceSummary() {
        Long transcriptId = 1L;
        ReferenceSummary referenceSummary = new ReferenceSummary();
        referenceSummary.setSummaryText("Test summary");

        when(referenceSummaryRepo.findByTranscriptId(transcriptId)).thenReturn(Optional.of(referenceSummary));

        ReferenceSummary result = referenceSummaryService.getReferenceSummary(transcriptId);
        assertEquals("Test summary", result.getSummaryText());
    }

    @Test
    void testGetReferenceSummary_NotFound() {
        Long transcriptId = 1L;

        when(referenceSummaryRepo.findByTranscriptId(transcriptId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> referenceSummaryService.getReferenceSummary(transcriptId));
    }

}