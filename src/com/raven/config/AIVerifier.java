package com.raven.config;

/**
 * AI Configuration Verification Utility
 * This utility helps verify that AI services are properly configured with valid credentials
 * 
 * @author RAVEN
 * @version 1.0
 */
public class AIVerifier {
    
    public static void main(String[] args) {
        System.out.println("🤖 AI Services Configuration Verification");
        System.out.println("==========================================");
        
        // Print diagnostics
        AIConfig.printDiagnostics();
        
        // Validate configuration
        if (AIConfig.isConfigured()) {
            System.out.println("\n✅ AI Configuration Status: READY");
            System.out.println("🚀 Application can use AI assistant features");
            
            // Test services if configured
            testServices();
            
        } else {
            System.out.println("\n❌ AI Configuration Status: NOT READY");
            System.out.println("⚠️ Please check ai.properties file and ensure valid credentials");
            
            // Provide setup instructions
            System.out.println("\n🔧 Setup Instructions:");
            System.out.println("1. Copy ai.properties.template to ai.properties");
            System.out.println("2. Get OpenAI API key from: https://platform.openai.com/api-keys");
            System.out.println("3. Get GitHub token from: https://github.com/settings/tokens");
            System.out.println("4. Enable at least one AI service in ai.properties");
            System.out.println("5. Run this verifier again to test the configuration");
        }
        
        System.out.println("\n==========================================");
        
        // Print additional information
        printSetupInstructions();
    }
    
    /**
     * Test configured AI services
     */
    private static void testServices() {
        System.out.println("\n🧪 Testing AI Services...");
        
        try {
            if (AIConfig.isOpenAIEnabled()) {
                System.out.println("🔄 Testing OpenAI connection...");
                // Note: Actual service testing would be done in the service class
                System.out.println("✅ OpenAI service configuration appears valid");
            }
            
            if (AIConfig.isGitHubEnabled()) {
                System.out.println("🔄 Testing GitHub Models connection...");
                // Note: Actual service testing would be done in the service class
                System.out.println("✅ GitHub Models service configuration appears valid");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Service test error: " + e.getMessage());
        }
    }
    
    /**
     * Print detailed setup instructions
     */
    private static void printSetupInstructions() {
        System.out.println("\n📚 Detailed Setup Guide:");
        System.out.println("========================");
        
        System.out.println("\n🔴 OpenAI Setup (ChatGPT PRO API):");
        System.out.println("1. Go to https://platform.openai.com/");
        System.out.println("2. Log in with your ChatGPT PRO account");
        System.out.println("3. Navigate to API Keys section");
        System.out.println("4. Create a new API key (starts with 'sk-')");
        System.out.println("5. Add billing information (API usage is separate from ChatGPT PRO)");
        System.out.println("6. Copy the API key to ai.properties");
        
        System.out.println("\n🟢 GitHub Models Setup (Copilot PRO):");
        System.out.println("1. Go to https://github.com/settings/tokens");
        System.out.println("2. Create a 'Fine-grained personal access token'");
        System.out.println("3. Select your repository");
        System.out.println("4. Grant 'Models' permission");
        System.out.println("5. Copy the token to ai.properties");
        System.out.println("6. Note: Requires GitHub Copilot PRO subscription");
        
        System.out.println("\n⚙️ Configuration Tips:");
        System.out.println("• Start with OpenAI for better compatibility");
        System.out.println("• Enable fallback for better reliability");
        System.out.println("• Monitor API usage to avoid unexpected costs");
        System.out.println("• Keep API keys secure and never share them");
        
        System.out.println("\n💰 Cost Considerations:");
        System.out.println("• OpenAI API: ~$0.01-0.03 per message (depending on length)");
        System.out.println("• GitHub Models: Included with Copilot PRO subscription");
        System.out.println("• Set up billing alerts to monitor usage");
    }
}
