package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.dtos.TranscriptDTO;
import java.util.List;

public interface TranscriptService {
    List<TranscriptDTO> getAll();
    TranscriptDTO getOne(Long id);
    TranscriptDTO create(TranscriptDTO dto);
    TranscriptDTO update(Long id, TranscriptDTO dto);
    void delete(Long id);
}
