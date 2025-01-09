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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.local.SummarizerOrchestrator.utils.JSONCleaner;

@Service
public class MetricsServiceImpl implements MetricsService {

    private static final Logger logger = LoggerFactory.getLogger(MetricsServiceImpl.class);

    private final TranscriptRepo transcriptRepo;
    private final SummaryRepo summaryRepo;
    private final MetricsRepo metricsRepo;
    private final ReferenceSummaryService referenceSummaryService;

    public MetricsServiceImpl(TranscriptRepo transcriptRepo, SummaryRepo summaryRepo, MetricsRepo metricsRepo, ReferenceSummaryService referenceSummaryService) {
        this.transcriptRepo = transcriptRepo;
        this.summaryRepo = summaryRepo;
        this.metricsRepo = metricsRepo;
        this.referenceSummaryService = referenceSummaryService;
    }

    @Override
    public MetricsBatchResponseDTO calculateMetricsForTranscript(MetricsRequestDTO request) {
        // Fetch the transcript using the repository
        Transcript transcript = transcriptRepo.findById(request.getTranscriptId())
                .orElseThrow(() -> new IllegalArgumentException("Transcript not found for ID: " + request.getTranscriptId()));

        // Fetch the reference summary using ReferenceSummaryService
        ReferenceSummary referenceSummary = referenceSummaryService.getReferenceSummary(request.getTranscriptId());
        String controlSummary = referenceSummary.getSummaryText();
        logger.info("Retrieved reference summary for transcript ID: {}", request.getTranscriptId());

        // Fetch summaries for the transcript
        List<Summary> summaries = summaryRepo.findByTranscriptId(request.getTranscriptId());
        logger.info("Found {} summaries for transcript ID: {}", summaries.size(), request.getTranscriptId());

        // Calculate metrics for each summary in parallel
        List<MetricsResponseDTO> metricsResponses = summaries.parallelStream().map(summary -> {
            try {
                return calculateAndStoreMetrics(summary, controlSummary);
            } catch (Exception e) {
                logger.error("Error calculating metrics for summary ID {}: {}", summary.getId(), e.getMessage(), e);
                return null; // Skip this summary in case of an error
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());

        // Create the batch response
        MetricsBatchResponseDTO batchResponse = new MetricsBatchResponseDTO();
        batchResponse.setTranscriptId(transcript.getId());
        batchResponse.setScenario(transcript.getScenario());
        batchResponse.setSummaryMetrics(metricsResponses);

        logger.info("Metrics calculation completed for transcript ID: {}", request.getTranscriptId());
        return batchResponse;
    }

    @Override
    public Metrics getMetricsBySummaryId(Long summaryId) {
        logger.info("Fetching metrics for summary ID: {}", summaryId);
        return metricsRepo.findBySummaryId(summaryId).orElse(null);
    }

    private MetricsResponseDTO calculateAndStoreMetrics(Summary summary, String controlSummary) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Validate and sanitize inputs
        String candidate = JSONCleaner.cleanGeneratedText(summary.getSummaryText());
        String reference = JSONCleaner.cleanGeneratedText(controlSummary);
        validateInputs(candidate, reference);

        // Prepare sanitized input JSON
        String inputJson = JSONCleaner.sanitizeInputJSON(Map.of("candidate", candidate, "reference", reference));
        logger.info("Sanitized input JSON for Python script: {}", inputJson);

        // Execute Python script and parse the output
        String stdout = executePythonScript(inputJson);
        validateJsonOutput(stdout);

        // Parse metrics from JSON output
        Map<String, Object> metricsMap = objectMapper.readValue(stdout, Map.class);

        // Save metrics and map to response DTO
        Metrics metrics = saveMetrics(summary, metricsMap);
        return mapMetricsToResponseDTO(summary, metrics);
    }

    private void validateInputs(String candidate, String reference) {
        if (candidate.isEmpty() || reference.isEmpty() ||
                candidate.toLowerCase().startsWith("error:") || reference.toLowerCase().startsWith("error:")) {
            logger.error("Invalid candidate or reference for metrics calculation. Candidate: {}, Reference: {}", candidate, reference);
            throw new IllegalArgumentException("Invalid candidate or reference for metrics calculation.");
        }
    }

    private String executePythonScript(String inputJson) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("C:\\Python312\\python.exe", "scripts/metrics_calculator.py", "\"" + inputJson + "\"");
        logger.info("Executing command: {}", String.join(" ", pb.command()));
        logger.info("Input JSON size: {} bytes", inputJson.getBytes(StandardCharsets.UTF_8).length);

        Process process = pb.start();

        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();

        try (BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader stdErrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            // Read output and error streams asynchronously to avoid blocking
            Thread stdOutThread = new Thread(() -> stdOutReader.lines().forEach(line -> {
                logger.debug("Python script stdout: {}", line);
                stdout.append(line).append("\n");
            }));

            Thread stdErrThread = new Thread(() -> stdErrReader.lines().forEach(line -> {
                logger.error("Python script stderr: {}", line);
                stderr.append(line).append("\n");
            }));

            stdOutThread.start();
            stdErrThread.start();

            boolean completed = process.waitFor(60, TimeUnit.SECONDS); // Timeout after 60 seconds

            // Ensure threads finish
            stdOutThread.join();
            stdErrThread.join();

            if (!completed) {
                process.destroyForcibly();
                throw new RuntimeException("Python script timed out.");
            }

            if (process.exitValue() != 0) {
                throw new RuntimeException("Python script failed with error: " + stderr.toString().trim());
            }

            return stdout.toString().trim();
        } catch (Exception e) {
            logger.error("Error executing Python script: {}", e.getMessage(), e);
            throw e;
        }
    }


    private void validateJsonOutput(String stdout) {
        if (!JSONCleaner.isValidJSON(stdout)) {
            throw new RuntimeException("Invalid JSON output from Python script: " + stdout);
        }
    }

    private Metrics saveMetrics(Summary summary, Map<String, Object> metricsMap) {
        // Check for existing metrics
        Metrics existingMetrics = metricsRepo.findBySummaryId(summary.getId()).orElse(null);

        if (existingMetrics != null) {
            logger.info("Updating existing metrics for summary ID: {}", summary.getId());
            existingMetrics.setRouge1(getFloatValue(metricsMap.get("ROUGE-1")));
            existingMetrics.setRouge2(getFloatValue(metricsMap.get("ROUGE-2")));
            existingMetrics.setRougeL(getFloatValue(metricsMap.get("ROUGE-L")));
            existingMetrics.setBertPrecision(getFloatValue(metricsMap.get("BERT Precision")));
            existingMetrics.setBertRecall(getFloatValue(metricsMap.get("BERT Recall")));
            existingMetrics.setBertF1(getFloatValue(metricsMap.get("BERT F1")));
            existingMetrics.setBleu(getFloatValue(metricsMap.get("BLEU")));
            existingMetrics.setMeteor(getFloatValue(metricsMap.get("METEOR")));
            existingMetrics.setLengthRatio(getFloatValue(metricsMap.get("Length Ratio")));
            existingMetrics.setRedundancy(getFloatValue(metricsMap.get("Redundancy")));
            existingMetrics.setCreatedAt(java.time.LocalDateTime.now());
            return metricsRepo.save(existingMetrics); // Update the existing record
        }

        logger.info("Saving new metrics for summary ID: {}", summary.getId());
        Metrics metrics = new Metrics();
        metrics.setSummary(summary);
        metrics.setTranscript(summary.getTranscript()); // Add transcript ID linkage here
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
        return metricsRepo.save(metrics); // Save a new record
    }


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

    private Float getFloatValue(Object value) {
        return (value instanceof Number) ? ((Number) value).floatValue() : null;
    }

}
