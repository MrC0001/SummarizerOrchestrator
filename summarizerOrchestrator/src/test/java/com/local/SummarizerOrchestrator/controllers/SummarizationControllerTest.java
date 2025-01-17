package com.local.SummarizerOrchestrator.controllers;

import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import com.local.SummarizerOrchestrator.models.Transcript;
import com.local.SummarizerOrchestrator.services.SummarizationService;
import com.local.SummarizerOrchestrator.repos.TranscriptRepo;
import com.local.SummarizerOrchestrator.repos.SummaryRepo;
import com.local.SummarizerOrchestrator.repos.MetricsRepo;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SummarizationController.class)
class SummarizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SummarizationService summarizationService;

    @MockBean
    private TranscriptRepo transcriptRepo;

    @MockBean
    private SummaryRepo summaryRepo;

    @MockBean
    private MetricsRepo metricsRepo;

    @Test
    void testSummarizeAll() throws Exception {
        Transcript transcript = new Transcript();
        transcript.setId(1L);
        transcript.setTranscript("This is a test transcript.");

        Mockito.when(transcriptRepo.findById(anyLong())).thenReturn(java.util.Optional.of(transcript));
        Mockito.when(summarizationService.summarizeAndSaveAsync(any(SummarizationRequestDTO.class)))
                .thenReturn(CompletableFuture.completedFuture(Map.of("summary", "This is a test summary")));

        mockMvc.perform(post("/api/summarizeAll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transcriptId\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("This is a test summary"));
    }

    @Test
    void testGetSummariesAndMetricsWithTranscript() throws Exception {
        Transcript transcript = new Transcript();
        transcript.setId(1L);
        transcript.setTranscript("This is a test transcript.");

        Mockito.when(transcriptRepo.findById(anyLong())).thenReturn(java.util.Optional.of(transcript));
        Mockito.when(summaryRepo.findByTranscriptId(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transcript").value("This is a test transcript."))
                .andExpect(jsonPath("$.summaries").isArray());
    }

    @Test
    void testOverwriteSummaries() throws Exception {
        Mockito.doNothing().when(summarizationService).overwriteSummaries(anyLong(), any());

        mockMvc.perform(post("/api/overwrite/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"providerName\": \"TestProvider\", \"summary\": \"This is a test summary.\"}]"))
                .andExpect(status().isOk())
                .andExpect(content().string("Summaries overwritten successfully."));
    }
}