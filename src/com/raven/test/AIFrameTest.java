package com.raven.test;

import com.raven.service.AIAssistantService;
import com.raven.model.AIMessage;
import java.util.concurrent.CompletableFuture;

/**
 * Test class to verify AI Assistant functionality
 */
public class AIFrameTest {
    
    public static void main(String[] args) {
        System.out.println("🧪 Testing AI Assistant Service Integration...\n");
        
        AIAssistantService aiService = new AIAssistantService();
        
        // Test basic chat functionality
        testBasicChat(aiService);
        
        // Test academic mode
        testAcademicMode(aiService);
        
        // Test code mode
        testCodeMode(aiService);
        
        System.out.println("✅ All tests completed!");
    }
    
    private static void testBasicChat(AIAssistantService aiService) {
        System.out.println("📝 Testing Basic Chat...");
        
        CompletableFuture<AIMessage> future = aiService.sendMessage("Hello, can you help me with my studies?");
        
        future.thenAccept(response -> {
            System.out.println("✅ Basic Chat Response:");
            System.out.println("Role: " + response.getRole());
            System.out.println("Content: " + response.getContent().substring(0, Math.min(100, response.getContent().length())) + "...");
            System.out.println();
        }).exceptionally(throwable -> {
            System.out.println("❌ Basic Chat Failed: " + throwable.getMessage());
            return null;
        }).join();
    }
    
    private static void testAcademicMode(AIAssistantService aiService) {
        System.out.println("📚 Testing Academic Mode...");
        
        String academicPrompt = "As an academic assistant for university students, please provide a scholarly and well-researched response to: What are the key principles of software design patterns?";
        CompletableFuture<AIMessage> future = aiService.sendMessage(academicPrompt);
        
        future.thenAccept(response -> {
            System.out.println("✅ Academic Mode Response:");
            System.out.println("Content length: " + response.getContent().length() + " characters");
            System.out.println("Preview: " + response.getContent().substring(0, Math.min(150, response.getContent().length())) + "...");
            System.out.println();
        }).exceptionally(throwable -> {
            System.out.println("❌ Academic Mode Failed: " + throwable.getMessage());
            return null;
        }).join();
    }
    
    private static void testCodeMode(AIAssistantService aiService) {
        System.out.println("💻 Testing Code Mode...");
        
        String codePrompt = "As a programming expert, please provide a detailed technical response with code examples when appropriate for: How do I implement a binary search tree in Java?";
        CompletableFuture<AIMessage> future = aiService.sendMessage(codePrompt);
        
        future.thenAccept(response -> {
            System.out.println("✅ Code Mode Response:");
            System.out.println("Content length: " + response.getContent().length() + " characters");
            System.out.println("Contains 'class': " + response.getContent().toLowerCase().contains("class"));
            System.out.println("Contains 'public': " + response.getContent().toLowerCase().contains("public"));
            System.out.println();
        }).exceptionally(throwable -> {
            System.out.println("❌ Code Mode Failed: " + throwable.getMessage());
            return null;
        }).join();
    }
}
