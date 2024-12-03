package com.aichat;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class AIClient {

    private final OkHttpClient client;
    private final String apiKey;
    private final String baseUrl;
    private final int maxTokens;
    private final String systemPrompt;
    private static final MediaType JSON = MediaType.get(
        "application/json; charset=utf-8"
    );

    public AIClient(
        String apiKey,
        boolean useGPT,
        int maxTokens,
        String systemPrompt
    ) {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();
        this.apiKey = apiKey; // Use the passed API key instead of environment variable
        this.maxTokens = maxTokens;
        this.systemPrompt = systemPrompt;
        this.baseUrl = useGPT
            ? "https://api.openai.com/v1/chat/completions"
            : "https://api.anthropic.com/v1/messages";
    }

    public AIClient(String apiKey, boolean useGPT) {
        this(
            apiKey,
            useGPT,
            useGPT ? 2048 : 2048,
            "You are a college professor, and you'll use the given text to give a set of 10 yes-or-no questions and answers. Please format your response as a series of questions and answers, " +
            "each starting with 'Q:' and 'A:' respectively. For example:\n" +
            "Q: Question here\n" +
            "A: Answer here\n"
        );
    }

    public String sendMessage(String message) throws IOException {
        JSONObject requestBody = new JSONObject();

        if (baseUrl.contains("openai")) {
            // ChatGPT request format
            requestBody
                .put("model", "gpt-4")
                .put("max_tokens", maxTokens)
                .put("temperature", 0.7) // Higher temperature means more randomness
                // Add system prompt, directly as "system="You are a seasoned data scientist at a Fortune 500 company."" in the JSON
                .put("system", systemPrompt);
            JSONArray messagesArray = new JSONArray();
            messagesArray.put(
                new JSONObject().put("role", "user").put("content", message)
            );
            requestBody.put("messages", messagesArray);
        } else {
            // Claude request format
            requestBody
                .put("model", "claude-3-opus-20240229")
                .put("max_tokens", maxTokens)
                .put("temperature", 0.7) // Higher temperature means more randomness
                // Add system prompt, directly as "system="You are a seasoned data scientist at a Fortune 500 company."" in the JSON
                .put("system", systemPrompt);
            JSONArray messagesArray = new JSONArray();
            messagesArray.put(
                new JSONObject().put("role", "user").put("content", message)
            );
            requestBody.put("messages", messagesArray);
        }

        Request.Builder requestBuilder = new Request.Builder()
            .url(baseUrl)
            .post(RequestBody.create(requestBody.toString(), JSON))
            .addHeader("Content-Type", "application/json");

        // Add appropriate authorization headers based on the service
        if (baseUrl.contains("openai")) {
            requestBuilder.addHeader("Authorization", "Bearer " + apiKey); // OpenAI API key
        } else {
            requestBuilder.addHeader("x-api-key", apiKey); // Claude API key
            requestBuilder.addHeader("anthropic-version", "2023-06-01");
        }

        Request request = requestBuilder.build(); // Build the request

        try (Response response = client.newCall(request).execute()) { // Execute the request
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null
                    ? response.body().string()
                    : "No error body";
                throw new IOException(
                    "Unexpected response " +
                    response +
                    "\nError body: " +
                    errorBody
                );
            }

            JSONObject jsonResponse = new JSONObject(response.body().string()); // Parse JSON response
            String responseText;
            JSONObject tokenUsage;

            if (baseUrl.contains("openai")) {
                // Parse ChatGPT response
                responseText = jsonResponse
                    .getJSONArray("choices") // Get the response from the "choices" array
                    .getJSONObject(0) // Get the first (and only) choice
                    .getJSONObject("message") // Get the message object
                    .getString("content"); // Get the content of the message
                tokenUsage = jsonResponse.getJSONObject("usage"); // Get the token usage object
                return (
                    responseText +
                    "\n\nToken usage - Input: " +
                    tokenUsage.getInt("prompt_tokens") +
                    ", Output: " +
                    tokenUsage.getInt("completion_tokens")
                );
            } else {
                // Parse Claude response
                responseText = jsonResponse
                    .getJSONArray("content") // Get the content array
                    .getJSONObject(0) // Get the first (and only) content object
                    .getString("text"); // Get the text of the content
                tokenUsage = jsonResponse.getJSONObject("usage"); // Get the token usage object
                return (
                    responseText +
                    "\n\nToken usage - Input: " +
                    tokenUsage.getInt("input_tokens") +
                    ", Output: " +
                    tokenUsage.getInt("output_tokens")
                );
            }
        }
    }

    public static int estimateTokenCount(String text) {
        // Rough estimation: average English word is ~1.3 tokens
        // Split by whitespace and punctuation
        String[] words = text.split("[\\s\\p{Punct}]+");
        return (int) Math.ceil(words.length * 1.3);
    }
}
