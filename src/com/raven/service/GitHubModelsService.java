package com.raven.service;

import com.raven.config.AIConfig;
import com.raven.model.AIMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * GitHub Models Service Implementation
 * Handles communication with GitHub's AI Models API for Copilot PRO integration
 * 
 * @author RAVEN
 * @version 1.0
 */
public class GitHubModelsService {
    
    private final Gson gson;
    private final String apiToken;
    private final String baseUrl;
    private final String model;
    private final int maxTokens;
    private final int timeoutSeconds;
    
    public GitHubModelsService() {
        this.gson = new Gson();
        this.apiToken = AIConfig.getGitHubApiToken();
        this.baseUrl = AIConfig.getGitHubBaseUrl();
        this.model = AIConfig.getGitHubModel();
        this.maxTokens = AIConfig.getGitHubMaxTokens();
        this.timeoutSeconds = AIConfig.getResponseTimeout();
        
        if (apiToken == null || apiToken.isEmpty()) {
            throw new IllegalStateException("GitHub API token not configured");
        }
    }
    
    /**
     * Send a single message to GitHub Models
     */
    public CompletableFuture<AIMessage> sendMessage(String userMessage) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                long startTime = System.currentTimeMillis();
                
                System.out.println("🐙 Sending message to GitHub Models: " + userMessage.substring(0, Math.min(50, userMessage.length())) + "...");
                
                // Create request payload
                JsonObject requestBody = createRequestBody(userMessage);
                
                // Send HTTP request
                String response = sendHttpRequest(requestBody);
                
                // Parse response
                AIMessage aiMessage = parseResponse(response);
                
                long responseTime = System.currentTimeMillis() - startTime;
                aiMessage.setResponseTimeMs(responseTime);
                aiMessage.setProvider(AIMessage.Provider.GITHUB);
                aiMessage.setModel(model);
                
                System.out.println("✅ GitHub Models response received in " + responseTime + "ms");
                return aiMessage;
                
            } catch (Exception e) {
                System.err.println("❌ GitHub Models service error: " + e.getMessage());
                e.printStackTrace();
                return AIMessage.createError("GitHub Models service temporarily unavailable: " + e.getMessage());
            }
        }).exceptionally(throwable -> {
            System.err.println("⏰ GitHub Models request timeout: " + throwable.getMessage());
            return AIMessage.createError("Request timeout - please try again");
        });
    }
    
    /**
     * Send a conversation to GitHub Models
     */
    public CompletableFuture<AIMessage> sendConversation(List<AIMessage> conversation) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                long startTime = System.currentTimeMillis();
                
                System.out.println("🐙 Sending conversation to GitHub Models (" + conversation.size() + " messages)");
                
                // Create request payload with conversation history
                JsonObject requestBody = createConversationRequestBody(conversation);
                
                // Send HTTP request
                String response = sendHttpRequest(requestBody);
                
                // Parse response
                AIMessage aiMessage = parseResponse(response);
                
                long responseTime = System.currentTimeMillis() - startTime;
                aiMessage.setResponseTimeMs(responseTime);
                aiMessage.setProvider(AIMessage.Provider.GITHUB);
                aiMessage.setModel(model);
                
                System.out.println("✅ GitHub Models conversation response received in " + responseTime + "ms");
                return aiMessage;
                
            } catch (Exception e) {
                System.err.println("❌ GitHub Models conversation error: " + e.getMessage());
                e.printStackTrace();
                return AIMessage.createError("GitHub Models service temporarily unavailable: " + e.getMessage());
            }
        }).exceptionally(throwable -> {
            System.err.println("⏰ GitHub Models conversation timeout: " + throwable.getMessage());
            return AIMessage.createError("Request timeout - please try again");
        });
    }
    
    /**
     * Create request body for single message
     */
    private JsonObject createRequestBody(String userMessage) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.addProperty("max_tokens", maxTokens);
        requestBody.addProperty("temperature", 0.7);
        
        JsonArray messages = new JsonArray();
        
        // Add system prompt
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", AIConfig.getSystemPrompt());
        messages.add(systemMessage);
        
        // Add user message
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userMessage);
        messages.add(userMsg);
        
        requestBody.add("messages", messages);
        
        return requestBody;
    }
    
    /**
     * Create request body for conversation
     */
    private JsonObject createConversationRequestBody(List<AIMessage> conversation) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.addProperty("max_tokens", maxTokens);
        requestBody.addProperty("temperature", 0.7);
        
        JsonArray messages = new JsonArray();
        
        // Add system prompt
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", AIConfig.getSystemPrompt());
        messages.add(systemMessage);
        
        // Add conversation history
        for (AIMessage msg : conversation) {
            if (msg.getRole() != AIMessage.Role.SYSTEM) {
                JsonObject message = new JsonObject();
                message.addProperty("role", msg.getRole().getValue());
                message.addProperty("content", msg.getContent());
                messages.add(message);
            }
        }
        
        requestBody.add("messages", messages);
        
        return requestBody;
    }
    
    /**
     * Send HTTP request to GitHub Models API
     */
    private String sendHttpRequest(JsonObject requestBody) throws IOException {
        URL url = new URL(baseUrl + "/" + model + "/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        // Set request headers
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiToken);
        connection.setDoOutput(true);
        connection.setConnectTimeout(timeoutSeconds * 1000);
        connection.setReadTimeout(timeoutSeconds * 1000);
        
        // Send request body
        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(requestBody.toString());
            writer.flush();
        }
        
        // Read response
        int responseCode = connection.getResponseCode();
        
        if (responseCode == 200) {
            return readResponse(connection.getInputStream());
        } else {
            String errorResponse = readResponse(connection.getErrorStream());
            throw new IOException("GitHub Models API error (HTTP " + responseCode + "): " + errorResponse);
        }
    }
    
    /**
     * Read HTTP response
     */
    private String readResponse(InputStream inputStream) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }
    
    /**
     * Parse GitHub Models API response
     */
    private AIMessage parseResponse(String response) {
        try {
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
            
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices != null && choices.size() > 0) {
                JsonObject choice = choices.get(0).getAsJsonObject();
                JsonObject message = choice.getAsJsonObject("message");
                String content = message.get("content").getAsString();
                
                AIMessage aiMessage = new AIMessage(AIMessage.Role.ASSISTANT, content);
                
                // Extract token usage if available
                JsonObject usage = jsonResponse.getAsJsonObject("usage");
                if (usage != null) {
                    JsonElement totalTokens = usage.get("total_tokens");
                    if (totalTokens != null) {
                        aiMessage.setTokenCount(totalTokens.getAsInt());
                    }
                }
                
                return aiMessage;
            } else {
                throw new RuntimeException("No choices in GitHub Models response");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Failed to parse GitHub Models response: " + e.getMessage());
            System.err.println("Response: " + response);
            throw new RuntimeException("Failed to parse GitHub Models response", e);
        }
    }
    
    /**
     * Test the GitHub Models connection
     */
    public boolean testConnection() {
        try {
            AIMessage response = sendMessage("Hello, this is a test message.").get();
            return !response.isError();
        } catch (Exception e) {
            System.err.println("❌ GitHub Models connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get service status
     */
    public String getServiceStatus() {
        if (testConnection()) {
            return "✅ GitHub Models service is operational";
        } else {
            return "❌ GitHub Models service is not available";
        }
    }
}
