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

    public DataLoaderConfig(TranscriptRepo transcriptRepo, ReferenceSummaryRepo referenceSummaryRepo) {
        this.transcriptRepo = transcriptRepo;
        this.referenceSummaryRepo = referenceSummaryRepo;
    }

    /**
     * Loads initial data into both transcripts and reference_summaries tables.
     *
     * @return CommandLineRunner to execute during application startup.
     */
    @Bean
    @Transactional
    CommandLineRunner loadData() {
        return args -> {
            loadTranscripts();
            loadReferenceSummaries();
        };
    }

    private void loadTranscripts() {
        logger.info("Starting transcript data loading process...");
        if (transcriptRepo.count() == 0) {
            logger.info("Transcript table is empty. Loading initial data...");
            ObjectMapper mapper = new ObjectMapper();
            try (InputStream inputStream = transcriptDataResource.getInputStream()) {
                List<Map<String, String>> dataList = mapper.readValue(
                        inputStream, new TypeReference<List<Map<String, String>>>() {
                        }
                );

                for (Map<String, String> entry : dataList) {
                    Transcript transcript = new Transcript();
                    transcript.setScenario(entry.get("scenario"));
                    transcript.setTranscript(entry.get("transcript"));
                    transcriptRepo.save(transcript);
                    logger.debug("Saved transcript: {}", transcript);
                }
                logger.info("Transcript data loaded successfully.");
            } catch (Exception e) {
                logger.error("Error loading transcript data", e);
                throw new RuntimeException("Error loading transcript data", e);
            }
        } else {
            logger.info("Transcript data already exists. Skipping data load.");
        }
    }

    private void loadReferenceSummaries() {
        logger.info("Starting reference summary data loading process...");
        if (referenceSummaryRepo.count() == 0) {
            logger.info("Reference summaries table is empty. Loading initial data...");
            ObjectMapper mapper = new ObjectMapper();
            try (InputStream inputStream = referenceSummaryDataResource.getInputStream()) {
                List<Map<String, Object>> dataList = mapper.readValue(
                        inputStream, new TypeReference<List<Map<String, Object>>>() {
                        }
                );

                for (Map<String, Object> entry : dataList) {
                    long transcriptId = ((Number) entry.get("transcriptId")).longValue();
                    Transcript transcript = transcriptRepo.findById(transcriptId)
                            .orElseThrow(() -> new IllegalArgumentException("Transcript not found for ID: " + transcriptId));

                    ReferenceSummary referenceSummary = new ReferenceSummary();
                    referenceSummary.setTranscript(transcript);
                    referenceSummary.setSummaryText((String) entry.get("summaryText"));
                    referenceSummaryRepo.save(referenceSummary);
                    logger.debug("Saved reference summary: {}", referenceSummary);
                }
                logger.info("Reference summary data loaded successfully.");
            } catch (Exception e) {
                logger.error("Error loading reference summary data", e);
                throw new RuntimeException("Error loading reference summary data", e);
            }
        } else {
            logger.info("Reference summary data already exists. Skipping data load.");
        }
    }
}