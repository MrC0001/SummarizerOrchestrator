package com.local.SummarizerOrchestrator.configs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.SummarizerOrchestrator.models.Transcript;
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
    private Resource dataResource;

    private final TranscriptRepo transcriptRepo;

    public DataLoaderConfig(TranscriptRepo transcriptRepo) {
        this.transcriptRepo = transcriptRepo;
    }

    /**
     * Loads initial data from a JSON file into the database if the transcript table is empty.
     *
     * @return CommandLineRunner to execute during application startup.
     */
    @Bean
    @Transactional
    CommandLineRunner loadData() {
        return args -> {
            logger.info("Starting data loading process...");
            if (transcriptRepo.count() == 0) {
                logger.info("Transcript table is empty. Loading initial data...");
                ObjectMapper mapper = new ObjectMapper();
                try (InputStream inputStream = dataResource.getInputStream()) {
                    List<Map<String, String>> dataList = mapper.readValue(
                            inputStream, new TypeReference<List<Map<String, String>>>() {}
                    );

                    for (Map<String, String> entry : dataList) {
                        Transcript transcript = new Transcript();
                        transcript.setScenario(entry.get("scenario"));
                        transcript.setTranscript(entry.get("transcript"));
                        transcriptRepo.save(transcript);
                        logger.debug("Saved transcript: {}", transcript);
                    }
                    logger.info("Initial data loaded successfully.");
                } catch (Exception e) {
                    logger.error("Error loading initial data", e);
                    throw new RuntimeException("Error loading initial data", e);
                }
            } else {
                logger.info("Data already exists. Skipping data load.");
            }
        };
    }
}
