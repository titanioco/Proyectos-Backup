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
import com.google.api.client.auth.oauth2.TokenResponse;

public class GoogleAuthService {
    private static final NetHttpTransport HTTP = new NetHttpTransport();
    private static final GsonFactory JSON = new GsonFactory();

    private final GoogleAuthorizationCodeFlow flow;
    private final LocalServerReceiver receiver;

    public GoogleAuthService(String clientId, String clientSecret) throws Exception {
        flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP, JSON,
                clientId, clientSecret,
                Collections.singletonList("openid email profile"))
            .setAccessType("offline")
            .build();
        receiver = new LocalServerReceiver.Builder()
            .setHost("127.0.0.1")
            .setPort(8080)
            .setCallbackPath("/oauth2callback")
            .build();
    }

    public GoogleUserInfo authorize() throws Exception {
        try {
            Credential credential = new AuthorizationCodeInstalledApp(flow, receiver)
                .authorize("user");
            
            System.out.println("🔑 OAuth credential obtained successfully");
            System.out.println("📧 Access token: " + (credential.getAccessToken() != null ? "Present" : "NULL"));
            
            if (credential.getAccessToken() == null) {
                throw new Exception("Access token is null - authorization failed");
            }
            
            HttpRequestFactory requestFactory = HTTP.createRequestFactory(request -> {
                request.getHeaders().setAuthorization("Bearer " + credential.getAccessToken());
                // Set JSON parser for response parsing
                request.setParser(new JsonObjectParser(JSON));
            });
            
            GenericUrl url = new GenericUrl(
                "https://www.googleapis.com/oauth2/v2/userinfo");
            
            System.out.println("🌐 Making request to: " + url.toString());
            
            com.google.api.client.http.HttpResponse response = requestFactory.buildGetRequest(url).execute();
            
            System.out.println("📡 Response status: " + response.getStatusCode());
            System.out.println("📡 Response content type: " + response.getContentType());
            
            if (response.getStatusCode() != 200) {
                throw new Exception("HTTP " + response.getStatusCode() + ": " + response.getStatusMessage());
            }
            
            String responseContent = response.parseAsString();
            System.out.println("📡 Response content: " + responseContent);
            
            // Reset the response for parsing
            response = requestFactory.buildGetRequest(url).execute();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.parseAs(Map.class);
            
            if (data == null) {
                throw new Exception("Failed to parse user info response");
            }
            
            String email = (String) data.get("email");
            String name = (String) data.get("name");
            String id = (String) data.get("id");
            
            System.out.println("👤 User info - Email: " + email + ", Name: " + name + ", ID: " + id);
            
            if (email == null) {
                throw new Exception("Email not found in OAuth response");
            }
            
            return new GoogleUserInfo(email, name, id);
            
        } catch (Exception e) {
            System.err.println("❌ OAuth authorization error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    /**
     * Exchange an authorization code manually for user info.
     */
    public GoogleUserInfo exchangeCodeForUserInfo(String code) throws Exception {
        // Exchange authorization code for tokens
        TokenResponse tokenResponse = flow.newTokenRequest(code)
            .setRedirectUri("http://127.0.0.1:" + receiver.getPort() + receiver.getCallbackPath())
            .execute();
        Credential credential = flow.createAndStoreCredential(tokenResponse, "user");

        if (credential.getAccessToken() == null) {
            throw new Exception("Access token is null - code exchange failed");
        }
        HttpRequestFactory requestFactory = HTTP.createRequestFactory(request -> {
            request.getHeaders().setAuthorization("Bearer " + credential.getAccessToken());
            request.setParser(new JsonObjectParser(JSON));
        });
        GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v2/userinfo");
        com.google.api.client.http.HttpResponse response = requestFactory.buildGetRequest(url).execute();
        if (response.getStatusCode() != 200) {
            throw new Exception("HTTP " + response.getStatusCode() + ": " + response.getStatusMessage());
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
        return new GoogleUserInfo(email, name, id);
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
