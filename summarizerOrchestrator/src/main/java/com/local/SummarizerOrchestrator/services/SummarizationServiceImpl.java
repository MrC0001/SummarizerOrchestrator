package com.local.SummarizerOrchestrator.services;

import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;
import com.local.SummarizerOrchestrator.dtos.SummarizationResponseDTO;
import com.local.SummarizerOrchestrator.models.Summary;
import com.local.SummarizerOrchestrator.models.Transcript;
import com.local.SummarizerOrchestrator.providers.SummarizationProvider;
import com.local.SummarizerOrchestrator.repos.SummaryRepo;
import com.local.SummarizerOrchestrator.repos.TranscriptRepo;
import com.local.SummarizerOrchestrator.repos.MetricsRepo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link SummarizationService} interface.
 * Handles operations related to summarization, including generating, saving, retrieving, and comparing summaries.
 */
@Service
public class SummarizationServiceImpl implements SummarizationService {

    private static final Logger logger = LoggerFactory.getLogger(SummarizationServiceImpl.class);

    private final SummaryRepo summaryRepo;
    private final TranscriptRepo transcriptRepo;
    private final MetricsRepo metricsRepo;
    private final List<SummarizationProvider> providers;
    private final Executor asyncExecutor;

    /**
     * Constructor for SummarizationServiceImpl.
     *
     * @param summaryRepo    Repository for managing summaries.
     * @param transcriptRepo Repository for managing transcripts.
     * @param metricsRepo    Repository for managing metrics.
     * @param providers      List of summarization providers.
     * @param asyncExecutor  Executor for asynchronous operations.
     */
    @Autowired
    public SummarizationServiceImpl(SummaryRepo summaryRepo, TranscriptRepo transcriptRepo, MetricsRepo metricsRepo, List<SummarizationProvider> providers, Executor asyncExecutor) {
        this.summaryRepo = summaryRepo;
        this.transcriptRepo = transcriptRepo;
        this.metricsRepo = metricsRepo;
        this.providers = providers;
        this.asyncExecutor = asyncExecutor;
    }

    /**
     * Checks if summaries exist for a given transcript ID.
     *
     * @param transcriptId The ID of the transcript.
     * @return {@code true} if summaries exist, {@code false} otherwise.
     */
    @Override
    public boolean summariesExistForTranscript(@NotNull(message = "Transcript ID must not be null.") Long transcriptId) {
        logger.info("Checking if summaries exist for transcript ID: {}", transcriptId);
        return summaryRepo.existsByTranscriptId(transcriptId);
    }

    /**
     * Asynchronously processes and saves summarization results.
     *
     * @param request Summarization request data.
     * @return CompletableFuture containing old and new summaries.
     */
    @Override
    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> summarizeAndSaveAsync(@Valid @NotNull SummarizationRequestDTO request) {
        logger.info("Starting async summarization and save for transcript ID: {}", request.getTranscriptId());

        Transcript transcript = transcriptRepo.findById(request.getTranscriptId())
                .orElseThrow(() -> {
                    logger.error("Transcript not found for ID: {}", request.getTranscriptId());
                    return new RuntimeException("Transcript not found");
                });

        logger.debug("Found transcript for summarization: {}", transcript.getId());

        return CompletableFuture.supplyAsync(() -> {
            List<Summary> existingSummaries = summaryRepo.findByTranscriptId(request.getTranscriptId());
            logger.debug("Existing summaries for transcript ID {}: {}", request.getTranscriptId(), existingSummaries);

            List<SummarizationResponseDTO> newSummaries = summarizeAcrossProviders(request);

            newSummaries.stream()
                    .filter(newSummary -> newSummary.getSummary() != null && !newSummary.getSummary().startsWith("Error"))
                    .forEach(newSummary -> {
                        if (existingSummaries.stream()
                                .noneMatch(existing -> existing.getProviderName().equals(newSummary.getProviderName()))) {
                            saveSummary(transcript, newSummary);
                        }
                    });

            List<SummarizationResponseDTO> oldSummaries = existingSummaries.stream()
                    .map(summary -> new SummarizationResponseDTO(summary.getProviderName(), summary.getSummaryText()))
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("oldSummaries", oldSummaries);
            result.put("newSummaries", newSummaries);
            return result;
        });
    }

    /**
     * Helper method to save a summary entity.
     *
     * @param transcript The associated transcript entity.
     * @param response   The summarization response DTO.
     */
    private void saveSummary(Transcript transcript, SummarizationResponseDTO response) {
        Summary summary = new Summary();
        summary.setTranscript(transcript);
        summary.setProviderName(response.getProviderName());
        summary.setSummaryText(response.getSummary());
        summaryRepo.save(summary);
        logger.info("Saved summary for provider: {}", response.getProviderName());
    }

    /**
     * Retrieves summaries for a specific transcript and formats the response.
     *
     * @param transcriptId The ID of the transcript.
     * @return A ResponseEntity containing the summaries or an error message if no summaries are found.
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getSummariesResponse(@NotNull(message = "Transcript ID must not be null.") Long transcriptId) {
        logger.info("Fetching summaries for transcript ID: {}", transcriptId);

        List<Summary> summaries = summaryRepo.findByTranscriptId(transcriptId);
        if (summaries.isEmpty()) {
            logger.warn("No summaries found for transcript ID: {}", transcriptId);
            return ResponseEntity.status(404).body("No summaries found for the given transcript.");
        }

        logger.info("Found {} summaries for transcript ID: {}", summaries.size(), transcriptId);
        return ResponseEntity.ok(summaries);
    }

    /**
     * Compares old and new summaries for a given transcript.
     *
     * @param request The summarization request DTO.
     * @return A map containing old and new summaries for comparison.
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> compareSummaries(@Valid @NotNull SummarizationRequestDTO request) {
        logger.info("Comparing old and new summaries for transcript ID: {}", request.getTranscriptId());

        List<Summary> oldSummaries = summaryRepo.findByTranscriptId(request.getTranscriptId());
        List<SummarizationResponseDTO> newSummaries = summarizeAcrossProviders(request);

        logger.debug("Old Summaries: {}", oldSummaries);
        logger.debug("New Summaries: {}", newSummaries);

        List<SummarizationResponseDTO> oldSummaryDTOs = oldSummaries.stream()
                .map(summary -> new SummarizationResponseDTO(summary.getProviderName(), summary.getSummaryText()))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("oldSummaries", oldSummaryDTOs);
        result.put("newSummaries", newSummaries);

        return result;
    }

    /**
     * Overwrites summaries for a transcript with new data.
     *
     * @param transcriptId ID of the transcript.
     * @param newSummaries New summaries to be saved.
     */
    @Override
    @Transactional
    public void overwriteSummaries(@NotNull(message = "Transcript ID must not be null.") Long transcriptId,
                                   @Valid @NotNull List<SummarizationResponseDTO> newSummaries) {
        logger.info("Overwriting summaries for transcript ID: {}", transcriptId);

        Transcript transcript = transcriptRepo.findById(transcriptId)
                .orElseThrow(() -> {
                    logger.error("Transcript not found for ID: {}", transcriptId);
                    return new RuntimeException("Transcript not found");
                });

        logger.debug("Found transcript for overwrite: {}", transcript.getId());

        logger.info("Deleting metrics for transcript ID: {}", transcriptId);
        metricsRepo.deleteByTranscriptId(transcriptId);

        logger.info("Deleting old summaries for transcript ID: {}", transcriptId);
        summaryRepo.deleteByTranscriptId(transcriptId);

        List<Summary> summariesToSave = newSummaries.stream()
                .map(dto -> {
                    Summary summary = new Summary();
                    summary.setTranscript(transcript);
                    summary.setProviderName(dto.getProviderName());
                    summary.setSummaryText(dto.getSummary());
                    return summary;
                }).collect(Collectors.toList());

        summaryRepo.saveAll(summariesToSave);
        logger.info("Saved new summaries for transcript ID: {}", transcriptId);
    }

    /**
     * Processes summarization requests across providers.
     *
     * @param request Summarization request data.
     * @return List of responses from all providers.
     */
    public List<SummarizationResponseDTO> summarizeAcrossProviders(SummarizationRequestDTO request) {
        logger.info("Distributing summarization request across providers.");

        List<CompletableFuture<SummarizationResponseDTO>> futures = providers.stream()
                .map(provider -> CompletableFuture.supplyAsync(() -> {
                    try {
                        SummarizationRequestDTO providerRequest = buildProviderPayload(request, provider.getProviderName());
                        SummarizationResponseDTO response = provider.summarize(providerRequest);
                        logger.debug("Provider {} response: {}", provider.getProviderName(), response);
                        return response;
                    } catch (Exception e) {
                        logger.error("Error summarizing with provider {}: {}", provider.getProviderName(), e.getMessage(), e);
                        return new SummarizationResponseDTO(provider.getProviderName(), "Error: " + e.getMessage());
                    }
                }, asyncExecutor))
                .collect(Collectors.toList());

        List<SummarizationResponseDTO> responses = futures.stream()
                .map(CompletableFuture::join)
                .filter(response -> response != null && !response.getSummary().isEmpty())
                .collect(Collectors.toList());

        logger.info("Collected summaries from providers: {}", responses);
        return responses;
    }

    /**
     * Builds a provider-specific payload.
     *
     * @param request  The base request.
     * @param provider The provider name.
     * @return A customized request for the provider.
     */
    protected SummarizationRequestDTO buildProviderPayload(SummarizationRequestDTO request, String provider) {
        switch (provider.toLowerCase()) {
            case "anthropic":
                return new SummarizationRequestDTO(
                        request.getTranscriptId(),
                        "anthropic",
                        "claude-3-5-sonnet-20241022",
                        null,
                        request.getContext(),
                        List.of(Map.of("role", "user", "content", "Summarize this text:\n" + request.getContext())),
                        Map.of("max_tokens", 256, "temperature", 0.7, "stream", true),
                        true
                );
            case "mistral":
            case "mistral ai":
                return new SummarizationRequestDTO(
                        request.getTranscriptId(),
                        "mistral",
                        "mistral-large-latest",
                        null,
                        request.getContext(),
                        List.of(
                                Map.of("role", "system", "content", "You are a helpful assistant specialized in summarizing text. Forget prior conversations and summarize each request independently."),
                                Map.of("role", "user", "content", "Summarize this text with as much detail as possible:\n" + request.getContext())
                        ),
                        Map.of(
                                "max_tokens", 512,
                                "temperature", 0.7,
                                "stream", false
                        ),
                        true
                );
            case "huggingface":
            case "hugging face":
                return new SummarizationRequestDTO(
                        request.getTranscriptId(),
                        "huggingface",
                        null,
                        null,
                        null,
                        List.of(
                                Map.of(
                                        "role", "user",
                                        "content", "Summarize this text:\n" + truncateContext(request.getContext(), 128000)
                                )
                        ),
                        Map.of(
                                "parameters", Map.of(
                                        "max_new_tokens", 128,
                                        "temperature", 0.7,
                                        "top_p", 0.9,
                                        "repetition_penalty", 1.0
                                )
                        ),
                        false
                );
            case "vertex":
            case "vertex ai":
                return new SummarizationRequestDTO(
                        request.getTranscriptId(),
                        "vertex",
                        "gemini-1.5-pro-002",
                        "Summarize this text:\n" + request.getContext(),
                        null,
                        null,
                        Map.of("max_output_tokens", 256, "temperature", 0.7, "top_p", 0.9, "top_k", 40),
                        false
                );
            case "vllm":
                return new SummarizationRequestDTO(
                        request.getTranscriptId(),
                        "vllm",
                        "phi-3.5-mini-instruct",
                        null,
                        request.getContext(),
                        List.of(
                                Map.of("role", "system", "content", "You are a helpful assistant."),
                                Map.of("role", "user", "content", "Summarize this text:\n" + request.getContext())
                        ),
                        Map.of(
                                "max_tokens", 512,
                                "temperature", 0.5,
                                "top_p", 0.8
                        ),
                        false
                );
                case "provider1":
                return new SummarizationRequestDTO(
                        request.getTranscriptId(),
                        "provider1",
                        "provider1-model",
                        null,
                        request.getContext(),
                        List.of(Map.of("role", "user", "content", "Summarize this text:\n" + request.getContext())),
                        Map.of("max_tokens", 128, "temperature", 0.3),
                        false
                );
            case "provider2":
                return new SummarizationRequestDTO(
                        request.getTranscriptId(),
                        "provider2",
                        "provider2-model",
                        null,
                        request.getContext(),
                        List.of(Map.of("role", "user", "content", "Summarize this text:\n" + request.getContext())),
                        Map.of("max_tokens", 128, "temperature", 0.3),
                        false
                );
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }

    /**
     * Truncates a context string to the specified maximum length.
     *
     * @param context   The context string.
     * @param maxTokens The maximum allowed tokens.
     * @return The truncated context.
     */
    private String truncateContext(String context, int maxTokens) {
        if (context == null || context.isEmpty()) {
            return "";
        }
        return context.length() > maxTokens ? context.substring(0, maxTokens) : context;
    }
}