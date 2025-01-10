package com.local.SummarizerOrchestrator;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;

/**
 * Main entry point for the Summarizer Orchestrator application.
 * This application integrates multiple summarization models and provides RESTful APIs.
 */
@SpringBootApplication
@EnableAsync
public class SummarizerOrchestratorApplication {

	private static final Logger logger = LoggerFactory.getLogger(SummarizerOrchestratorApplication.class);

	/**
	 * Main method to launch the Spring Boot application.
	 *
	 * @param args Command-line arguments passed to the application.
	 */
	public static void main(String[] args) throws IOException {
		setGoogleCredentials();
		logGoogleCredentials();

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

	/**
	 * Sets the Google application credentials from the environment variable.
	 */
	private static void setGoogleCredentials() {
		String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
		if (credentialsPath != null) {
			System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", credentialsPath);
		} else {
			logger.error("GOOGLE_APPLICATION_CREDENTIALS environment variable not set.");
		}
	}

	/**
	 * Logs the Google credentials information.
	 *
	 * @throws IOException If an I/O error occurs.
	 */
	private static void logGoogleCredentials() throws IOException {
		GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
		if (credentials instanceof ServiceAccountCredentials) {
			ServiceAccountCredentials sa = (ServiceAccountCredentials) credentials;
			logger.info("Credential Project ID: {}", sa.getProjectId());
			logger.info("Credential Client Email: {}", sa.getClientEmail());
		} else {
			logger.warn("Not using a service account credential, got: {}", credentials.getClass());
		}
	}
}