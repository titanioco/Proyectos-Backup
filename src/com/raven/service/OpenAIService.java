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
import java.util.concurrent.TimeUnit;

/**
 * OpenAI Service Implementation
 * Handles communication with OpenAI's GPT API for ChatGPT PRO integration
 * 
 * @author RAVEN
 * @version 1.0
 */
public class OpenAIService {
    
    private final Gson gson;
    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final int maxTokens;
    private final double temperature;
    private final int timeoutSeconds;
    
    public OpenAIService() {
        this.gson = new Gson();
        this.apiKey = AIConfig.getOpenAIApiKey();
        this.baseUrl = AIConfig.getOpenAIBaseUrl();
        this.model = AIConfig.getOpenAIModel();
        this.maxTokens = AIConfig.getOpenAIMaxTokens();
        this.temperature = AIConfig.getOpenAITemperature();
        this.timeoutSeconds = AIConfig.getResponseTimeout();
        
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("OpenAI API key not configured");
        }
    }
    
    /**
     * Send a single message to OpenAI
     */
    public CompletableFuture<AIMessage> sendMessage(String userMessage) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                long startTime = System.currentTimeMillis();
                
                System.out.println("🤖 Sending message to OpenAI: " + userMessage.substring(0, Math.min(50, userMessage.length())) + "...");
                
                // Create request payload
                JsonObject requestBody = createRequestBody(userMessage);
                
                // Send HTTP request
                String response = sendHttpRequest(requestBody);
                
                // Parse response
                AIMessage aiMessage = parseResponse(response);
                
                long responseTime = System.currentTimeMillis() - startTime;
                aiMessage.setResponseTimeMs(responseTime);
                aiMessage.setProvider(AIMessage.Provider.OPENAI);
                aiMessage.setModel(model);
                
                System.out.println("✅ OpenAI response received in " + responseTime + "ms");
                return aiMessage;
                
            } catch (Exception e) {
                System.err.println("❌ OpenAI service error: " + e.getMessage());
                e.printStackTrace();
                return AIMessage.createError("OpenAI service temporarily unavailable: " + e.getMessage());
            }
        }).exceptionally(throwable -> {
            System.err.println("⏰ OpenAI request timeout: " + throwable.getMessage());
            return AIMessage.createError("Request timeout - please try again");
        });
    }
    
    /**
     * Send a conversation to OpenAI
     */
    public CompletableFuture<AIMessage> sendConversation(List<AIMessage> conversation) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                long startTime = System.currentTimeMillis();
                
                System.out.println("🤖 Sending conversation to OpenAI (" + conversation.size() + " messages)");
                
                // Create request payload with conversation history
                JsonObject requestBody = createConversationRequestBody(conversation);
                
                // Send HTTP request
                String response = sendHttpRequest(requestBody);
                
                // Parse response
                AIMessage aiMessage = parseResponse(response);
                
                long responseTime = System.currentTimeMillis() - startTime;
                aiMessage.setResponseTimeMs(responseTime);
                aiMessage.setProvider(AIMessage.Provider.OPENAI);
                aiMessage.setModel(model);
                
                System.out.println("✅ OpenAI conversation response received in " + responseTime + "ms");
                return aiMessage;
                
            } catch (Exception e) {
                System.err.println("❌ OpenAI conversation error: " + e.getMessage());
                e.printStackTrace();
                return AIMessage.createError("OpenAI service temporarily unavailable: " + e.getMessage());
            }
        }).exceptionally(throwable -> {
            System.err.println("⏰ OpenAI conversation timeout: " + throwable.getMessage());
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
        requestBody.addProperty("temperature", temperature);
        
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
        requestBody.addProperty("temperature", temperature);
        
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
     * Send HTTP request to OpenAI API
     */
    private String sendHttpRequest(JsonObject requestBody) throws IOException {
        URL url = new URL(baseUrl + "/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        // Set request headers
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
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
            throw new IOException("OpenAI API error (HTTP " + responseCode + "): " + errorResponse);
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
     * Parse OpenAI API response
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
                throw new RuntimeException("No choices in OpenAI response");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Failed to parse OpenAI response: " + e.getMessage());
            System.err.println("Response: " + response);
            throw new RuntimeException("Failed to parse OpenAI response", e);
        }
    }
    
    /**
     * Test the OpenAI connection
     */
    public boolean testConnection() {
        try {
            AIMessage response = sendMessage("Hello, this is a test message.").get(10, TimeUnit.SECONDS);
            return !response.isError();
        } catch (Exception e) {
            System.err.println("❌ OpenAI connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get service status
     */
    public String getServiceStatus() {
        if (testConnection()) {
            return "✅ OpenAI service is operational";
        } else {
            return "❌ OpenAI service is not available";
        }
    }
}
