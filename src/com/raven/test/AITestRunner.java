package com.raven.test;

import com.raven.service.AIAssistantService;
import com.raven.model.AIMessage;
import java.util.concurrent.CompletableFuture;

/**
 * Simple AI Test Utility
 * Tests the AI integration with actual API calls
 * 
 * @author RAVEN
 * @version 1.0
 */
public class AITestRunner {
    
    public static void main(String[] args) {
        System.out.println("🚀 Testing AI Integration...");
        System.out.println("=====================================");
        
        try {
            // Initialize AI service
            System.out.println("🔄 Initializing AI services...");
            AIAssistantService aiService = new AIAssistantService();
            
            // Print service status
            System.out.println("\n📊 Service Status:");
            System.out.println(aiService.getServiceStatus());
            
            // Test with a simple academic question
            System.out.println("\n🧪 Testing with academic question...");
            String testQuestion = "What are the key principles of good software design?";
            System.out.println("📝 Question: " + testQuestion);
            
            CompletableFuture<AIMessage> future = aiService.sendMessage(testQuestion);
            
            // Wait for response
            System.out.println("⏳ Waiting for AI response...");
            AIMessage response = future.get();
            
            // Display results
            System.out.println("\n✅ AI Response Received!");
            System.out.println("=====================================");
            System.out.println("🤖 Provider: " + response.getProvider());
            System.out.println("⚡ Response Time: " + response.getResponseTimeMs() + "ms");
            System.out.println("📊 Token Count: " + response.getTokenCount());
            System.out.println("❌ Error: " + response.isError());
            System.out.println("\n💬 Response Content:");
            System.out.println("-------------------------------------");
            System.out.println(response.getContent());
            System.out.println("-------------------------------------");
            
            if (!response.isError()) {
                System.out.println("\n🎉 AI Integration Test: SUCCESS!");
                System.out.println("✅ Your AI assistant is working perfectly!");
            } else {
                System.err.println("\n❌ AI Integration Test: FAILED");
                System.err.println("Error: " + response.getErrorMessage());
            }
            
        } catch (Exception e) {
            System.err.println("\n💥 Test failed with exception:");
            e.printStackTrace();
            
            System.err.println("\n🔧 Troubleshooting Tips:");
            System.err.println("1. Check your internet connection");
            System.err.println("2. Verify API keys in ai.properties");
            System.err.println("3. Check OpenAI/GitHub API status");
            System.err.println("4. Ensure billing is set up for OpenAI");
        }
        
        System.out.println("\n🏁 Test completed.");
    }
}
