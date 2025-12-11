package com.raven.service;

import com.raven.config.AIConfig;
import com.raven.model.User;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Enhanced AI Service with OpenAI and GitHub Models integration
 * Supports text, audio, and video processing
 */
public class AIService {
    private static AIService instance;
    private final String openaiApiKey;
    private final String githubApiToken;
    private final String systemPrompt;
    
    private AIService() {
        this.openaiApiKey = AIConfig.getOpenAIApiKey();
        this.githubApiToken = AIConfig.getGitHubApiToken();
        this.systemPrompt = AIConfig.getSystemPrompt();
        
        System.out.println("AI SERVICE: Initializing with OpenAI: " + AIConfig.isOpenAIConfigured() + 
                          ", GitHub: " + AIConfig.isGitHubConfigured());
    }
    
    public static synchronized AIService getInstance() {
        if (instance == null) {
            instance = new AIService();
        }
        return instance;
    }
    
    /**
     * Send text message to AI assistant
     */
    public CompletableFuture<String> sendMessage(String message, User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("AI SERVICE: Processing message from " + user.getEmail());
                
                // Prefer OpenAI if configured, fallback to GitHub
                if (AIConfig.isOpenAIConfigured()) {
                    return sendToOpenAI(message, user);
                } else if (AIConfig.isGitHubConfigured()) {
                    return sendToGitHub(message, user);
                } else {
                    throw new RuntimeException("No AI service configured. Please check ai.properties");
                }
                
            } catch (Exception e) {
                System.err.println("AI SERVICE ERROR: " + e.getMessage());
                e.printStackTrace();
                return "I apologize, but I'm experiencing technical difficulties. Please try again later.\n\nError: " + e.getMessage();
            }
        });
    }
    
    /**
     * Process audio file with AI
     */
    public CompletableFuture<String> processAudio(File audioFile, User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("AI SERVICE: Processing audio file for " + user.getEmail());
                
                if (!AIConfig.isOpenAIConfigured()) {
                    throw new RuntimeException("Audio processing requires OpenAI configuration");
                }
                
                // First transcribe audio to text
                String transcription = transcribeAudio(audioFile);
                
                // Then process the transcription
                return sendToOpenAI("Please analyze this transcribed audio content: " + transcription, user);
                
            } catch (Exception e) {
                System.err.println("AI AUDIO ERROR: " + e.getMessage());
                e.printStackTrace();
                return "Failed to process audio file: " + e.getMessage();
            }
        }); // Audio processing
    }
    
    /**
     * Process video file with AI
     */
    public CompletableFuture<String> processVideo(File videoFile, User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("AI SERVICE: Processing video file for " + user.getEmail());
                
                if (!AIConfig.isOpenAIConfigured()) {
                    throw new RuntimeException("Video processing requires OpenAI configuration");
                }
                
                // Extract frames and analyze (simplified implementation)
                String analysis = analyzeVideo(videoFile);
                
                return "Video Analysis Results:\n" + analysis;
                
            } catch (Exception e) {
                System.err.println("AI VIDEO ERROR: " + e.getMessage());
                e.printStackTrace();
                return "Failed to process video file: " + e.getMessage();
            }
        }); // Video processing
    }
    
    private String sendToOpenAI(String message, User user) throws Exception {
        URL url = new URL(AIConfig.getOpenAIBaseUrl() + "/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + openaiApiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        
        String jsonPayload = String.format(
            "{\n" +
            "  \"model\": \"%s\",\n" +
            "  \"messages\": [\n" +
            "    {\"role\": \"system\", \"content\": \"%s\"},\n" +
            "    {\"role\": \"user\", \"content\": \"%s\"}\n" +
            "  ],\n" +
            "  \"max_tokens\": %d,\n" +
            "  \"temperature\": %.1f\n" +
            "}",
            AIConfig.getOpenAIModel(),
            escapeJson(systemPrompt + " User: " + user.getEmail()),
            escapeJson(message),
            AIConfig.getOpenAIMaxTokens(),
            AIConfig.getOpenAITemperature()
        );
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
        }
        
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                return extractContentFromOpenAIResponse(response.toString());
            }
        } else {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    error.append(line);
                }
                throw new RuntimeException("OpenAI API Error (" + responseCode + "): " + error.toString());
            }
        }
    }
    
    private String sendToGitHub(String message, User user) throws Exception {
        URL url = new URL(AIConfig.getGitHubBaseUrl() + "/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + githubApiToken);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        
        String jsonPayload = String.format(
            "{\n" +
            "  \"model\": \"%s\",\n" +
            "  \"messages\": [\n" +
            "    {\"role\": \"system\", \"content\": \"%s\"},\n" +
            "    {\"role\": \"user\", \"content\": \"%s\"}\n" +
            "  ],\n" +
            "  \"max_tokens\": %d\n" +
            "}",
            AIConfig.getGitHubModel(),
            escapeJson(systemPrompt + " User: " + user.getEmail()),
            escapeJson(message),
            AIConfig.getGitHubMaxTokens()
        );
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
        }
        
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                return extractContentFromOpenAIResponse(response.toString()); // Same format
            }
        } else {
            throw new RuntimeException("GitHub Models API Error (" + responseCode + ")");
        }
    }
    
    private String transcribeAudio(File audioFile) throws Exception {
        // OpenAI Whisper API for audio transcription
        URL url = new URL(AIConfig.getOpenAIBaseUrl() + "/audio/transcriptions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + openaiApiKey);
        conn.setDoOutput(true);
        
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        
        try (OutputStream os = conn.getOutputStream()) {
            // Model parameter
            os.write(("--" + boundary + "\r\n").getBytes());
            os.write("Content-Disposition: form-data; name=\"model\"\r\n\r\n".getBytes());
            os.write("whisper-1\r\n".getBytes());
            
            // File parameter
            os.write(("--" + boundary + "\r\n").getBytes());
            os.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + audioFile.getName() + "\"\r\n").getBytes());
            os.write("Content-Type: audio/mpeg\r\n\r\n".getBytes());
            
            // File content
            try (FileInputStream fis = new FileInputStream(audioFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
            
            os.write(("\r\n--" + boundary + "--\r\n").getBytes());
        }
        
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                return extractTranscriptionFromResponse(response.toString());
            }
        } else {
            throw new RuntimeException("Audio transcription failed (" + responseCode + ")");
        }
    }
    
    private String analyzeVideo(File videoFile) throws Exception {
        // Simplified video analysis - in production, this would extract frames and analyze them
        return "Video file analyzed: " + videoFile.getName() + 
               "\nDuration: ~" + (videoFile.length() / 1024 / 1024) + " MB" +
               "\nThis is a placeholder for video analysis. Full implementation would require video processing libraries.";
    }
    
    private String extractContentFromOpenAIResponse(String jsonResponse) {
        try {
            // Simple JSON parsing for content - in production use a proper JSON parser
            String pattern = "\"content\"\\s*:\\s*\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"";
            Pattern p = Pattern.compile(pattern);
            java.util.regex.Matcher matcher = p.matcher(jsonResponse);
            
            if (matcher.find()) {
                return matcher.group(1).replace("\\n", "\n").replace("\\\"", "\"");
            }
            
            return "I received your message but couldn't parse the response properly.";
        } catch (Exception e) {
            return "Response parsing error: " + e.getMessage();
        }
    }
    
    private String extractTranscriptionFromResponse(String jsonResponse) {
        try {
            String pattern = "\"text\"\\s*:\\s*\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"";
            Pattern p = Pattern.compile(pattern);
            java.util.regex.Matcher matcher = p.matcher(jsonResponse);
            
            if (matcher.find()) {
                return matcher.group(1);
            }
            
            return "Transcription not available";
        } catch (Exception e) {
            return "Transcription error: " + e.getMessage();
        }
    }
    
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Check if AI service is available
     */
    public boolean isAvailable() {
        return AIConfig.isOpenAIConfigured() || AIConfig.isGitHubConfigured();
    }
    
    /**
     * Get service status
     */
    public String getServiceStatus() {
        if (AIConfig.isOpenAIConfigured() && AIConfig.isGitHubConfigured()) {
            return "Both OpenAI and GitHub Models available";
        } else if (AIConfig.isOpenAIConfigured()) {
            return "OpenAI available";
        } else if (AIConfig.isGitHubConfigured()) {
            return "GitHub Models available";
        } else {
            return "No AI service configured";
        }
    }
}
