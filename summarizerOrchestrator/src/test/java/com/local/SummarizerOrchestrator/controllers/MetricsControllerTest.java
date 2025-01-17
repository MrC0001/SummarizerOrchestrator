package com.local.SummarizerOrchestrator.controllers;

import com.local.SummarizerOrchestrator.dtos.MetricsBatchResponseDTO;
import com.local.SummarizerOrchestrator.dtos.MetricsRequestDTO;
import com.local.SummarizerOrchestrator.models.ReferenceSummary;
import com.local.SummarizerOrchestrator.services.MetricsService;
import com.local.SummarizerOrchestrator.services.ReferenceSummaryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MetricsController.class)
class MetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MetricsService metricsService;

    @MockBean
    private ReferenceSummaryService referenceSummaryService;

    @Test
    void testCalculateMetrics() throws Exception {
        MetricsBatchResponseDTO responseDTO = new MetricsBatchResponseDTO();
        Mockito.when(metricsService.calculateMetricsForTranscript(any(MetricsRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transcriptId\": 1, \"controlSummary\": \"This is a control summary.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testGetControlSummary() throws Exception {
        ReferenceSummary referenceSummary = new ReferenceSummary();
        referenceSummary.setSummaryText("This is a control summary.");

        Mockito.when(referenceSummaryService.getReferenceSummary(anyLong()))
                .thenReturn(referenceSummary);

        mockMvc.perform(get("/api/metrics/control-summary/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summaryText").value("This is a control summary."));
    }

    @Test
    void testGetControlSummary_NotFound() throws Exception {
        Mockito.when(referenceSummaryService.getReferenceSummary(anyLong()))
                .thenThrow(new RuntimeException("Control summary not found"));

        mockMvc.perform(get("/api/metrics/control-summary/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Control summary not found for transcript ID: 1"));
    }
}