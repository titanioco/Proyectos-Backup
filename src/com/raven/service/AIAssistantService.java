package com.raven.service;

import com.raven.config.AIConfig;
import com.raven.model.AIMessage;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * AI Assistant Service
 * Main service that coordinates between different AI providers (OpenAI, GitHub)
 * and provides fallback functionality
 * 
 * @author RAVEN
 * @version 1.0
 */
public class AIAssistantService {
    
    private final OpenAIService openAIService;
    private final GitHubModelsService gitHubService;
    private final List<AIMessage> conversationHistory;
    private final int maxHistorySize = 20; // Keep last 20 messages for context
    
    public AIAssistantService() {
        // Initialize services based on configuration
        this.openAIService = AIConfig.isOpenAIEnabled() ? new OpenAIService() : null;
        this.gitHubService = AIConfig.isGitHubEnabled() ? new GitHubModelsService() : null;
        this.conversationHistory = new ArrayList<>();
        
        if (openAIService == null && gitHubService == null) {
            System.err.println("⚠️ No AI services are enabled. Please configure at least one AI provider in ai.properties");
        }
    }
    
    /**
     * Send a message to the AI assistant
     * Uses intelligent provider selection and fallback
     */
    public CompletableFuture<AIMessage> sendMessage(String userMessage) {
        // Add user message to history
        AIMessage userMsg = new AIMessage(AIMessage.Role.USER, userMessage);
        addToHistory(userMsg);
        
        System.out.println("💬 User message: " + userMessage.substring(0, Math.min(100, userMessage.length())) + "...");
        
        return sendWithFallback(userMessage)
            .thenApply(response -> {
                // Add response to history
                addToHistory(response);
                return response;
            });
    }
    
    /**
     * Send message with intelligent provider selection and fallback
     */
    private CompletableFuture<AIMessage> sendWithFallback(String userMessage) {
        // Try primary provider first (OpenAI preferred, then GitHub)
        if (openAIService != null) {
            System.out.println("🎯 Trying OpenAI as primary provider...");
            return openAIService.sendMessage(userMessage)
                .thenCompose(response -> {
                    if (response.isError() && gitHubService != null && AIConfig.isFallbackEnabled()) {
                        System.out.println("🔄 OpenAI failed, falling back to GitHub Models...");
                        return gitHubService.sendMessage(userMessage);
                    }
                    return CompletableFuture.completedFuture(response);
                });
                
        } else if (gitHubService != null) {
            System.out.println("🎯 Using GitHub Models as primary provider...");
            return gitHubService.sendMessage(userMessage);
            
        } else {
            // No services available - return fallback response
            return CompletableFuture.completedFuture(createFallbackResponse(userMessage));
        }
    }
    
    /**
     * Send conversation with context
     * Includes recent conversation history for better responses
     */
    public CompletableFuture<AIMessage> sendConversationMessage(String userMessage) {
        // Add user message to history
        AIMessage userMsg = new AIMessage(AIMessage.Role.USER, userMessage);
        addToHistory(userMsg);
        
        System.out.println("💬 Conversation message with " + conversationHistory.size() + " messages of context");
        
        return sendConversationWithFallback()
            .thenApply(response -> {
                // Add response to history
                addToHistory(response);
                return response;
            });
    }
    
    /**
     * Send conversation with fallback
     */
    private CompletableFuture<AIMessage> sendConversationWithFallback() {
        if (openAIService != null) {
            System.out.println("🎯 Sending conversation to OpenAI...");
            return openAIService.sendConversation(conversationHistory)
                .thenCompose(response -> {
                    if (response.isError() && gitHubService != null && AIConfig.isFallbackEnabled()) {
                        System.out.println("🔄 OpenAI conversation failed, falling back to GitHub Models...");
                        return gitHubService.sendConversation(conversationHistory);
                    }
                    return CompletableFuture.completedFuture(response);
                });
                
        } else if (gitHubService != null) {
            System.out.println("🎯 Sending conversation to GitHub Models...");
            return gitHubService.sendConversation(conversationHistory);
            
        } else {
            // No services available
            String lastUserMessage = getLastUserMessage();
            return CompletableFuture.completedFuture(createFallbackResponse(lastUserMessage));
        }
    }
    
    /**
     * Get specialized academic assistance
     */
    public CompletableFuture<AIMessage> getAcademicHelp(String query, String academicContext) {
        String enhancedQuery = "Academic Context: " + academicContext + "\n\n" +
                              "Student Query: " + query + "\n\n" +
                              "Please provide a detailed academic response with practical examples and citations where appropriate.";
        
        return sendMessage(enhancedQuery);
    }
    
    /**
     * Get project-specific assistance
     */
    public CompletableFuture<AIMessage> getProjectHelp(String projectTitle, String projectDescription, String specificQuestion) {
        String projectQuery = "Project: " + projectTitle + "\n" +
                             "Description: " + projectDescription + "\n\n" +
                             "Question: " + specificQuestion + "\n\n" +
                             "Please provide specific guidance for this project.";
        
        return sendMessage(projectQuery);
    }
    
    /**
     * Get methodology assistance
     */
    public CompletableFuture<AIMessage> getMethodologyHelp(String researchArea, String methodologyQuestion) {
        String methodologyQuery = "Research Area: " + researchArea + "\n" +
                                 "Methodology Question: " + methodologyQuestion + "\n\n" +
                                 "Please provide detailed methodology guidance with step-by-step instructions and best practices.";
        
        return sendMessage(methodologyQuery);
    }
    
    /**
     * Create fallback response when no AI services are available
     */
    private AIMessage createFallbackResponse(String userMessage) {
        String fallbackContent = "I apologize, but the AI assistant is currently unavailable. " +
                               "Here are some suggestions for your query:\n\n" +
                               "• Review your project documentation and notes\n" +
                               "• Consult academic resources and textbooks\n" +
                               "• Reach out to your professors or classmates\n" +
                               "• Check online academic databases and journals\n\n" +
                               "Please try again later when the AI service is restored.";
        
        AIMessage fallbackMessage = new AIMessage(AIMessage.Role.ASSISTANT, fallbackContent);
        fallbackMessage.setProvider(AIMessage.Provider.FALLBACK);
        return fallbackMessage;
    }
    
    /**
     * Add message to conversation history
     */
    private void addToHistory(AIMessage message) {
        conversationHistory.add(message);
        
        // Keep only recent messages to avoid context length limits
        while (conversationHistory.size() > maxHistorySize) {
            conversationHistory.remove(0);
        }
    }
    
    /**
     * Get the last user message from history
     */
    private String getLastUserMessage() {
        for (int i = conversationHistory.size() - 1; i >= 0; i--) {
            AIMessage msg = conversationHistory.get(i);
            if (msg.isUserMessage()) {
                return msg.getContent();
            }
        }
        return "Hello";
    }
    
    /**
     * Clear conversation history
     */
    public void clearHistory() {
        conversationHistory.clear();
        System.out.println("🗑️ Conversation history cleared");
    }
    
    /**
     * Get conversation history
     */
    public List<AIMessage> getConversationHistory() {
        return new ArrayList<>(conversationHistory);
    }
    
    /**
     * Get service status
     */
    public String getServiceStatus() {
        StringBuilder status = new StringBuilder();
        status.append("🤖 AI Assistant Service Status:\n");
        
        if (openAIService != null) {
            status.append("• ").append(openAIService.getServiceStatus()).append("\n");
        } else {
            status.append("• ❌ OpenAI service not configured\n");
        }
        
        if (gitHubService != null) {
            status.append("• ").append(gitHubService.getServiceStatus()).append("\n");
        } else {
            status.append("• ❌ GitHub Models service not configured\n");
        }
        
        status.append("• 🔄 Fallback enabled: ").append(AIConfig.isFallbackEnabled()).append("\n");
        status.append("• 💬 Messages in history: ").append(conversationHistory.size());
        
        return status.toString();
    }
    
    /**
     * Test all configured services
     */
    public boolean testAllServices() {
        boolean allWorking = true;
        
        if (openAIService != null) {
            allWorking &= openAIService.testConnection();
        }
        
        if (gitHubService != null) {
            allWorking &= gitHubService.testConnection();
        }
        
        return allWorking;
    }
}
