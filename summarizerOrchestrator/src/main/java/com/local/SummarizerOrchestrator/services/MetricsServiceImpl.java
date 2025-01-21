package com.local.SummarizerOrchestrator.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.SummarizerOrchestrator.dtos.MetricsBatchResponseDTO;
import com.local.SummarizerOrchestrator.dtos.MetricsRequestDTO;
import com.local.SummarizerOrchestrator.dtos.MetricsResponseDTO;
import com.local.SummarizerOrchestrator.models.Metrics;
import com.local.SummarizerOrchestrator.models.ReferenceSummary;
import com.local.SummarizerOrchestrator.models.Summary;
import com.local.SummarizerOrchestrator.models.Transcript;
import com.local.SummarizerOrchestrator.repos.MetricsRepo;
import com.local.SummarizerOrchestrator.repos.SummaryRepo;
import com.local.SummarizerOrchestrator.repos.TranscriptRepo;
import com.local.SummarizerOrchestrator.utils.JSONCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service implementation for calculating and managing metrics for summaries.
 *
 * <p>Handles tasks such as:
 * <ul>
 *     <li>Calculating evaluation metrics for generated summaries using a Python script.</li>
 *     <li>Persisting metrics in the database.</li>
 *     <li>Fetching metrics for summaries.</li>
 * </ul>
 */
@Service
public class MetricsServiceImpl implements MetricsService {

    private static final Logger logger = LoggerFactory.getLogger(MetricsServiceImpl.class);

    private final TranscriptRepo transcriptRepo;
    private final SummaryRepo summaryRepo;
    private final MetricsRepo metricsRepo;
    private final ReferenceSummaryService referenceSummaryService;

    /**
     * Constructs a new instance of {@link MetricsServiceImpl}.
     *
     * @param transcriptRepo         Repository for managing transcripts.
     * @param summaryRepo            Repository for managing summaries.
     * @param metricsRepo            Repository for managing metrics.
     * @param referenceSummaryService Service for managing reference summaries.
     */
    public MetricsServiceImpl(TranscriptRepo transcriptRepo, SummaryRepo summaryRepo, MetricsRepo metricsRepo,
                              ReferenceSummaryService referenceSummaryService) {
        this.transcriptRepo = transcriptRepo;
        this.summaryRepo = summaryRepo;
        this.metricsRepo = metricsRepo;
        this.referenceSummaryService = referenceSummaryService;
    }

    /**
     * Calculates metrics for all summaries associated with a transcript.
     *
     * @param request The request containing the transcript ID and other parameters.
     * @return A {@link MetricsBatchResponseDTO} containing calculated metrics for all summaries.
     */
    @Override
    public MetricsBatchResponseDTO calculateMetricsForTranscript(MetricsRequestDTO request) {
        Transcript transcript = fetchTranscript(request.getTranscriptId());
        ReferenceSummary referenceSummary = referenceSummaryService.getReferenceSummary(request.getTranscriptId());
        String controlSummary = referenceSummary.getSummaryText();

        List<Summary> summaries = summaryRepo.findByTranscriptId(request.getTranscriptId());
        logger.info("Found {} summaries for transcript ID: {}", summaries.size(), request.getTranscriptId());

        List<MetricsResponseDTO> metricsResponses = summaries.parallelStream()
                .map(summary -> safelyCalculateMetrics(summary, controlSummary))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return createBatchResponse(transcript, metricsResponses);
    }

    /**
     * Fetches metrics for a specific summary by its ID.
     *
     * @param summaryId The ID of the summary.
     * @return The {@link Metrics} associated with the given summary ID, or {@code null} if not found.
     */
    @Override
    public Metrics getMetricsBySummaryId(Long summaryId) {
        logger.info("Fetching metrics for summary ID: {}", summaryId);
        return metricsRepo.findBySummaryId(summaryId).orElse(null);
    }

    /**
     * Safely calculates metrics for a summary, handling any exceptions during the process.
     *
     * @param summary       The summary for which metrics are being calculated.
     * @param controlSummary The reference (control) summary for comparison.
     * @return A {@link MetricsResponseDTO} containing the calculated metrics, or {@code null} if an error occurs.
     */
    private MetricsResponseDTO safelyCalculateMetrics(Summary summary, String controlSummary) {
        try {
            return calculateAndStoreMetrics(summary, controlSummary);
        } catch (Exception e) {
            logger.error("Error calculating metrics for summary ID {}: {}", summary.getId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Calculates and stores metrics for a summary.
     *
     * @param summary       The summary for which metrics are being calculated.
     * @param controlSummary The reference (control) summary for comparison.
     * @return A {@link MetricsResponseDTO} containing the calculated metrics.
     * @throws Exception If an error occurs during the calculation process.
     */
    @Transactional
    private MetricsResponseDTO calculateAndStoreMetrics(Summary summary, String controlSummary) throws Exception {
        String candidate = JSONCleaner.cleanGeneratedText(summary.getSummaryText());
        String reference = JSONCleaner.cleanGeneratedText(controlSummary);
        validateInputs(candidate, reference);

        String inputJson = JSONCleaner.sanitizeInputJSON(Map.of("candidate", candidate, "reference", reference));
        String stdout = executePythonScript(inputJson);
        validateJsonOutput(stdout);

        Map<String, Object> metricsMap = parseMetrics(stdout);
        Metrics metrics = saveOrUpdateMetrics(summary, metricsMap);
        return mapMetricsToResponseDTO(summary, metrics);
    }

    /**
     * Creates a batch response containing metrics for all summaries of a transcript.
     *
     * @param transcript         The transcript associated with the summaries.
     * @param metricsResponses The list of metrics responses for each summary.
     * @return A {@link MetricsBatchResponseDTO} containing the batch metrics response.
     */
    private MetricsBatchResponseDTO createBatchResponse(Transcript transcript, List<MetricsResponseDTO> metricsResponses) {
        MetricsBatchResponseDTO batchResponse = new MetricsBatchResponseDTO();
        batchResponse.setTranscriptId(transcript.getId());
        batchResponse.setScenario(transcript.getScenario());
        batchResponse.setSummaryMetrics(metricsResponses);
        return batchResponse;
    }

    /**
     * Saves or updates the metrics for a summary in the database.
     *
     * @param summary    The summary for which metrics are being saved or updated.
     * @param metricsMap The map containing the calculated metrics.
     * @return The saved or updated {@link Metrics} entity.
     */
    private Metrics saveOrUpdateMetrics(Summary summary, Map<String, Object> metricsMap) {
        Metrics metrics = metricsRepo.findBySummaryId(summary.getId()).orElse(new Metrics());

        metrics.setSummary(summary);
        metrics.setTranscript(summary.getTranscript());
        metrics.setRouge1(getFloatValue(metricsMap.get("ROUGE-1")));
        metrics.setRouge2(getFloatValue(metricsMap.get("ROUGE-2")));
        metrics.setRougeL(getFloatValue(metricsMap.get("ROUGE-L")));
        metrics.setBertPrecision(getFloatValue(metricsMap.get("BERT Precision")));
        metrics.setBertRecall(getFloatValue(metricsMap.get("BERT Recall")));
        metrics.setBertF1(getFloatValue(metricsMap.get("BERT F1")));
        metrics.setBleu(getFloatValue(metricsMap.get("BLEU")));
        metrics.setMeteor(getFloatValue(metricsMap.get("METEOR")));
        metrics.setLengthRatio(getFloatValue(metricsMap.get("Length Ratio")));
        metrics.setRedundancy(getFloatValue(metricsMap.get("Redundancy")));
        metrics.setCreatedAt(java.time.LocalDateTime.now());

        return metricsRepo.save(metrics);
    }

    /**
     * Validates the candidate and reference summaries before processing.
     *
     * @param candidate The candidate (generated) summary.
     * @param reference The reference (control) summary.
     * @throws IllegalArgumentException If either the candidate or reference is invalid.
     */
    private void validateInputs(String candidate, String reference) {
        if (candidate.isEmpty() || reference.isEmpty() ||
                candidate.toLowerCase().startsWith("error:") || reference.toLowerCase().startsWith("error:")) {
            throw new IllegalArgumentException("Invalid candidate or reference for metrics calculation.");
        }
    }

    /**
     * Executes a Python script for metrics calculation and retrieves its output.
     *
     * @param inputJson The input JSON string to pass to the Python script.
     * @return The output of the Python script.
     * @throws Exception If an error occurs during script execution or if the script times out.
     */
    private String executePythonScript(String inputJson) throws Exception {
        String pythonPath = System.getenv("PYTHON_PATH");
        String scriptPath = System.getenv("METRICS_SCRIPT_PATH");

        if (pythonPath == null || scriptPath == null) {
            throw new RuntimeException("Python path or script path environment variable is not set.");
        }

        ProcessBuilder pb = new ProcessBuilder(pythonPath, scriptPath, inputJson);
        pb.environment().put("PYTHONUNBUFFERED", "1"); // Ensure real-time output streaming

        Process process = pb.start();
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();

        try (BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader stdErrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            Thread stdOutThread = new Thread(() -> stdOutReader.lines().forEach(stdout::append));
            Thread stdErrThread = new Thread(() -> stdErrReader.lines().forEach(stderr::append));

            stdOutThread.start();
            stdErrThread.start();

            boolean completed = process.waitFor(60, TimeUnit.SECONDS);

            stdOutThread.join();
            stdErrThread.join();

            if (!completed) {
                process.destroyForcibly();
                throw new RuntimeException("Python script timed out.");
            }

            if (process.exitValue() != 0) {
                throw new RuntimeException("Python script failed: " + stderr.toString().trim());
            }

            return stdout.toString().trim();
        }
    }

    /**
     * Validates the output JSON string from the Python script.
     *
     * @param stdout The output JSON string.
     * @throws RuntimeException If the JSON output is invalid.
     */
    private void validateJsonOutput(String stdout) {
        if (!JSONCleaner.isValidJSON(stdout)) {
            throw new RuntimeException("Invalid JSON output from Python script.");
        }
    }

    /**
     * Parses the JSON string from the Python script into a metrics map.
     *
     * @param stdout The JSON string containing the metrics.
     * @return A map of metrics values.
     * @throws Exception If parsing the JSON fails.
     */
    private Map<String, Object> parseMetrics(String stdout) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(stdout, Map.class);
    }

    /**
     * Retrieves the float value from an object.
     *
     * @param value The object to convert.
     * @return The float value, or {@code null} if the value is not a number.
     */
    private Float getFloatValue(Object value) {
        return value instanceof Number ? ((Number) value).floatValue() : null;
    }

    /**
     * Fetches a transcript by its ID from the repository.
     *
     * @param transcriptId The ID of the transcript.
     * @return The {@link Transcript} entity.
     * @throws IllegalArgumentException If the transcript is not found.
     */
    private Transcript fetchTranscript(Long transcriptId) {
        return transcriptRepo.findById(transcriptId)
                .orElseThrow(() -> new IllegalArgumentException("Transcript not found for ID: " + transcriptId));
    }

    /**
     * Maps a metrics entity to a metrics response DTO.
     *
     * @param summary The summary associated with the metrics.
     * @param metrics The metrics entity to map.
     * @return The mapped {@link MetricsResponseDTO}.
     */
    private MetricsResponseDTO mapMetricsToResponseDTO(Summary summary, Metrics metrics) {
        MetricsResponseDTO responseDTO = new MetricsResponseDTO();
        responseDTO.setSummaryId(summary.getId());
        responseDTO.setProviderName(summary.getProviderName());
        responseDTO.setRouge1(metrics.getRouge1());
        responseDTO.setRouge2(metrics.getRouge2());
        responseDTO.setRougeL(metrics.getRougeL());
        responseDTO.setBertPrecision(metrics.getBertPrecision());
        responseDTO.setBertRecall(metrics.getBertRecall());
        responseDTO.setBertF1(metrics.getBertF1());
        responseDTO.setBleu(metrics.getBleu());
        responseDTO.setMeteor(metrics.getMeteor());
        responseDTO.setLengthRatio(metrics.getLengthRatio());
        responseDTO.setRedundancy(metrics.getRedundancy());
        return responseDTO;
    }
}
