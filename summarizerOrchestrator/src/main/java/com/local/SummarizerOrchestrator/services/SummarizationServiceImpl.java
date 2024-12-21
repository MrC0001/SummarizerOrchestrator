package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import com.local.SummarizerOrchestrator.providers.SummarizationProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SummarizationServiceImpl implements SummarizationService {

    private final List<SummarizationProvider> providers;

    public SummarizationServiceImpl(List<SummarizationProvider> providers) {
        this.providers = providers;
    }

    @Override
    public List<SummarizationResponseDTO> summarizeAcrossProviders(SummarizationRequestDTO request) {
        List<SummarizationResponseDTO> responses = new ArrayList<>();

        for (SummarizationProvider provider : providers) {
            try {
                SummarizationResponseDTO response = provider.summarize(request);
                responses.add(response);
            } catch (Exception e) {
                SummarizationResponseDTO errorResponse = new SummarizationResponseDTO();
                errorResponse.setProviderName(provider.getProviderName());
                errorResponse.setSummary("Error: " + e.getMessage());
                responses.add(errorResponse);
            }
        }

        return responses;
    }
}
