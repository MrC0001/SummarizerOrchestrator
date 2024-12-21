package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.dtos.TranscriptDTO;
import com.local.SummarizerOrchestrator.models.Transcript;
import com.local.SummarizerOrchestrator.repos.TranscriptRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TranscriptServiceImpl implements TranscriptService {

    private final TranscriptRepo transcriptRepo;

    public TranscriptServiceImpl(TranscriptRepo transcriptRepo) {
        this.transcriptRepo = transcriptRepo;
    }

    @Override
    public List<TranscriptDTO> getAll() {
        return transcriptRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public TranscriptDTO getOne(Long id) {
        Transcript entity = transcriptRepo.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        return toDTO(entity);
    }

    @Override
    public TranscriptDTO create(TranscriptDTO dto) {
        Transcript entity = new Transcript();
        entity.setScenario(dto.getScenario());
        entity.setTranscript(dto.getTranscript());
        entity = transcriptRepo.save(entity);
        return toDTO(entity);
    }

    @Override
    public TranscriptDTO update(Long id, TranscriptDTO dto) {
        Transcript entity = transcriptRepo.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        entity.setScenario(dto.getScenario());
        entity.setTranscript(dto.getTranscript());
        entity = transcriptRepo.save(entity);
        return toDTO(entity);
    }

    @Override
    public void delete(Long id) {
        transcriptRepo.deleteById(id);
    }

    private TranscriptDTO toDTO(Transcript entity) {
        TranscriptDTO dto = new TranscriptDTO();
        dto.setId(entity.getId());
        dto.setScenario(entity.getScenario());
        dto.setTranscript(entity.getTranscript());
        return dto;
    }
}
