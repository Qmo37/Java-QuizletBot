package com.aichat;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        // Replace with your API key
        String apiKey = System.getenv("ANTHROPIC_API_KEY");

        // Choose which AI service to use (true for GPT, false for Claude)
        boolean useGPT = true;

        AIClient aiClient = new AIClient(apiKey, useGPT);

        try {
            // Example message
            String response = aiClient.sendMessage(
                "Tell me a joke about programming"
            );
            System.out.println("AI Response: " + response);
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
}
