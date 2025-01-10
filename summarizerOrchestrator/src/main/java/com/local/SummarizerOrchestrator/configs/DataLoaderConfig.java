package com.local.SummarizerOrchestrator.configs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.SummarizerOrchestrator.models.ReferenceSummary;
import com.local.SummarizerOrchestrator.models.Transcript;
import com.local.SummarizerOrchestrator.repos.ReferenceSummaryRepo;
import com.local.SummarizerOrchestrator.repos.TranscriptRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Configuration for loading initial data into the database on application startup.
 * This class handles data loading for transcripts and reference summaries.
 */
@Configuration
public class DataLoaderConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataLoaderConfig.class);

    @Value("classpath:/data/combined_data.json")
    private Resource transcriptDataResource;

    @Value("classpath:/data/combined_data2.json")
    private Resource referenceSummaryDataResource;

    private final TranscriptRepo transcriptRepo;
    private final ReferenceSummaryRepo referenceSummaryRepo;

    /**
     * Constructor for DataLoaderConfig.
     *
     * @param transcriptRepo         Repository for managing transcript entities.
     * @param referenceSummaryRepo   Repository for managing reference summary entities.
     */
    public DataLoaderConfig(TranscriptRepo transcriptRepo, ReferenceSummaryRepo referenceSummaryRepo) {
        this.transcriptRepo = transcriptRepo;
        this.referenceSummaryRepo = referenceSummaryRepo;
    }

    /**
     * Loads initial data into both transcripts and reference summaries tables.
     * This method executes automatically during application startup.
     *
     * @return A CommandLineRunner instance to execute the data loading process.
     */
    @Bean
    @Transactional
    CommandLineRunner loadData() {
        return args -> {
            loadTranscripts();
            loadReferenceSummaries();
        };
    }

    /**
     * Loads transcripts from a JSON resource file into the database.
     */
    private void loadTranscripts() {
        logger.info("Starting transcript data loading process...");
        if (transcriptRepo.count() == 0) {
            logger.info("Transcript table is empty. Loading initial data...");
            try {
                List<Map<String, String>> transcripts = parseJsonFile(transcriptDataResource, new TypeReference<>() {});
                transcripts.forEach(this::saveTranscript);
                logger.info("Transcript data loaded successfully.");
            } catch (Exception e) {
                logger.error("Error loading transcript data", e);
                throw new RuntimeException("Error loading transcript data", e);
            }
        } else {
            logger.info("Transcript data already exists. Skipping data load.");
        }
    }

    /**
     * Loads reference summaries from a JSON resource file into the database.
     */
    private void loadReferenceSummaries() {
        logger.info("Starting reference summary data loading process...");
        if (referenceSummaryRepo.count() == 0) {
            logger.info("Reference summaries table is empty. Loading initial data...");
            try {
                List<Map<String, Object>> summaries = parseJsonFile(referenceSummaryDataResource, new TypeReference<>() {});
                summaries.forEach(this::saveReferenceSummary);
                logger.info("Reference summary data loaded successfully.");
            } catch (Exception e) {
                logger.error("Error loading reference summary data", e);
                throw new RuntimeException("Error loading reference summary data", e);
            }
        } else {
            logger.info("Reference summary data already exists. Skipping data load.");
        }
    }

    /**
     * Parses a JSON file into a list of maps.
     *
     * @param resource The JSON resource file.
     * @param typeReference The type reference for mapping the JSON data.
     * @param <T> The type of the parsed data.
     * @return A list of mapped objects.
     * @throws Exception If an error occurs during file reading or parsing.
     */
    private <T> List<T> parseJsonFile(Resource resource, TypeReference<List<T>> typeReference) throws Exception {
        try (InputStream inputStream = resource.getInputStream()) {
            return new ObjectMapper().readValue(inputStream, typeReference);
        }
    }

    /**
     * Saves a single transcript to the database.
     *
     * @param entry A map containing transcript data.
     */
    private void saveTranscript(Map<String, String> entry) {
        Transcript transcript = new Transcript();
        transcript.setScenario(entry.get("scenario"));
        transcript.setTranscript(entry.get("transcript"));
        transcriptRepo.save(transcript);
        logger.debug("Saved transcript: {}", transcript);
    }

    /**
     * Saves a single reference summary to the database.
     *
     * @param entry A map containing reference summary data.
     */
    private void saveReferenceSummary(Map<String, Object> entry) {
        long transcriptId = ((Number) entry.get("transcriptId")).longValue();
        Transcript transcript = transcriptRepo.findById(transcriptId)
                .orElseThrow(() -> new IllegalArgumentException("Transcript not found for ID: " + transcriptId));

        ReferenceSummary referenceSummary = new ReferenceSummary();
        referenceSummary.setTranscript(transcript);
        referenceSummary.setSummaryText((String) entry.get("summaryText"));
        referenceSummaryRepo.save(referenceSummary);
        logger.debug("Saved reference summary: {}", referenceSummary);
    }
}
