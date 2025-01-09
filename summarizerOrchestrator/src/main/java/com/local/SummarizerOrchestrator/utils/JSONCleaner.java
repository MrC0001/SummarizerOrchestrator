package com.local.SummarizerOrchestrator.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.SummarizerOrchestrator.dtos.SummarizationRequestDTO;

import java.util.Map;

public class JSONCleaner {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    public static String removeControlCharacters(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\\u0000-\\u001F]", "").trim();
    }

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
