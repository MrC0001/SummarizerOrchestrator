package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.dtos.MetricsBatchResponseDTO;
import com.local.SummarizerOrchestrator.dtos.MetricsRequestDTO;
import com.local.SummarizerOrchestrator.dtos.MetricsResponseDTO;
import com.local.SummarizerOrchestrator.models.Metrics;
import com.local.SummarizerOrchestrator.models.ReferenceSummary;
import com.local.SummarizerOrchestrator.models.Summary;
import com.local.SummarizerOrchestrator.models.Transcript;
import com.local.SummarizerOrchestrator.repos.MetricsRepo;
import com.local.SummarizerOrchestrator.repos.SummaryRepo;
import com.local.SummarizerOrchestrator.repos.TranscriptRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MetricsServiceTest {

    @Mock
    private TranscriptRepo transcriptRepo;
    @Mock
    private SummaryRepo summaryRepo;
    @Mock
    private MetricsRepo metricsRepo;
    @Mock
    private ReferenceSummaryService referenceSummaryService;

    @InjectMocks
    private MetricsServiceImpl metricsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCalculateMetricsForTranscript_Success() {
        MetricsRequestDTO request = new MetricsRequestDTO();
        request.setTranscriptId(123L);

        Transcript transcript = new Transcript();
        transcript.setId(123L);
        transcript.setScenario("TestScenario");
        when(transcriptRepo.findById(123L)).thenReturn(Optional.of(transcript));

        ReferenceSummary refSummary = new ReferenceSummary();
        refSummary.setSummaryText("Reference text");
        when(referenceSummaryService.getReferenceSummary(123L)).thenReturn(refSummary);

        Summary summary = new Summary();
        summary.setId(456L);
        summary.setSummaryText("Candidate text");
        summary.setProviderName("provider1");
        when(summaryRepo.findByTranscriptId(123L)).thenReturn(List.of(summary));

        // Mock metrics entity
        Metrics metricsMock = new Metrics();
        metricsMock.setSummary(summary);
        when(metricsRepo.save(any(Metrics.class))).thenReturn(metricsMock);

        MetricsBatchResponseDTO result = metricsService.calculateMetricsForTranscript(request);
        assertNotNull(result);
        assertEquals(1, result.getSummaryMetrics().size());
    }

    @Test
    void testCalculateMetricsForTranscript_TranscriptNotFound() {
        MetricsRequestDTO request = new MetricsRequestDTO();
        request.setTranscriptId(999L);
        when(transcriptRepo.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> metricsService.calculateMetricsForTranscript(request));
    }

    @Test
    void testGetMetricsBySummaryId_Found() {
        Long summaryId = 123L;
        Metrics expectedMetrics = new Metrics();
        when(metricsRepo.findBySummaryId(summaryId)).thenReturn(Optional.of(expectedMetrics));

        Metrics result = metricsService.getMetricsBySummaryId(summaryId);
        assertSame(expectedMetrics, result);
    }

    @Test
    void testGetMetricsBySummaryId_NotFound() {
        Long summaryId = 999L;
        when(metricsRepo.findBySummaryId(summaryId)).thenReturn(Optional.empty());
        Metrics result = metricsService.getMetricsBySummaryId(summaryId);
        assertNull(result);
    }
}