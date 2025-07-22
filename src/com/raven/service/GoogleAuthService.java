package com.raven.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.JsonObjectParser;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import com.google.api.client.auth.oauth2.TokenResponse;

public class GoogleAuthService {
    private static final NetHttpTransport HTTP = new NetHttpTransport();
    private static final GsonFactory JSON = new GsonFactory();
    private static final int TIMEOUT_SECONDS = 30;
    
    private final GoogleAuthorizationCodeFlow flow;
    private LocalServerReceiver receiver;
    private volatile boolean isAuthorizing = false;

    public GoogleAuthService(String clientId, String clientSecret) throws Exception {
        // Always use desktop app mode for Swing applications - simpler and more stable
        System.out.println("🔧 Initializing Google Auth Service for Desktop Application");
        
        this.flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP, JSON,
                clientId, clientSecret,
                Collections.singletonList("openid email profile"))
            .setAccessType("offline")
            .setApprovalPrompt("force")  // Always force consent screen
            .build();
            
        // Create receiver with fixed port for stability
        this.receiver = new LocalServerReceiver.Builder()
            .setHost("127.0.0.1")
            .setPort(8080)
            .setCallbackPath("/oauth2callback")
            .build();
            
        System.out.println("✅ Google Auth Service initialized successfully");
        System.out.println("📍 Callback URL: http://127.0.0.1:8080/oauth2callback");
    }

    public synchronized GoogleUserInfo authorize() throws Exception {
        if (isAuthorizing) {
            throw new IllegalStateException("Authorization already in progress");
        }
        
        isAuthorizing = true;
        try {
            System.out.println("🚀 Starting OAuth authorization flow...");
            
            // Use the standard installed app flow - most reliable for desktop apps
            AuthorizationCodeInstalledApp installedApp = new AuthorizationCodeInstalledApp(flow, receiver);
            
            // Set timeout for authorization
            CompletableFuture<Credential> credentialFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return installedApp.authorize("user");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            
            Credential credential = credentialFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            if (credential == null || credential.getAccessToken() == null) {
                throw new Exception("Failed to obtain valid access token");
            }
            
            System.out.println("✅ OAuth credential obtained successfully");
            
            // Get user info from Google API
            return getUserInfo(credential);
            
        } catch (Exception e) {
            System.err.println("❌ OAuth authorization failed: " + e.getMessage());
            throw e;
        } finally {
            isAuthorizing = false;
        }
    }
    
    private GoogleUserInfo getUserInfo(Credential credential) throws Exception {
        HttpRequestFactory requestFactory = HTTP.createRequestFactory(request -> {
            request.getHeaders().setAuthorization("Bearer " + credential.getAccessToken());
            request.setParser(new JsonObjectParser(JSON));
        });
        
        GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v2/userinfo");
        
        System.out.println("🌐 Fetching user info from Google API...");
        
        com.google.api.client.http.HttpResponse response = requestFactory.buildGetRequest(url).execute();
        
        if (response.getStatusCode() != 200) {
            throw new Exception("Failed to get user info: HTTP " + response.getStatusCode());
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.parseAs(Map.class);
        
        if (data == null) {
            throw new Exception("Failed to parse user info response");
        }
        
        String email = (String) data.get("email");
        String name = (String) data.get("name");
        String id = (String) data.get("id");
        
        if (email == null) {
            throw new Exception("Email not found in OAuth response");
        }
        
        System.out.println("✅ User info retrieved: " + name + " (" + email + ")");
        return new GoogleUserInfo(email, name, id);
    }
    /**
     * Exchange an authorization code for user info.
     * This method is used when manual callback handling is needed.
     */
    public GoogleUserInfo exchangeCodeForUserInfo(String code) throws Exception {
        try {
            System.out.println("🔄 Exchanging authorization code for tokens...");
            
            // Exchange authorization code for tokens
            TokenResponse tokenResponse = flow.newTokenRequest(code)
                .setRedirectUri("http://127.0.0.1:" + receiver.getPort() + receiver.getCallbackPath())
                .execute();
                
            Credential credential = flow.createAndStoreCredential(tokenResponse, "user");

            if (credential.getAccessToken() == null) {
                throw new Exception("Access token is null - code exchange failed");
            }
            
            System.out.println("✅ Token exchange successful");
            return getUserInfo(credential);
            
        } catch (Exception e) {
            System.err.println("❌ Code exchange failed: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Cleanup method to stop any running receivers
     */
    public void cleanup() {
        if (receiver != null) {
            try {
                receiver.stop();
                System.out.println("✅ OAuth receiver stopped");
            } catch (Exception e) {
                System.out.println("ℹ️ Receiver cleanup: " + e.getMessage());
            }
        }
        isAuthorizing = false;
    }

    public static class GoogleUserInfo {
        private final String email;
        private final String name;
        private final String id;

        public GoogleUserInfo(String email, String name, String id) {
            this.email = email;
            this.name = name;
            this.id = id;
        }
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getId() { return id; }
    }
}
