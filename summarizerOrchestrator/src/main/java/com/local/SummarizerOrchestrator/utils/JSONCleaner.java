package com.local.SummarizerOrchestrator.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;

import java.util.Map;

public class JSONCleaner {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Cleans a generated text response by replacing escape sequences.
     *
     * @param generatedText The raw response string.
     * @return The cleaned string.
     */
    public static String cleanGeneratedText(String generatedText) {
        return generatedText
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .trim();
    }

    /**
     * Parses the JSON response and extracts the "generated_text" field.
     *
     * @param jsonResponse The raw JSON response as a string.
     * @return The extracted text or an error message if the field is not found.
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
            return "Error parsing JSON: " + e.getMessage();
        }
        return "";
    }

    /**
     * Formats a summary text by normalizing line breaks and ensuring proper indentation.
     *
     * @param summary The raw summary text.
     * @return The formatted summary.
     */
    public static String formatSummary(String summary) {
        return summary
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .trim();
    }

    /**
     * Parses a given JSON string and extracts a specific field as text.
     *
     * @param jsonResponse The raw JSON response as a string.
     * @param fieldName    The name of the field to extract.
     * @return The extracted field value or an error message.
     */
    public static String extractField(String jsonResponse, String fieldName) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(jsonResponse);
            JsonNode fieldNode = root.get(fieldName);
            if (fieldNode != null) {
                return fieldNode.asText();
            }
        } catch (Exception e) {
            return "Error parsing field " + fieldName + ": " + e.getMessage();
        }
        return "";
    }

    /**
     * Removes control characters from the input string.
     *
     * @param text The input string to clean.
     * @return The cleaned string with control characters removed.
     */
    public static String removeControlCharacters(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("[\\u0000-\\u001F]", "").trim();
    }

    /**
     * Cleans the entire SummarizationRequestDTO object.
     *
     * @param request The request DTO to sanitize.
     * @return The sanitized request DTO.
     */
    public static SummarizationRequestDTO sanitizeRequest(SummarizationRequestDTO request) {
        if (request == null) {
            return null;
        }

        // Sanitize prompt and context
        request.setPrompt(cleanGeneratedText(request.getPrompt()));
        request.setContext(cleanGeneratedText(request.getContext()));

        // Sanitize parameters
        if (request.getParameters() != null) {
            Map<String, Object> sanitizedParameters = request.getParameters();
            sanitizedParameters.replaceAll((key, value) ->
                    value instanceof String ? cleanGeneratedText((String) value) : value
            );
            request.setParameters(sanitizedParameters);
        }

        return request;
    }
}
