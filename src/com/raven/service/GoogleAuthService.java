package com.raven.service;

import com.raven.config.OAuthConfig;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class GoogleAuthService {
    private String codeVerifier;
    private String codeChallenge;
    private String state;
    
    public void initiateOAuth() throws IOException, URISyntaxException {
        // Check if OAuth is properly configured
        if (!OAuthConfig.isConfigured()) {
            javax.swing.JOptionPane.showMessageDialog(null, 
                "Google OAuth Setup Required\n\n" +
                "To enable live Google authentication:\n\n" +
                "1. Go to Google Cloud Console (console.cloud.google.com)\n" +
                "2. Create a new project or select existing one\n" +
                "3. Enable Google+ API\n" +
                "4. Create OAuth 2.0 credentials\n" +
                "5. Add authorized redirect URI: " + OAuthConfig.getRedirectUri() + "\n" +
                "6. Edit oauth.properties file with your Client ID\n\n" +
                "For now, using demo mode...",
                "Google OAuth Configuration", 
                javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        System.out.println("🔧 Generating PKCE parameters for OAuth security...");
        // Generate PKCE code verifier and challenge for security
        generateCodeVerifierAndChallenge();
        
        // Generate state for CSRF protection
        generateState();
        
        // Build authorization URL with real OAuth parameters
        String authUrl = buildAuthorizationUrl();
        System.out.println("🌐 Authorization URL: " + authUrl);
        
        // Open browser for real OAuth flow
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI(authUrl));
            
            javax.swing.JOptionPane.showMessageDialog(null, 
                "Google OAuth Started\n\n" +
                "Your browser should open with Google sign-in.\n" +
                "After authorization, you'll be redirected back to the app.\n\n" +
                "Note: Make sure your redirect URI is properly configured\n" +
                "in Google Cloud Console.",
                "Google Sign-In", 
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } else {
            javax.swing.JOptionPane.showMessageDialog(null, 
                "Cannot open browser automatically.\n\n" +
                "Please manually navigate to:\n" + authUrl,
                "Manual OAuth", 
                javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void generateCodeVerifierAndChallenge() {
        try {
            // Generate a cryptographically random code verifier (43-128 characters)
            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            this.codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            
            // Create SHA256 hash of the code verifier for the challenge
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
            this.codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            
            System.out.println("✓ PKCE code verifier generated (length: " + codeVerifier.length() + ")");
        } catch (Exception e) {
            System.err.println("❌ Error generating PKCE parameters: " + e.getMessage());
            throw new RuntimeException("Failed to generate PKCE parameters", e);
        }
    }
    
    private void generateState() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        this.state = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        System.out.println("✓ OAuth state generated for CSRF protection");
    }
    
    private String buildAuthorizationUrl() {
        try {
            StringBuilder url = new StringBuilder(OAuthConfig.getAuthUrl());
            url.append("?response_type=code");
            url.append("&client_id=").append(URLEncoder.encode(OAuthConfig.getClientId(), "UTF-8"));
            url.append("&redirect_uri=").append(URLEncoder.encode(OAuthConfig.getRedirectUri(), "UTF-8"));
            url.append("&scope=").append(URLEncoder.encode(OAuthConfig.getScope(), "UTF-8"));
            url.append("&state=").append(state);
            url.append("&code_challenge=").append(codeChallenge);
            url.append("&code_challenge_method=S256");
            return url.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error building authorization URL", e);
        }
    }
    
    // Exchange authorization code for access token and get user info
    public GoogleUserInfo exchangeCodeForUserInfo(String authorizationCode) throws IOException {
        System.out.println("🔄 Exchanging authorization code for access token...");
        
        // Step 1: Exchange code for access token
        String accessToken = exchangeCodeForToken(authorizationCode);
        
        // Step 2: Use access token to get user info
        return getUserInfoFromGoogle(accessToken);
    }
    
    private String exchangeCodeForToken(String authorizationCode) throws IOException {
        System.out.println("📞 Making token request to Google...");
        
        URL url = new URL(OAuthConfig.getTokenUrl());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        
        // Build POST parameters
        StringBuilder params = new StringBuilder();
        params.append("client_id=").append(URLEncoder.encode(OAuthConfig.getClientId(), "UTF-8"));
        params.append("&code=").append(URLEncoder.encode(authorizationCode, "UTF-8"));
        params.append("&code_verifier=").append(URLEncoder.encode(codeVerifier, "UTF-8"));
        params.append("&grant_type=authorization_code");
        params.append("&redirect_uri=").append(URLEncoder.encode(OAuthConfig.getRedirectUri(), "UTF-8"));
        
        // Send request
        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.toString().getBytes(StandardCharsets.UTF_8));
        }
        
        // Read response
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        
        // Parse JSON response (simple parsing without external libraries)
        String responseStr = response.toString();
        System.out.println("✓ Token response received: " + responseStr.substring(0, Math.min(100, responseStr.length())) + "...");
        
        // Extract access token from JSON response
        String accessToken = extractJsonValue(responseStr, "access_token");
        if (accessToken == null) {
            throw new IOException("Failed to extract access token from response: " + responseStr);
        }
        
        System.out.println("✓ Access token obtained");
        return accessToken;
    }
    
    private GoogleUserInfo getUserInfoFromGoogle(String accessToken) throws IOException {
        System.out.println("👤 Fetching user info from Google API...");
        
        URL url = new URL("https://www.googleapis.com/oauth2/v2/userinfo");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        
        // Read response
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        
        String responseStr = response.toString();
        System.out.println("✓ User info received: " + responseStr.substring(0, Math.min(100, responseStr.length())) + "...");
        
        // Parse user info from JSON response
        String email = extractJsonValue(responseStr, "email");
        String name = extractJsonValue(responseStr, "name");
        String id = extractJsonValue(responseStr, "id");
        
        if (email == null || name == null || id == null) {
            throw new IOException("Failed to extract user info from response: " + responseStr);
        }
        
        System.out.println("✓ User authenticated: " + name + " (" + email + ")");
        return new GoogleUserInfo(email, name, id);
    }
    
    // Simple JSON value extraction (avoiding external dependencies)
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;
        
        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) return null;
        
        int startIndex = json.indexOf("\"", colonIndex);
        if (startIndex == -1) return null;
        startIndex++; // Skip opening quote
        
        int endIndex = json.indexOf("\"", startIndex);
        if (endIndex == -1) return null;
        
        return json.substring(startIndex, endIndex);
    }
    
    public String getCodeVerifier() {
        return codeVerifier;
    }
    
    public String getState() {
        return state;
    }
    
    // Mock user info for demonstration (in real implementation, this would exchange code for token)
    public GoogleUserInfo getMockUserInfo() {
        return new GoogleUserInfo("user@gmail.com", "John Doe", "google-sub-123456");
    }
    
    public static class GoogleUserInfo {
        private final String email;
        private final String name;
        private final String sub;
        
        public GoogleUserInfo(String email, String name, String sub) {
            this.email = email;
            this.name = name;
            this.sub = sub;
        }
        
        public String getEmail() {
            return email;
        }
        
        public String getName() {
            return name;
        }
        
        public String getSub() {
            return sub;
        }
    }
}
