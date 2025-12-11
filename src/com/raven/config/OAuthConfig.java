package com.raven.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class OAuthConfig {
    private static final Properties props = new Properties();
    private static boolean loaded = false;
    
    static {
        loadProperties();
    }
    
    private static void loadProperties() {
        try {
            // 1. Try to load from working directory (file system)
            java.io.File file = new java.io.File("oauth.properties");
            System.out.println("🔍 Looking for config at: " + file.getAbsolutePath());
            if (file.exists()) {
                try (InputStream input = new FileInputStream(file)) {
                    props.load(input);
                    loaded = true;
                    System.out.println("✅ Loaded OAuth configuration from file: " + file.getAbsolutePath());
                    return;
                }
            } else {
                System.out.println("❌ File not found at: " + file.getAbsolutePath());
            }

            // 2. Try to load from classpath (root)
            System.out.println("🔍 Looking for config in classpath: oauth.properties");
            try (InputStream input = OAuthConfig.class.getClassLoader().getResourceAsStream("oauth.properties")) {
                if (input != null) {
                    props.load(input);
                    loaded = true;
                    System.out.println("✅ Loaded OAuth configuration from classpath (root)");
                    return;
                } else {
                    System.out.println("❌ Not found in classpath (root)");
                }
            }

            // 3. Try to load from classpath (com/raven package)
            System.out.println("🔍 Looking for config in classpath: com/raven/oauth.properties");
            try (InputStream input = OAuthConfig.class.getClassLoader().getResourceAsStream("com/raven/oauth.properties")) {
                if (input != null) {
                    props.load(input);
                    loaded = true;
                    System.out.println("✅ Loaded OAuth configuration from classpath (com/raven)");
                    return;
                } else {
                    System.out.println("❌ Not found in classpath (com/raven)");
                }
            }
            
            // 4. Try to load from src/com/raven/oauth.properties (direct file access for dev)
            java.io.File devFile = new java.io.File("src/com/raven/oauth.properties");
            System.out.println("🔍 Looking for config at: " + devFile.getAbsolutePath());
            if (devFile.exists()) {
                try (InputStream input = new FileInputStream(devFile)) {
                    props.load(input);
                    loaded = true;
                    System.out.println("✅ Loaded OAuth configuration from dev file: " + devFile.getAbsolutePath());
                    return;
                }
            }

            System.out.println("⚠ Could not load oauth.properties from any location, using default values");

        } catch (Exception e) {
            System.out.println("⚠ Error loading OAuth configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static String getClientId() {
        return props.getProperty("google.oauth.client.id", "YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com");
    }
    
    public static String getClientSecret() {
        return props.getProperty("google.oauth.client.secret", "");
    }
    
    public static String getRedirectUri() {
        return props.getProperty("google.oauth.redirect.uri", "http://localhost:8080/oauth/callback");
    }
    
    public static String getScope() {
        return props.getProperty("google.oauth.scope", "openid email profile");
    }
    
    public static String getAuthUrl() {
        return props.getProperty("google.oauth.auth.url", "https://accounts.google.com/o/oauth2/v2/auth");
    }
    
    public static String getTokenUrl() {
        return props.getProperty("google.oauth.token.url", "https://oauth2.googleapis.com/token");
    }
    
    public static String getUserInfoUrl() {
        return props.getProperty("google.oauth.userinfo.url", "https://www.googleapis.com/oauth2/v2/userinfo");
    }
    
    public static boolean isConfigured() {
        String clientId = getClientId();
        String clientSecret = getClientSecret();
        
        // Check if OAuth is properly configured with real credentials
        boolean hasValidClientId = loaded && clientId != null && 
            !clientId.equals("YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com") &&
            !clientId.equals("YOUR_CLIENT_ID_HERE") &&
            !clientId.equals("dev-test-client-id.apps.googleusercontent.com") &&
            !clientId.startsWith("dev-test-") &&
            !clientId.startsWith("test-") &&
            clientId.contains(".apps.googleusercontent.com");
            
        // For desktop applications, client secret can be empty
        // Only validate client secret if it's provided
        boolean hasValidClientSecret = clientSecret == null || clientSecret.trim().isEmpty() ||
            (!clientSecret.equals("YOUR_GOOGLE_CLIENT_SECRET") &&
             !clientSecret.equals("YOUR_CLIENT_SECRET_HERE") &&
             !clientSecret.equals("dev-test-client-secret-placeholder") &&
             !clientSecret.startsWith("dev-test-") &&
             !clientSecret.startsWith("test-"));
        
        return hasValidClientId && hasValidClientSecret;
    }
    
    /**
     * Print diagnostic information about OAuth configuration
     */
    public static void printDiagnostics() {
        System.out.println("🔍 OAuth Configuration Diagnostics:");
        System.out.println("  - Properties file loaded: " + loaded);
        System.out.println("  - Client ID: " + getClientId());
        System.out.println("  - Client Secret: " + (getClientSecret().isEmpty() ? "EMPTY (Desktop App)" : "PROVIDED (Web App)"));
        System.out.println("  - Redirect URI: " + getRedirectUri());
        System.out.println("  - Is Configured: " + isConfigured());
        System.out.println("  - Configuration Status: " + (isConfigured() ? "✅ Ready for OAuth" : "⚠️ Needs setup"));
    }
}
