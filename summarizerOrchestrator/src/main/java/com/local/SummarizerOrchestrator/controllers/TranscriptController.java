package com.local.SummarizerOrchestrator.controllers;

import com.local.SummarizerOrchestrator.dtos.TranscriptDTO;
import com.local.SummarizerOrchestrator.services.TranscriptService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transcripts")
public class TranscriptController {
    private final TranscriptService transcriptService;

    public TranscriptController(TranscriptService transcriptService) {
        this.transcriptService = transcriptService;
    }

    @GetMapping
    public List<TranscriptDTO> getAll() {
        return transcriptService.getAll();
    }

    @GetMapping("/{id}")
    public TranscriptDTO getOne(@PathVariable Long id) {
        return transcriptService.getOne(id);
    }

    @PostMapping
    public TranscriptDTO create(@RequestBody TranscriptDTO dto) {
        return transcriptService.create(dto);
    }

    @PutMapping("/{id}")
    public TranscriptDTO update(@PathVariable Long id, @RequestBody TranscriptDTO dto) {
        return transcriptService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        transcriptService.delete(id);
    }
}
