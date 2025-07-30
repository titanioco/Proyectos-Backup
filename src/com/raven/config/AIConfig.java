package com.raven.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * AI Configuration Manager
 * Handles loading and managing AI service configurations
 * 
 * @author RAVEN
 * @version 1.0
 */
public class AIConfig {
    private static Properties properties;
    private static boolean isConfigured = false;
    
    static {
        loadConfiguration();
    }
    
    /**
     * Load AI configuration from ai.properties file
     */
    private static void loadConfiguration() {
        properties = new Properties();
        
        try {
            // Try to load from ai.properties file
            FileInputStream fis = new FileInputStream("ai.properties");
            properties.load(fis);
            fis.close();
            
            isConfigured = validateConfiguration();
            
        } catch (IOException e) {
            System.err.println("⚠️ AI configuration file not found: ai.properties");
            System.err.println("📝 Please copy ai.properties.template to ai.properties and configure your API keys");
            isConfigured = false;
        }
    }
    
    /**
     * Validate that required configuration is present
     */
    private static boolean validateConfiguration() {
        boolean openaiEnabled = Boolean.parseBoolean(getProperty("ai.openai.enabled", "false"));
        boolean githubEnabled = Boolean.parseBoolean(getProperty("ai.github.enabled", "false"));
        
        if (openaiEnabled) {
            String apiKey = getProperty("openai.api.key", "");
            if (apiKey.isEmpty() || apiKey.equals("YOUR_OPENAI_API_KEY_HERE")) {
                System.err.println("❌ OpenAI API key not configured");
                return false;
            }
        }
        
        if (githubEnabled) {
            String token = getProperty("github.api.token", "");
            if (token.isEmpty() || token.equals("YOUR_GITHUB_TOKEN_HERE")) {
                System.err.println("❌ GitHub API token not configured");
                return false;
            }
        }
        
        if (!openaiEnabled && !githubEnabled) {
            System.err.println("⚠️ No AI services enabled");
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if AI services are properly configured
     */
    public static boolean isConfigured() {
        return isConfigured;
    }
    
    /**
     * Get configuration property with default value
     */
    public static String getProperty(String key, String defaultValue) {
        if (properties == null) {
            return defaultValue;
        }
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Get configuration property
     */
    public static String getProperty(String key) {
        return getProperty(key, null);
    }
    
    // OpenAI Configuration Getters
    public static String getOpenAIApiKey() {
        return getProperty("openai.api.key");
    }
    
    public static String getOpenAIModel() {
        return getProperty("openai.model", "gpt-4o");
    }
    
    public static String getOpenAIBaseUrl() {
        return getProperty("openai.base.url", "https://api.openai.com/v1");
    }
    
    public static int getOpenAIMaxTokens() {
        return Integer.parseInt(getProperty("openai.max.tokens", "2000"));
    }
    
    public static double getOpenAITemperature() {
        return Double.parseDouble(getProperty("openai.temperature", "0.7"));
    }
    
    // GitHub Configuration Getters
    public static String getGitHubApiToken() {
        return getProperty("github.api.token");
    }
    
    public static String getGitHubModel() {
        return getProperty("github.model", "gpt-4o");
    }
    
    public static String getGitHubBaseUrl() {
        return getProperty("github.base.url", "https://models.inference.ai.azure.com");
    }
    
    public static int getGitHubMaxTokens() {
        return Integer.parseInt(getProperty("github.max.tokens", "2000"));
    }
    
    // General AI Configuration
    public static String getSystemPrompt() {
        return getProperty("ai.system.prompt", 
            "You are an academic assistant specialized in helping university students with their projects, research methodologies, and academic writing. Provide clear, educational responses with practical examples.");
    }
    
    public static int getResponseTimeout() {
        return Integer.parseInt(getProperty("ai.response.timeout", "30"));
    }
    
    public static int getRetryAttempts() {
        return Integer.parseInt(getProperty("ai.retry.attempts", "3"));
    }
    
    // Feature Toggles
    public static boolean isOpenAIEnabled() {
        return Boolean.parseBoolean(getProperty("ai.openai.enabled", "false"));
    }
    
    public static boolean isGitHubEnabled() {
        return Boolean.parseBoolean(getProperty("ai.github.enabled", "false"));
    }
    
    public static boolean isFallbackEnabled() {
        return Boolean.parseBoolean(getProperty("ai.fallback.enabled", "true"));
    }
    
    /**
     * Print diagnostic information
     */
    public static void printDiagnostics() {
        System.out.println("🔍 AI Configuration Diagnostics");
        System.out.println("================================");
        System.out.println("📁 Configuration loaded: " + (properties != null));
        System.out.println("✅ AI services configured: " + isConfigured());
        System.out.println("🤖 OpenAI enabled: " + isOpenAIEnabled());
        System.out.println("🐙 GitHub enabled: " + isGitHubEnabled());
        System.out.println("🔄 Fallback enabled: " + isFallbackEnabled());
        
        if (isOpenAIEnabled()) {
            String apiKey = getOpenAIApiKey();
            if (apiKey != null && apiKey.length() > 10) {
                System.out.println("🔑 OpenAI API Key: " + apiKey.substring(0, 10) + "...");
            }
            System.out.println("🤖 OpenAI Model: " + getOpenAIModel());
        }
        
        if (isGitHubEnabled()) {
            String token = getGitHubApiToken();
            if (token != null && token.length() > 10) {
                System.out.println("🔑 GitHub Token: " + token.substring(0, 15) + "...");
            }
            System.out.println("🐙 GitHub Model: " + getGitHubModel());
        }
    }
    
    /**
     * Reload configuration (useful for testing)
     */
    public static void reload() {
        loadConfiguration();
    }
}
