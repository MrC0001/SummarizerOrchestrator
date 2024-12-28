package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import com.local.SummarizerOrchestrator.models.Summary;
import com.local.SummarizerOrchestrator.models.Transcript;
import com.local.SummarizerOrchestrator.providers.SummarizationProvider;
import com.local.SummarizerOrchestrator.repos.SummaryRepo;
import com.local.SummarizerOrchestrator.repos.TranscriptRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SummarizationServiceImpl implements SummarizationService {

    private final SummaryRepo summaryRepo;
    private final TranscriptRepo transcriptRepo;
    private final List<SummarizationProvider> providers;

    public SummarizationServiceImpl(SummaryRepo summaryRepo, TranscriptRepo transcriptRepo, List<SummarizationProvider> providers) {
        this.summaryRepo = summaryRepo;
        this.transcriptRepo = transcriptRepo;
        this.providers = providers;
    }

    @Override
    public boolean summariesExistForTranscript(Long transcriptId) {
        return summaryRepo.existsByTranscriptId(transcriptId);
    }

    @Override
    public List<SummarizationResponseDTO> summarizeAndSave(SummarizationRequestDTO request) {
        Transcript transcript = transcriptRepo.findById(request.getTranscriptId())
                .orElseThrow(() -> new RuntimeException("Transcript not found"));

        System.out.println("Transcript found for summarization: " + transcript.getId());

        List<SummarizationResponseDTO> responses = summarizeAcrossProviders(request);

        System.out.println("Generated summaries:");
        responses.forEach(response -> System.out.println(response.getProviderName() + ": " + response.getSummary()));

        responses.stream()
                .filter(response -> !response.getSummary().startsWith("Error")) // Ignore error summaries
                .forEach(response -> {
                    Summary summary = new Summary();
                    summary.setTranscript(transcript);
                    summary.setProviderName(response.getProviderName());
                    summary.setSummaryText(response.getSummary());
                    summaryRepo.save(summary);

                    System.out.println("Saved summary: " + summary.getSummaryText());
                });

        return responses; // Return generated summaries
    }



    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getSummariesResponse(Long transcriptId) {
        List<Summary> summaries = summaryRepo.findByTranscriptId(transcriptId);

        if (summaries.isEmpty()) {
            return ResponseEntity.status(404).body("No summaries found for the given transcript.");
        }

        return ResponseEntity.ok(summaries);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> compareSummaries(SummarizationRequestDTO request) {
        List<Summary> oldSummaries = summaryRepo.findByTranscriptId(request.getTranscriptId());
        List<SummarizationResponseDTO> newSummaries = summarizeAcrossProviders(request);

        System.out.println("Old Summaries:");
        oldSummaries.forEach(summary -> System.out.println(summary.getProviderName() + ": " + summary.getSummaryText()));

        System.out.println("New Summaries:");
        newSummaries.forEach(summary -> System.out.println(summary.getProviderName() + ": " + summary.getSummary()));

        Map<String, Object> result = new HashMap<>();
        result.put("oldSummaries", oldSummaries.stream()
                .map(summary -> new SummarizationResponseDTO(summary.getProviderName(), summary.getSummaryText()))
                .collect(Collectors.toList()));
        result.put("newSummaries", newSummaries);

        return result;
    }


    @Override
    @Transactional
    public void overwriteSummaries(Long transcriptId, List<SummarizationResponseDTO> newSummaries) {
        Transcript transcript = transcriptRepo.findById(transcriptId)
                .orElseThrow(() -> new RuntimeException("Transcript not found"));

        System.out.println("Found transcript for overwrite: " + transcript.getId());

        // Fetch existing summaries for debugging
        List<Summary> oldSummaries = summaryRepo.findByTranscriptId(transcriptId);
        oldSummaries.forEach(summary ->
                System.out.println("Existing summary to delete: " + summary.getProviderName() + " - " + summary.getSummaryText())
        );

        // Delete old summaries
        System.out.println("Deleting old summaries for transcript ID: " + transcriptId);
        summaryRepo.deleteByTranscriptId(transcriptId); // Ensure this executes properly
        System.out.println("Old summaries deleted for transcript ID: " + transcriptId);

        // Insert new summaries
        List<Summary> summariesToSave = newSummaries.stream()
                .map(dto -> {
                    Summary summary = new Summary();
                    summary.setTranscript(transcript);
                    summary.setProviderName(dto.getProviderName());
                    summary.setSummaryText(dto.getSummary());
                    return summary;
                }).collect(Collectors.toList());

        summaryRepo.saveAll(summariesToSave);
        System.out.println("New summaries saved for transcript ID: " + transcriptId);
    }




    private List<SummarizationResponseDTO> summarizeAcrossProviders(SummarizationRequestDTO request) {
        return providers.stream()
                .map(provider -> {
                    try {
                        return provider.summarize(request);
                    } catch (Exception e) {
                        return new SummarizationResponseDTO(provider.getProviderName(), "Error: " + e.getMessage());
                    }
                }).collect(Collectors.toList());
    }
}
