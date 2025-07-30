package com.raven.test;

import com.raven.service.AIAssistantService;
import com.raven.model.AIMessage;
import java.util.concurrent.CompletableFuture;

/**
 * Comprehensive AI Assistant Test with Academic Focus
 * Tests the AI system with real-world engineering student questions
 */
public class AcademicAITest {
    
    public static void main(String[] args) {
        System.out.println("🎓 Academic AI Assistant Test");
        System.out.println("Testing with engineering student scenarios\n");
        
        AIAssistantService aiService = new AIAssistantService();
        
        // Test the main academic question
        testCollegeImprovementQuestion(aiService);
        
        // Test calculus assistance
        testCalculusHelp(aiService);
        
        // Test Japanese learning assistance
        testJapaneseLearning(aiService);
        
        // Test code mode for engineering
        testEngineeringCodeHelp(aiService);
        
        // Test research mode
        testResearchMode(aiService);
        
        System.out.println("✅ All academic tests completed!");
        System.out.println("\n💡 Recommendation: Use the AI Assistant Frame to interact with these responses and save them for later review!");
    }
    
    private static void testCollegeImprovementQuestion(AIAssistantService aiService) {
        System.out.println("📚 Testing Main Academic Question...");
        
        String question = "How can I get better at college as an engineering student? I struggle with calculus and I want to improve my Japanese. I can read hiragana, katakana, and some 50 or more kanji. Please provide comprehensive advice for both areas.";
        
        String academicPrompt = "As an academic assistant for university students, please provide a scholarly and well-researched response to: " + question;
        
        CompletableFuture<AIMessage> future = aiService.sendMessage(academicPrompt);
        
        future.thenAccept(response -> {
            System.out.println("✅ Main Academic Question Response:");
            System.out.println("Length: " + response.getContent().length() + " characters");
            System.out.println("Preview: " + response.getContent().substring(0, Math.min(200, response.getContent().length())) + "...");
            System.out.println("Contains calculus advice: " + response.getContent().toLowerCase().contains("calculus"));
            System.out.println("Contains Japanese advice: " + response.getContent().toLowerCase().contains("japanese"));
            System.out.println();
        }).exceptionally(throwable -> {
            System.out.println("❌ Main Academic Question Failed: " + throwable.getMessage());
            return null;
        }).join();
    }
    
    private static void testCalculusHelp(AIAssistantService aiService) {
        System.out.println("📐 Testing Calculus Assistance...");
        
        String calculusQuestion = "I'm struggling with calculus as an engineering student. Can you explain the fundamental concepts of derivatives and integrals with practical engineering applications? Please include study strategies and common mistakes to avoid.";
        
        String academicPrompt = "As an academic assistant for university students, please provide a scholarly and well-researched response to: " + calculusQuestion;
        
        CompletableFuture<AIMessage> future = aiService.sendMessage(academicPrompt);
        
        future.thenAccept(response -> {
            System.out.println("✅ Calculus Help Response:");
            System.out.println("Length: " + response.getContent().length() + " characters");
            System.out.println("Contains derivatives: " + response.getContent().toLowerCase().contains("derivative"));
            System.out.println("Contains integrals: " + response.getContent().toLowerCase().contains("integral"));
            System.out.println("Contains engineering examples: " + response.getContent().toLowerCase().contains("engineering"));
            System.out.println();
        }).exceptionally(throwable -> {
            System.out.println("❌ Calculus Help Failed: " + throwable.getMessage());
            return null;
        }).join();
    }
    
    private static void testJapaneseLearning(AIAssistantService aiService) {
        System.out.println("🗾 Testing Japanese Learning Assistance...");
        
        String japaneseQuestion = "I want to improve my Japanese. I can read hiragana, katakana, and about 50+ kanji. What's the best way to progress from this intermediate beginner level? Please suggest study methods, resources, and a learning schedule that works for a busy engineering student.";
        
        String academicPrompt = "As an academic assistant for university students, please provide a scholarly and well-researched response to: " + japaneseQuestion;
        
        CompletableFuture<AIMessage> future = aiService.sendMessage(academicPrompt);
        
        future.thenAccept(response -> {
            System.out.println("✅ Japanese Learning Response:");
            System.out.println("Length: " + response.getContent().length() + " characters");
            System.out.println("Contains kanji advice: " + response.getContent().toLowerCase().contains("kanji"));
            System.out.println("Contains study methods: " + (response.getContent().toLowerCase().contains("study") || response.getContent().toLowerCase().contains("method")));
            System.out.println("Contains resources: " + response.getContent().toLowerCase().contains("resource"));
            System.out.println();
        }).exceptionally(throwable -> {
            System.out.println("❌ Japanese Learning Failed: " + throwable.getMessage());
            return null;
        }).join();
    }
    
    private static void testEngineeringCodeHelp(AIAssistantService aiService) {
        System.out.println("💻 Testing Engineering Code Assistance...");
        
        String codeQuestion = "As an engineering student, I need to write a Java program to calculate derivatives numerically for engineering analysis. Can you help me implement a numerical differentiation algorithm with error handling?";
        
        String codePrompt = "As a programming expert, please provide a detailed technical response with code examples when appropriate for: " + codeQuestion;
        
        CompletableFuture<AIMessage> future = aiService.sendMessage(codePrompt);
        
        future.thenAccept(response -> {
            System.out.println("✅ Engineering Code Response:");
            System.out.println("Length: " + response.getContent().length() + " characters");
            System.out.println("Contains Java code: " + (response.getContent().contains("class") || response.getContent().contains("public")));
            System.out.println("Contains numerical methods: " + response.getContent().toLowerCase().contains("numerical"));
            System.out.println();
        }).exceptionally(throwable -> {
            System.out.println("❌ Engineering Code Failed: " + throwable.getMessage());
            return null;
        }).join();
    }
    
    private static void testResearchMode(AIAssistantService aiService) {
        System.out.println("🔍 Testing Research Mode...");
        
        String researchQuestion = "What are the current trends and challenges in engineering education, particularly regarding international students learning technical subjects in a second language?";
        
        String researchPrompt = "As a research assistant, please provide an in-depth analytical response with multiple perspectives on: " + researchQuestion;
        
        CompletableFuture<AIMessage> future = aiService.sendMessage(researchPrompt);
        
        future.thenAccept(response -> {
            System.out.println("✅ Research Mode Response:");
            System.out.println("Length: " + response.getContent().length() + " characters");
            System.out.println("Contains research insights: " + (response.getContent().toLowerCase().contains("research") || response.getContent().toLowerCase().contains("study")));
            System.out.println("Contains multiple perspectives: " + (response.getContent().toLowerCase().contains("perspective") || response.getContent().toLowerCase().contains("approach")));
            System.out.println();
        }).exceptionally(throwable -> {
            System.out.println("❌ Research Mode Failed: " + throwable.getMessage());
            return null;
        }).join();
    }
}
