package org.example.geoscienceragassistant.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    private final Client client;

    private static final String MODEL = "gemini-3.6-flash";

    public GeminiService() {

        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY is not set"
            );
        }

        client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public String generateAnswer(String prompt) {

        int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {

            try {

                System.out.println(
                        "Sending request to Gemini. Attempt "
                                + attempt
                );

                GenerateContentResponse response =
                        client.models.generateContent(
                                MODEL,
                                prompt,
                                null
                        );

                String answer = response.text();

                if (answer == null || answer.isBlank()) {
                    return "Gemini returned an empty answer.";
                }

                System.out.println(
                        "Gemini answer generated successfully."
                );

                return answer;

            } catch (Exception e) {

                System.err.println(
                        "Gemini attempt "
                                + attempt
                                + " failed: "
                                + e.getMessage()
                );

                if (attempt == maxAttempts) {

                    return "Gemini is temporarily unavailable. "
                            + "Please try again in a few seconds.";
                }

                try {

                    long waitTime =
                            (long) Math.pow(2, attempt) * 1000;

                    Thread.sleep(waitTime);

                } catch (InterruptedException interruptedException) {

                    Thread.currentThread().interrupt();

                    return "Gemini request was interrupted.";
                }
            }
        }

        return "Unable to generate an answer.";
    }
}