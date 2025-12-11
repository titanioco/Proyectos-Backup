package com.raven.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * AI Configuration Manager
 * Handles OpenAI and GitHub Models API configuration
 */
public class AIConfig {
    private static final Properties props = new Properties();
    private static boolean loaded = false;
    
    static {
        loadProperties();
    }
    
    private static void loadProperties() {
        try {
            // Try to load from file first
            try (InputStream input = new FileInputStream("ai.properties")) {
                props.load(input);
                loaded = true;
                System.out.println("SUCCESS: Loaded AI configuration from ai.properties");
            } catch (IOException e) {
                // Fallback to classpath
                try (InputStream input = AIConfig.class.getClassLoader().getResourceAsStream("ai.properties")) {
                    if (input != null) {
                        props.load(input);
                        loaded = true;
                        System.out.println("SUCCESS: Loaded AI configuration from classpath");
                    }
                } catch (IOException e2) {
                    System.out.println("WARNING: Could not load ai.properties, using default values");
                }
            }
        } catch (Exception e) {
            System.out.println("WARNING: Error loading AI configuration: " + e.getMessage());
        }
    }
    
    // OpenAI Configuration
    public static String getOpenAIApiKey() {
        return props.getProperty("openai.api.key", "");
    }
    
    public static String getOpenAIModel() {
        return props.getProperty("openai.model", "gpt-4o");
    }
    
    public static String getOpenAIBaseUrl() {
        return props.getProperty("openai.base.url", "https://api.openai.com/v1");
    }
    
    public static int getOpenAIMaxTokens() {
        return Integer.parseInt(props.getProperty("openai.max.tokens", "2000"));
    }
    
    public static double getOpenAITemperature() {
        return Double.parseDouble(props.getProperty("openai.temperature", "0.7"));
    }
    
    // GitHub Models Configuration
    public static String getGitHubApiToken() {
        return props.getProperty("github.api.token", "");
    }
    
    public static String getGitHubModel() {
        return props.getProperty("github.model", "gpt-4o");
    }
    
    public static String getGitHubBaseUrl() {
        return props.getProperty("github.base.url", "https://models.inference.ai.azure.com");
    }
    
    public static int getGitHubMaxTokens() {
        return Integer.parseInt(props.getProperty("github.max.tokens", "2000"));
    }
    
    // AI Assistant Configuration
    public static String getSystemPrompt() {
        return props.getProperty("ai.system.prompt", 
            "You are an academic assistant specialized in helping university students with their projects, research methodologies, and academic writing. Provide clear, educational responses with practical examples.");
    }
    
    public static int getResponseTimeout() {
        return Integer.parseInt(props.getProperty("ai.response.timeout", "30"));
    }
    
    public static int getRetryAttempts() {
        return Integer.parseInt(props.getProperty("ai.retry.attempts", "3"));
    }
    
    public static boolean isOpenAIConfigured() {
        return loaded && !getOpenAIApiKey().isEmpty() && getOpenAIApiKey().startsWith("sk-");
    }
    
    public static boolean isGitHubConfigured() {
        return loaded && !getGitHubApiToken().isEmpty() && getGitHubApiToken().startsWith("github_pat_");
    }
    
    /**
     * Print diagnostic information about AI configuration
     */
    public static void printDiagnostics() {
        System.out.println("DIAGNOSTICS: AI Configuration Diagnostics:");
        System.out.println("  - Properties file loaded: " + loaded);
        System.out.println("  - OpenAI configured: " + isOpenAIConfigured());
        System.out.println("  - OpenAI model: " + getOpenAIModel());
        System.out.println("  - GitHub configured: " + isGitHubConfigured());
        System.out.println("  - GitHub model: " + getGitHubModel());
        System.out.println("  - Max tokens: " + getOpenAIMaxTokens());
        System.out.println("  - Temperature: " + getOpenAITemperature());
    }
}
