package com.local.SummarizerOrchestrator.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customSwagger() {
        return new OpenAPI()
                .info(new Info()
                        .title("Orchestrator API")
                        .version("v0.5")
                        .description("Orchestrator API for summarizing text using Vertex AI"));
    }
}