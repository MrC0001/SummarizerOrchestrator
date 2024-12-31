package com.local.SummarizerOrchestrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Summarizer Orchestrator application.
 * This application integrates multiple summarization models and provides RESTful APIs.
 */
@SpringBootApplication
public class SummarizerOrchestratorApplication {

	private static final Logger logger = LoggerFactory.getLogger(SummarizerOrchestratorApplication.class);

	/**
	 * Main method to launch the Spring Boot application.
	 *
	 * @param args Command-line arguments passed to the application.
	 */
	public static void main(String[] args) {
		logger.info("Starting Summarizer Orchestrator Application...");
		try {
			SpringApplication.run(SummarizerOrchestratorApplication.class, args);
			logger.info("Summarizer Orchestrator Application started successfully.");
		} catch (Exception e) {
			logger.error("Application failed to start due to an exception: {}", e.getMessage(), e);
			throw e; // Rethrow the exception to allow Spring Boot to handle it
		}

		// Graceful shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.info("Summarizer Orchestrator Application is shutting down...");
		}));
	}
}
