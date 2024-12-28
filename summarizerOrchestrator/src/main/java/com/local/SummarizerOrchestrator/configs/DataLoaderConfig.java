package com.local.SummarizerOrchestrator.configs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.SummarizerOrchestrator.dtos.TranscriptDTO;
import com.local.SummarizerOrchestrator.models.Transcript;
import com.local.SummarizerOrchestrator.repos.TranscriptRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Configuration
public class DataLoaderConfig {

    @Value("classpath:/data/combined_data.json")
    private Resource dataResource;

    private final TranscriptRepo transcriptRepo;

    public DataLoaderConfig(TranscriptRepo transcriptRepo) {
        this.transcriptRepo = transcriptRepo;
    }

    @Bean
    @Transactional
    CommandLineRunner loadData() {
        return args -> {
            // Check if the table is empty
            if (transcriptRepo.count() == 0) {
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
                    }

                    System.out.println("Initial data loaded successfully.");
                } catch (Exception e) {
                    throw new RuntimeException("Error loading initial data", e);
                }
            } else {
                System.out.println("Data already exists. Skipping data load.");
            }
        };
    }
}
