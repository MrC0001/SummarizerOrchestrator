package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.dtos.TranscriptDTO;
import com.local.SummarizerOrchestrator.models.Transcript;
import com.local.SummarizerOrchestrator.repos.TranscriptRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TranscriptServiceTest {

    @Mock
    private TranscriptRepo transcriptRepo;

    @InjectMocks
    private TranscriptServiceImpl transcriptService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAll() {
        Transcript transcript1 = new Transcript();
        transcript1.setId(1L);
        transcript1.setScenario("Scenario 1");
        transcript1.setTranscript("Transcript 1");

        Transcript transcript2 = new Transcript();
        transcript2.setId(2L);
        transcript2.setScenario("Scenario 2");
        transcript2.setTranscript("Transcript 2");

        when(transcriptRepo.findAll()).thenReturn(Stream.of(transcript1, transcript2).collect(Collectors.toList()));

        List<TranscriptDTO> result = transcriptService.getAll();
        assertEquals(2, result.size());
        assertEquals("Scenario 1", result.get(0).getScenario());
        assertEquals("Scenario 2", result.get(1).getScenario());
    }

    @Test
    void testGetOne() {
        Long transcriptId = 1L;
        Transcript transcript = new Transcript();
        transcript.setId(transcriptId);
        transcript.setScenario("Scenario 1");
        transcript.setTranscript("Transcript 1");

        when(transcriptRepo.findById(transcriptId)).thenReturn(Optional.of(transcript));

        TranscriptDTO result = transcriptService.getOne(transcriptId);
        assertEquals("Scenario 1", result.getScenario());
        assertEquals("Transcript 1", result.getTranscript());
    }

    @Test
    void testGetOne_NotFound() {
        Long transcriptId = 1L;

        when(transcriptRepo.findById(transcriptId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> transcriptService.getOne(transcriptId));
    }

    @Test
    void testCreate() {
        TranscriptDTO dto = new TranscriptDTO();
        dto.setScenario("New Scenario");
        dto.setTranscript("New Transcript");

        Transcript transcript = new Transcript();
        transcript.setScenario(dto.getScenario());
        transcript.setTranscript(dto.getTranscript());

        when(transcriptRepo.save(any(Transcript.class))).thenReturn(transcript);

        TranscriptDTO result = transcriptService.create(dto);
        assertEquals("New Scenario", result.getScenario());
        assertEquals("New Transcript", result.getTranscript());
    }

    @Test
    void testUpdate() {
        Long transcriptId = 1L;
        TranscriptDTO dto = new TranscriptDTO();
        dto.setScenario("Updated Scenario");
        dto.setTranscript("Updated Transcript");

        Transcript existingTranscript = new Transcript();
        existingTranscript.setId(transcriptId);
        existingTranscript.setScenario("Old Scenario");
        existingTranscript.setTranscript("Old Transcript");

        when(transcriptRepo.findById(transcriptId)).thenReturn(Optional.of(existingTranscript));
        when(transcriptRepo.save(existingTranscript)).thenReturn(existingTranscript);

        TranscriptDTO result = transcriptService.update(transcriptId, dto);
        assertEquals("Updated Scenario", result.getScenario());
        assertEquals("Updated Transcript", result.getTranscript());
    }

    @Test
    void testUpdate_NotFound() {
        Long transcriptId = 1L;
        TranscriptDTO dto = new TranscriptDTO();
        dto.setScenario("Updated Scenario");
        dto.setTranscript("Updated Transcript");

        when(transcriptRepo.findById(transcriptId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> transcriptService.update(transcriptId, dto));
    }

    @Test
    void testDelete() {
        Long transcriptId = 1L;

        when(transcriptRepo.existsById(transcriptId)).thenReturn(true);
        doNothing().when(transcriptRepo).deleteById(transcriptId);

        transcriptService.delete(transcriptId);

        verify(transcriptRepo, times(1)).deleteById(transcriptId);
    }

    @Test
    void testDelete_NotFound() {
        Long transcriptId = 1L;

        when(transcriptRepo.existsById(transcriptId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> transcriptService.delete(transcriptId));
    }
}