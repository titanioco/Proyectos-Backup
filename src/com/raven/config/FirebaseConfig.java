package com.raven.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.List;
import java.util.Arrays;

/**
 * Firebase Configuration Manager
 * Handles Firebase project configuration and user management settings
 */
public class FirebaseConfig {
    private static final Properties props = new Properties();
    private static boolean loaded = false;
    
    static {
        loadProperties();
    }
    
    private static void loadProperties() {
        try {
            // Try to load from file first
            try (InputStream input = new FileInputStream("firebase.properties")) {
                props.load(input);
                loaded = true;
                System.out.println("SUCCESS: Loaded Firebase configuration from firebase.properties");
            } catch (IOException e) {
                // Fallback to classpath
                try (InputStream input = FirebaseConfig.class.getClassLoader().getResourceAsStream("firebase.properties")) {
                    if (input != null) {
                        props.load(input);
                        loaded = true;
                        System.out.println("SUCCESS: Loaded Firebase configuration from classpath");
                    }
                } catch (IOException e2) {
                    System.out.println("WARNING: Could not load firebase.properties, using default values");
                }
            }
        } catch (Exception e) {
            System.out.println("WARNING: Error loading Firebase configuration: " + e.getMessage());
        }
    }
    
    public static String getProjectId() {
        return props.getProperty("firebase.project.id", "investsmart-university-system");
    }
    
    public static String getDatabaseUrl() {
        return props.getProperty("firebase.database.url", "https://investsmart-university-system-default-rtdb.firebaseio.com/");
    }
    
    public static String getStorageBucket() {
        return props.getProperty("firebase.storage.bucket", "investsmart-university-system.appspot.com");
    }
    
    public static String getCredentialsPath() {
        return props.getProperty("firebase.credentials.path", "config/firebase-service-account.json");
    }
    
    public static boolean isWhitelistEnabled() {
        return Boolean.parseBoolean(props.getProperty("user.whitelist.enabled", "true"));
    }
    
    public static int getMaxUserCount() {
        return Integer.parseInt(props.getProperty("user.max.count", "1000"));
    }
    
    public static String getRegistrationMode() {
        return props.getProperty("user.registration.mode", "WHITELIST_ONLY");
    }
    
    public static List<String> getWhitelistedEmails() {
        String emails = props.getProperty("whitelist.emails", "diacostamo@unal.edu.co,titanioco@hotmail.com,goanetapp@gmail.com");
        return Arrays.asList(emails.split(","));
    }
    
    public static int getFreeUserMaxProjects() {
        return Integer.parseInt(props.getProperty("user.type.free.max.projects", "5"));
    }
    
    public static int getPremiumUserMaxProjects() {
        return Integer.parseInt(props.getProperty("user.type.premium.max.projects", "50"));
    }
    
    public static boolean isConfigured() {
        return loaded && getProjectId() != null && !getProjectId().isEmpty();
    }
    
    /**
     * Print diagnostic information about Firebase configuration
     */
    public static void printDiagnostics() {
        System.out.println("DIAGNOSTICS: Firebase Configuration Diagnostics:");
        System.out.println("  - Properties file loaded: " + loaded);
        System.out.println("  - Project ID: " + getProjectId());
        System.out.println("  - Database URL: " + getDatabaseUrl());
        System.out.println("  - Whitelist enabled: " + isWhitelistEnabled());
        System.out.println("  - Max users: " + getMaxUserCount());
        System.out.println("  - Whitelisted emails: " + getWhitelistedEmails().size());
        System.out.println("  - Is Configured: " + isConfigured());
    }
}
