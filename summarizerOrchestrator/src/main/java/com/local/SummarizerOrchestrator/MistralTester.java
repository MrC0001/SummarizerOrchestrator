package com.local.SummarizerOrchestrator;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.auth.oauth2.UserCredentials;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MistralTester {
    private  static final Logger logger = LoggerFactory.getLogger(MistralTester.class);
    public static void main(String[] args) throws Exception {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        if (credentials instanceof ServiceAccountCredentials) {
            ServiceAccountCredentials sa = (ServiceAccountCredentials) credentials;
            System.out.println("Java SA Email: " + sa.getClientEmail());
            System.out.println("Java SA Project: " + sa.getProjectId());
        } else if (credentials instanceof UserCredentials) {
            UserCredentials userCreds = (UserCredentials) credentials;
            System.out.println("Java User Credential: " + userCreds.getClientId());
            System.out.println("Java Quota Project: " + userCreds.getQuotaProjectId());
        } else {
            System.out.println("Other credential type: " + credentials.getClass().getName());

        }
        System.out.println("GOOGLE_APPLICATION_CREDENTIALS: " + System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));


        try (VertexAI vertexAI = new VertexAI("summariser-prototype", "us-central1")) {
            String mistralResource = "projects/summariser-prototype/locations/us-central1/publishers/mistralai/models/mistral-large-2411";
            GenerativeModel model = new GenerativeModel(mistralResource, vertexAI);

            String prompt = "Summarize this text: AI aims to replicate human intelligence in machines.";
            GenerateContentResponse response = model.generateContent(prompt);
            String text = ResponseHandler.getText(response);
            System.out.println("Response: " + text);
        }
    }

}