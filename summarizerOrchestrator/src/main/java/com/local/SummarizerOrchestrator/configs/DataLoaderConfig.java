package com.local.SummarizerOrchestrator.configs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.SummarizerOrchestrator.dtos.TranscriptDTO;
import com.local.SummarizerOrchestrator.services.TranscriptService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Configuration
public class DataLoaderConfig {

    @Value("classpath:/data/combined_data.json")
    private Resource dataResource;

    @Bean
    CommandLineRunner loadData(TranscriptService transcriptService) {
        return args -> {
            ObjectMapper mapper = new ObjectMapper();
            try (InputStream inputStream = dataResource.getInputStream()) {
                List<Map<String, String>> dataList = mapper.readValue(
                        inputStream, new TypeReference<List<Map<String, String>>>() {}
                );

                for (Map<String, String> entry : dataList) {
                    TranscriptDTO dto = new TranscriptDTO();
                    dto.setScenario(entry.get("scenario"));
                    dto.setTranscript(entry.get("transcript"));

                    transcriptService.create(dto);
                }
            }
        };
    }
}
