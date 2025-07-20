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
            // Try to load from file first
            try (InputStream input = new FileInputStream("oauth.properties")) {
                props.load(input);
                loaded = true;
                System.out.println("✓ Loaded OAuth configuration from oauth.properties");
            } catch (IOException e) {
                // Fallback to classpath
                try (InputStream input = OAuthConfig.class.getClassLoader().getResourceAsStream("oauth.properties")) {
                    if (input != null) {
                        props.load(input);
                        loaded = true;
                        System.out.println("✓ Loaded OAuth configuration from classpath");
                    }
                } catch (IOException e2) {
                    System.out.println("⚠ Could not load oauth.properties, using default values");
                }
            }
        } catch (Exception e) {
            System.out.println("⚠ Error loading OAuth configuration: " + e.getMessage());
        }
    }
    
    public static String getClientId() {
        return props.getProperty("google.oauth.client.id", "YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com");
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
        return loaded && clientId != null && !clientId.equals("YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com");
    }
}
