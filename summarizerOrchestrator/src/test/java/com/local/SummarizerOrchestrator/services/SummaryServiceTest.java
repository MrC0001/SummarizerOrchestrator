package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import com.local.SummarizerOrchestrator.models.Summary;
import com.local.SummarizerOrchestrator.models.Transcript;
import com.local.SummarizerOrchestrator.providers.SummarizationProvider;
import com.local.SummarizerOrchestrator.repos.SummaryRepo;
import com.local.SummarizerOrchestrator.repos.TranscriptRepo;
import com.local.SummarizerOrchestrator.repos.MetricsRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class SummaryServiceTest {

    @Mock
    private SummaryRepo summaryRepo;

    @Mock
    private TranscriptRepo transcriptRepo;

    @Mock
    private MetricsRepo metricsRepo;

    @Mock
    private List<SummarizationProvider> providers;

    @Mock
    private Executor asyncExecutor;

    @InjectMocks
    private SummarizationServiceImpl summarizationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSummariesExistForTranscript() {
        Long transcriptId = 1L;
        when(summaryRepo.existsByTranscriptId(transcriptId)).thenReturn(true);

        boolean result = summarizationService.summariesExistForTranscript(transcriptId);
        assertTrue(result);
    }

    @Test
    void testSummarizeAndSaveAsync() {
        Long transcriptId = 1L;
        SummarizationRequestDTO request = new SummarizationRequestDTO();
        request.setTranscriptId(transcriptId);

        Transcript transcript = new Transcript();
        transcript.setId(transcriptId);

        when(transcriptRepo.findById(transcriptId)).thenReturn(Optional.of(transcript));
        when(summaryRepo.findByTranscriptId(transcriptId)).thenReturn(Collections.emptyList());

        CompletableFuture<Map<String, Object>> future = summarizationService.summarizeAndSaveAsync(request);
        Map<String, Object> result = future.join();

        assertNotNull(result);
        assertTrue(result.containsKey("oldSummaries"));
        assertTrue(result.containsKey("newSummaries"));
    }

    @Test
    void testGetSummariesResponse() {
        Long transcriptId = 1L;
        Transcript transcript = new Transcript();
        transcript.setId(transcriptId);

        Summary summary = new Summary();
        summary.setTranscript(transcript);
        summary.setSummaryText("Test summary");

        when(summaryRepo.findByTranscriptId(transcriptId)).thenReturn(Collections.singletonList(summary));

        ResponseEntity<?> response = summarizationService.getSummariesResponse(transcriptId);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testGetSummariesResponse_NotFound() {
        Long transcriptId = 1L;
        when(summaryRepo.findByTranscriptId(transcriptId)).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = summarizationService.getSummariesResponse(transcriptId);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testCompareSummaries() {
        Long transcriptId = 1L;
        SummarizationRequestDTO request = new SummarizationRequestDTO();
        request.setTranscriptId(transcriptId);

        Transcript transcript = new Transcript();
        transcript.setId(transcriptId);

        Summary summary = new Summary();
        summary.setTranscript(transcript);
        summary.setSummaryText("Old summary");

        when(summaryRepo.findByTranscriptId(transcriptId)).thenReturn(Collections.singletonList(summary));

        Map<String, Object> result = summarizationService.compareSummaries(request);
        assertNotNull(result);
        assertTrue(result.containsKey("oldSummaries"));
        assertTrue(result.containsKey("newSummaries"));
    }

    @Test
    void testOverwriteSummaries() {
        Long transcriptId = 1L;
        SummarizationResponseDTO newSummary = new SummarizationResponseDTO();
        newSummary.setProviderName("TestProvider");
        newSummary.setSummary("New summary");

        Transcript transcript = new Transcript();
        transcript.setId(transcriptId);

        when(transcriptRepo.findById(transcriptId)).thenReturn(Optional.of(transcript));
        doNothing().when(metricsRepo).deleteByTranscriptId(transcriptId);
        doNothing().when(summaryRepo).deleteByTranscriptId(transcriptId);

        summarizationService.overwriteSummaries(transcriptId, Collections.singletonList(newSummary));

        verify(summaryRepo, times(1)).saveAll(anyList());
    }

    @Test
    void testSummarizeAcrossProviders() throws InterruptedException {
        SummarizationRequestDTO request = new SummarizationRequestDTO();
        request.setTranscriptId(1L);
        request.setContext("Test context");

        SummarizationProvider provider1 = mock(SummarizationProvider.class);
        SummarizationProvider provider2 = mock(SummarizationProvider.class);

        when(provider1.getProviderName()).thenReturn("provider1");
        when(provider2.getProviderName()).thenReturn("provider2");

        SummarizationResponseDTO response1 = new SummarizationResponseDTO("provider1", "Summary 1");
        SummarizationResponseDTO response2 = new SummarizationResponseDTO("provider2", "Summary 2");

        when(provider1.summarize(any(SummarizationRequestDTO.class))).thenReturn(response1);
        when(provider2.summarize(any(SummarizationRequestDTO.class))).thenReturn(response2);

        // Mock the providers list
        List<SummarizationProvider> mockProviders = List.of(provider1, provider2);
        when(providers.stream()).thenReturn(mockProviders.stream());

        CountDownLatch latch = new CountDownLatch(2);

       doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            // Avoid re-submitting the same task to asyncExecutor
            task.run();
            latch.countDown();
            return null;
        }).when(asyncExecutor).execute(any(Runnable.class));

        List<SummarizationResponseDTO> result = summarizationService.summarizeAcrossProviders(request);

        latch.await(); // Wait for all async tasks to complete

        assertEquals(2, result.size());
        assertEquals("Summary 1", result.get(0).getSummary());
        assertEquals("Summary 2", result.get(1).getSummary());
    }

    @Test
    void testBuildProviderPayload() {
        SummarizationRequestDTO request = new SummarizationRequestDTO();
        request.setTranscriptId(1L);
        request.setContext("Test context");

        SummarizationRequestDTO result = summarizationService.buildProviderPayload(request, "anthropic");

        assertEquals("anthropic", result.getProvider());
        assertEquals("claude-3-5-sonnet-20241022", result.getModel());
        assertEquals("Test context", result.getContext());
        assertTrue(result.getParameters().containsKey("max_tokens"));
    }
}
