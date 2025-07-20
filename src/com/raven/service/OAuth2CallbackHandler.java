package com.raven.service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class OAuth2CallbackHandler {
    private HttpServer server;
    private CompletableFuture<GoogleAuthService.GoogleUserInfo> userInfoFuture;
    private GoogleAuthService authService;
    private String expectedState;
    
    public OAuth2CallbackHandler(GoogleAuthService authService, String expectedState) {
        this.authService = authService;
        this.expectedState = expectedState;
        this.userInfoFuture = new CompletableFuture<>();
    }
    
    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/oauth/callback", new CallbackHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("OAuth callback server started on port 8080");
    }
    
    public void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("OAuth callback server stopped");
        }
    }
    
    public CompletableFuture<GoogleAuthService.GoogleUserInfo> getUserInfoFuture() {
        return userInfoFuture;
    }
    
    private class CallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            
            String response;
            int statusCode;
            
            try {
                String code = params.get("code");
                String state = params.get("state");
                String error = params.get("error");
                
                if (error != null) {
                    response = "<html><body><h1>Authentication Error</h1><p>Error: " + error + "</p></body></html>";
                    statusCode = 400;
                    userInfoFuture.completeExceptionally(new Exception("OAuth error: " + error));
                } else if (code == null || state == null) {
                    response = "<html><body><h1>Authentication Error</h1><p>Missing required parameters</p></body></html>";
                    statusCode = 400;
                    userInfoFuture.completeExceptionally(new Exception("Missing OAuth parameters"));
                } else if (!expectedState.equals(state)) {
                    response = "<html><body><h1>Security Error</h1><p>Invalid state parameter</p></body></html>";
                    statusCode = 400;
                    userInfoFuture.completeExceptionally(new Exception("Invalid state parameter"));
                } else {
                    System.out.println("✓ OAuth callback received with valid parameters");
                    System.out.println("📋 Authorization code: " + code.substring(0, 10) + "...");
                    System.out.println("🔐 State verified: " + state.equals(expectedState));
                    
                    // Exchange code for tokens using real Google API
                    GoogleAuthService.GoogleUserInfo userInfo = authService.exchangeCodeForUserInfo(code);
                    
                    response = "<html><body style='font-family: Arial, sans-serif; text-align: center; padding: 50px;'>" +
                              "<h1 style='color: #4CAF50;'>✅ Authentication Successful!</h1>" +
                              "<p style='font-size: 18px;'>Welcome, " + userInfo.getName() + "!</p>" +
                              "<p style='color: #666;'>You can close this window and return to the application.</p>" +
                              "<script>setTimeout(function(){window.close();}, 3000);</script>" +
                              "</body></html>";
                    statusCode = 200;
                    userInfoFuture.complete(userInfo);
                }
            } catch (Exception e) {
                response = "<html><body><h1>Authentication Error</h1><p>Error: " + e.getMessage() + "</p></body></html>";
                statusCode = 500;
                userInfoFuture.completeExceptionally(e);
            }
            
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(statusCode, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            
            // Stop server after handling callback
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // Give time for response to be sent
                    stopServer();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
    
    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    try {
                        params.put(keyValue[0], URLDecoder.decode(keyValue[1], "UTF-8"));
                    } catch (Exception e) {
                        // Skip malformed parameters
                    }
                }
            }
        }
        return params;
    }
}
