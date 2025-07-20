package com.raven.service;

import java.awt.Desktop;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Base64;

public class GoogleAuthService {
    // For demo purposes, we'll simulate Google auth without real OAuth
    // To use real Google OAuth, you need to:
    // 1. Create a project in Google Cloud Console
    // 2. Enable Google+ API
    // 3. Create OAuth 2.0 credentials
    // 4. Replace CLIENT_ID with your actual client ID
    private static final String CLIENT_ID = "demo-client-id";
    private static final String REDIRECT_URI = "http://localhost:8080/callback";
    private static final String SCOPE = "openid email profile";
    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    
    private String codeVerifier;
    private String state;
    
    public void initiateOAuth() throws IOException, URISyntaxException {
        // For demo purposes, show a message instead of opening browser
        javax.swing.JOptionPane.showMessageDialog(null, 
            "Demo Google Sign-In\n\n" +
            "In a real implementation, this would:\n" +
            "1. Open your browser to Google OAuth\n" +
            "2. Ask for your permission\n" +
            "3. Return with authentication\n\n" +
            "For now, clicking OK will simulate successful sign-in.",
            "Google Sign-In Demo", 
            javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void generateCodeVerifierAndChallenge() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        this.codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    private void generateState() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        this.state = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    private String buildAuthorizationUrl() {
        try {
            StringBuilder url = new StringBuilder(AUTH_URL);
            url.append("?response_type=code");
            url.append("&client_id=").append(URLEncoder.encode(CLIENT_ID, "UTF-8"));
            url.append("&redirect_uri=").append(URLEncoder.encode(REDIRECT_URI, "UTF-8"));
            url.append("&scope=").append(URLEncoder.encode(SCOPE, "UTF-8"));
            url.append("&state=").append(state);
            url.append("&code_challenge=").append(codeVerifier);
            url.append("&code_challenge_method=S256");
            return url.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error building authorization URL", e);
        }
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
