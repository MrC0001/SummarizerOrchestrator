package com.local.SummarizerOrchestrator.controllers;

import com.local.SummarizerOrchestrator.dtos.TranscriptDTO;
import com.local.SummarizerOrchestrator.services.TranscriptService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TranscriptController.class)
class TranscriptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TranscriptService transcriptService;

    @Test
    void testGetAll() throws Exception {
        Mockito.when(transcriptService.getAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/transcripts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetOne() throws Exception {
        TranscriptDTO transcriptDTO = new TranscriptDTO();
        transcriptDTO.setId(1L); // Set the ID
        Mockito.when(transcriptService.getOne(anyLong())).thenReturn(transcriptDTO);

        mockMvc.perform(get("/api/transcripts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testCreate() throws Exception {
        TranscriptDTO transcriptDTO = new TranscriptDTO();
        transcriptDTO.setId(1L); // Set the ID
        Mockito.when(transcriptService.create(any(TranscriptDTO.class))).thenReturn(transcriptDTO);

        mockMvc.perform(post("/api/transcripts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scenario\": \"Test Scenario\", \"transcript\": \"Test Transcript\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testUpdate() throws Exception {
        TranscriptDTO transcriptDTO = new TranscriptDTO();
        transcriptDTO.setId(1L); // Set the ID
        Mockito.when(transcriptService.update(anyLong(), any(TranscriptDTO.class))).thenReturn(transcriptDTO);

        mockMvc.perform(put("/api/transcripts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scenario\": \"Updated Scenario\", \"transcript\": \"Updated Transcript\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testDelete() throws Exception {
        Mockito.doNothing().when(transcriptService).delete(anyLong());

        mockMvc.perform(delete("/api/transcripts/1"))
                .andExpect(status().isNoContent());
    }
}