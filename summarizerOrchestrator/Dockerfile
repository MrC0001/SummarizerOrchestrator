# Use the OpenJDK base image for Java
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Add Python dependencies
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

# Install Python packages required for the metrics script
RUN pip3 install --no-cache-dir \
    nltk \
    rouge-score \
    bert-score

# Copy the Java application JAR file
ARG JAR_FILE=target/summarizerOrchestrator-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Copy the Python script to the container
COPY scripts/ ./scripts/

# Set environment variables for the metrics script
ENV PYTHON_PATH=/usr/bin/python3
ENV METRICS_SCRIPT_PATH=/app/scripts/metrics_calculator.py

# Expose the entry point for the Java application
ENTRYPOINT ["java", "-jar", "/app.jar"]
