package com.local.SummarizerOrchestrator.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up OpenAPI documentation for the application.
 *
 * <p>This configuration ensures that API documentation is generated and customized with
 * relevant application details such as title, version, description, contact information, and license.</p>
 */
@Configuration
public class OpenAPIConfig {

    /**
     * Configures and customizes the OpenAPI documentation for the application.
     *
     * <p>The documentation includes:
     * <ul>
     *     <li>API title and description</li>
     *     <li>Versioning information</li>
     *     <li>Contact details for support</li>
     *     <li>License details for the API</li>
     * </ul>
     * </p>
     *
     * @return A fully configured {@link OpenAPI} instance.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Orchestrator API")
                        .version("v0.5")
                        .description("Orchestrator API for summarizing text using Vertex AI")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@example.com")
                                .url("https://example.com/support"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
