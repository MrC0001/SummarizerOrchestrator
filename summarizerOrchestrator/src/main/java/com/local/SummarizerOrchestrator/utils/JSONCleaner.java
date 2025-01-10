package com.local.SummarizerOrchestrator.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;

import java.util.Map;

/**
 * Utility class for cleaning and processing JSON data.
 */
public class JSONCleaner {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Cleans the generated text by removing control characters and replacing escape sequences.
     *
     * @param generatedText The generated text to clean.
     * @return The cleaned text.
     */
    public static String cleanGeneratedText(String generatedText) {
        if (generatedText == null) {
            return "";
        }
        return removeControlCharacters(generatedText)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .trim();
    }

    /**
     * Extracts the generated text from a JSON response.
     *
     * @param jsonResponse The JSON response containing the generated text.
     * @return The extracted generated text, or an empty string if not found.
     */
    public static String extractGeneratedText(String jsonResponse) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(jsonResponse);
            if (root.isArray() && root.size() > 0) {
                JsonNode firstElement = root.get(0);
                JsonNode generatedTextNode = firstElement.get("generated_text");
                if (generatedTextNode != null) {
                    return generatedTextNode.asText();
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }
        return "";
    }

    /**
     * Formats a summary by removing control characters and replacing escape sequences.
     *
     * @param summary The summary to format.
     * @return The formatted summary.
     */
    public static String formatSummary(String summary) {
        if (summary == null) {
            return "";
        }
        return removeControlCharacters(summary)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("'", "\"")
                .replaceAll("[^a-zA-Z0-9.,!?\\s]", " ")
                .trim();
    }

    /**
     * Extracts a specific field from a JSON response.
     *
     * @param jsonResponse The JSON response.
     * @param fieldName    The name of the field to extract.
     * @return The extracted field value, or an empty string if not found.
     */
    public static String extractField(String jsonResponse, String fieldName) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(jsonResponse);
            JsonNode fieldNode = root.get(fieldName);
            if (fieldNode != null) {
                return fieldNode.asText();
            }
        } catch (Exception e) {
            System.err.println("Error parsing field " + fieldName + ": " + e.getMessage());
        }
        return "";
    }

    /**
     * Removes control characters from a text.
     *
     * @param text The text to clean.
     * @return The cleaned text.
     */
    public static String removeControlCharacters(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\\u0000-\\u001F]", "").trim();
    }

    /**
     * Sanitizes a summarization request by cleaning its prompt and context.
     *
     * @param request The summarization request to sanitize.
     * @return The sanitized summarization request.
     */
    public static SummarizationRequestDTO sanitizeRequest(SummarizationRequestDTO request) {
        if (request == null) {
            return null;
        }

        String sanitizedPrompt = cleanGeneratedText(request.getPrompt());
        String sanitizedContext = cleanGeneratedText(request.getContext());

        request.setPrompt(sanitizedPrompt.isEmpty() ? "Default prompt" : sanitizedPrompt);
        request.setContext(sanitizedContext.isEmpty() ? "Default context" : sanitizedContext);

        if (request.getParameters() != null) {
            Map<String, Object> sanitizedParameters = request.getParameters();
            sanitizedParameters.replaceAll((key, value) ->
                    value instanceof String ? cleanGeneratedText((String) value) : value
            );
            request.setParameters(sanitizedParameters);
        }

        return request;
    }

    /**
     * Validates if a JSON string contains all required fields for metrics output.
     *
     * @param jsonString The JSON string to validate.
     * @return True if the JSON is valid and contains all required fields, false otherwise.
     */
    public static boolean isValidJSON(String jsonString) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(jsonString);

            // Define required fields specific to metrics output
            String[] requiredFields = {
                    "ROUGE-1", "ROUGE-2", "ROUGE-L",
                    "BERT Precision", "BERT Recall", "BERT F1",
                    "BLEU", "METEOR", "Length Ratio", "Redundancy"
            };

            // Check for missing fields
            for (String field : requiredFields) {
                if (!root.has(field)) {
                    System.err.println("Invalid JSON: Missing required field - " + field);
                    return false;
                }
            }
            return true; // JSON is valid and contains all required fields
        } catch (Exception e) {
            System.err.println("Invalid JSON detected: " + jsonString + ". Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Sanitizes input JSON by escaping characters for safe shell execution.
     *
     * @param input The input map to sanitize.
     * @return The sanitized JSON string.
     */
    public static String sanitizeInputJSON(Map<String, String> input) {
        try {
            // Serialize the map to JSON string
            String jsonString = OBJECT_MAPPER.writeValueAsString(input);

            // Escape characters for safe shell execution
            jsonString = jsonString.replace("\\", "\\\\")  // Escape backslashes
                    .replace("\"", "\\\"") // Escape double quotes
                    .replace("\n", "\\n")  // Escape newlines
                    .replace("\r", "\\r"); // Escape carriage returns

            return jsonString;
        } catch (Exception e) {
            throw new RuntimeException("Error serializing sanitized input JSON: " + e.getMessage(), e);
        }
    }
}