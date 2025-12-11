package com.raven.service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class OAuth2CallbackHandler {
    private static HttpServer activeServer = null; // Track active server globally
    private static OAuth2CallbackHandler activeHandler = null; // Track active handler globally
    private HttpServer server;
    private CompletableFuture<GoogleAuthService.GoogleUserInfo> userInfoFuture;
    private Consumer<GoogleAuthService.GoogleUserInfo> onSuccess;
    private Consumer<Exception> onError;
    private GoogleAuthService authService;
    private String expectedState;
    private int serverPort = 8080; // Track which port we're using
    
    // Constructor with async callbacks
    public OAuth2CallbackHandler(GoogleAuthService authService, String expectedState,
        Consumer<GoogleAuthService.GoogleUserInfo> onSuccess, Consumer<Exception> onError) {
        this(authService, expectedState);
        this.onSuccess = onSuccess;
        this.onError = onError;
    }
    
    // Existing constructor
    private OAuth2CallbackHandler(GoogleAuthService authService, String expectedState) {
        this.authService = authService;
        this.expectedState = expectedState;
        this.userInfoFuture = new CompletableFuture<>();
        activeHandler = this;
    }
    
    public int getServerPort() {
        return serverPort;
    }
    
    public void startServer() throws IOException {
        // Only stop servers that aren't ours
        if (activeServer != null && activeHandler != this) {
            System.out.println("🔄 Stopping different OAuth handler's server...");
            stopAnyActiveServer();
        }
        
        // Try only the most reliable port and address first for speed
        int[] portsToTry = {8080, 8081, 3000}; // Reduced to 3 ports
        String[] addressesToTry = {"127.0.0.1"}; // Only use 127.0.0.1 for speed
        
        boolean serverStarted = false;
        Exception lastException = null;
        
        System.out.println("🚀 Attempting fast server startup...");
        for (int port : portsToTry) {
            for (String address : addressesToTry) {
                try {
                    System.out.println("⏳ Trying " + address + ":" + port + "...");
                    server = HttpServer.create(new InetSocketAddress(address, port), 0);
                    serverPort = port;
                    System.out.println("✅ Server bound to " + address + ":" + port);
                    serverStarted = true;
                    break;
                } catch (Exception e) {
                    lastException = e;
                    System.out.println("❌ Failed to bind to " + address + ":" + port + " - " + e.getMessage());
                }
            }
            if (serverStarted) break;
        }
        
        if (!serverStarted) {
            throw new IOException("Failed to start server on any port: " + lastException.getMessage());
        }
        
        server.createContext("/oauth/callback", new CallbackHandler());
        server.setExecutor(null);
        server.start();
        
        // Track this as the active server
        activeServer = server;
        System.out.println("OAuth callback server started on port " + serverPort);
        
        // Important: Update the redirect URI to match the actual port used
        if (serverPort != 8080) {
            System.out.println("⚠️ IMPORTANT: Server started on port " + serverPort + " instead of 8080");
            System.out.println("   You may need to update Google Cloud Console redirect URI to:");
            System.out.println("   http://127.0.0.1:" + serverPort + "/oauth/callback");
        }
        
        System.out.println("✓ Callback server ready to receive OAuth redirects at http://127.0.0.1:" + serverPort + "/oauth/callback");
    }
    
    private void testServerAccessibility() {
        try {
            System.out.println("🔍 Testing server accessibility...");
            
            // Test with a different endpoint that won't interfere with OAuth
            URL testUrl = new URL("http://127.0.0.1:" + serverPort + "/test");
            HttpURLConnection connection = (HttpURLConnection) testUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            
            // We expect this to fail (404) since /test doesn't exist, but connection should work
            try {
                int responseCode = connection.getResponseCode();
                System.out.println("✓ Server accessible - Response code: " + responseCode);
            } catch (Exception e) {
                // Check if it's just a 404 (which means server is running)
                if (connection.getResponseCode() == 404) {
                    System.out.println("✓ Server accessible (404 expected for /test endpoint)");
                } else {
                    throw e;
                }
            }
            connection.disconnect();
            
        } catch (Exception e) {
            System.err.println("❌ Server accessibility test failed: " + e.getMessage());
            System.err.println("   This might indicate firewall or network issues");
            
            // Additional debugging: try to see if something else is using the port
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress("127.0.0.1", serverPort), 1000);
                socket.close();
                System.err.println("   Port " + serverPort + " appears to be occupied by another service");
            } catch (Exception portTest) {
                System.err.println("   Port " + serverPort + " connectivity test also failed: " + portTest.getMessage());
            }
        }
    }
    
    public static void stopAnyActiveServer() {
        if (activeServer != null) {
            try {
                activeServer.stop(0);
                System.out.println("Previous OAuth callback server stopped");
            } catch (Exception e) {
                System.out.println("Note: No previous server to stop");
            }
            activeServer = null;
        }
        
        if (activeHandler != null && activeHandler.userInfoFuture != null && !activeHandler.userInfoFuture.isDone()) {
            activeHandler.userInfoFuture.completeExceptionally(new Exception("OAuth cancelled - new attempt started"));
            System.out.println("Previous OAuth attempt cancelled");
        }
        activeHandler = null;
    }
    
    public void stopServer() {
        if (server != null) {
            server.stop(0);
            if (activeServer == server) {
                activeServer = null;
            }
            System.out.println("OAuth callback server stopped");
        }
    }
    
    public CompletableFuture<GoogleAuthService.GoogleUserInfo> getUserInfoFuture() {
        return userInfoFuture;
    }
    
    // Manual callback injection for when browser can't connect to localhost
    public void injectManualCallback(String callbackUrl) {
        try {
            System.out.println("CALLBACK: Processing manual callback URL: " + callbackUrl);
            
            // Extract query parameters from the URL
            if (callbackUrl.contains("?")) {
                String query = callbackUrl.substring(callbackUrl.indexOf("?") + 1);
                Map<String, String> params = parseQuery(query);
                
                String code = params.get("code");
                String state = params.get("state");
                String error = params.get("error");
                
                System.out.println("🔍 Manual OAuth Callback Debug:");
                System.out.println("  - Received state: '" + state + "'");
                System.out.println("  - Expected state: '" + expectedState + "'");
                System.out.println("  - States match: " + (expectedState != null && expectedState.equals(state)));
                System.out.println("  - Code present: " + (code != null));
                System.out.println("  - Error: " + error);
                
                if (error != null) {
                    userInfoFuture.completeExceptionally(new Exception("OAuth error: " + error));
                } else if (code == null || state == null) {
                    userInfoFuture.completeExceptionally(new Exception("Missing OAuth parameters"));
                } else if (expectedState == null || !expectedState.equals(state)) {
                    System.err.println("❌ State mismatch - Expected: '" + expectedState + "', Received: '" + state + "'");
                    userInfoFuture.completeExceptionally(new Exception("Invalid state parameter"));
                } else {
                    System.out.println("✓ Manual OAuth callback received with valid parameters");
                    System.out.println("📋 Authorization code: " + code.substring(0, 10) + "...");
                    System.out.println("🔐 State verified: " + state.equals(expectedState));
                    
                    try {
                        System.out.println("🔄 About to exchange code for user info...");
                        GoogleAuthService.GoogleUserInfo userInfo = authService.exchangeCodeForUserInfo(code);
                        System.out.println("✓ User info received: " + userInfo.getName() + " (" + userInfo.getEmail() + ")");
                        
                        System.out.println("🚀 Completing userInfoFuture with user info...");
                        userInfoFuture.complete(userInfo);
                        System.out.println("✓ UserInfoFuture completed successfully");
                        
                    } catch (Exception tokenException) {
                        System.err.println("❌ Error during token exchange: " + tokenException.getMessage());
                        tokenException.printStackTrace();
                        userInfoFuture.completeExceptionally(tokenException);
                    }
                }
            } else {
                userInfoFuture.completeExceptionally(new Exception("Invalid callback URL format"));
            }
        } catch (Exception e) {
            System.err.println("❌ Error processing manual callback: " + e.getMessage());
            userInfoFuture.completeExceptionally(e);
        }
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
                
                System.out.println("🔍 OAuth Callback Debug:");
                System.out.println("  - Received state: '" + state + "'");
                System.out.println("  - Expected state: '" + expectedState + "'");
                System.out.println("  - States match: " + (expectedState != null && expectedState.equals(state)));
                System.out.println("  - Code present: " + (code != null));
                System.out.println("  - Error: " + error);
                
                if (error != null) {
                    response = "<html><body><h1>Authentication Error</h1><p>Error: " + error + "</p></body></html>";
                    statusCode = 400;
                    userInfoFuture.completeExceptionally(new Exception("OAuth error: " + error));
                } else if (code == null || state == null) {
                    response = "<html><body><h1>Authentication Error</h1><p>Missing required parameters</p></body></html>";
                    statusCode = 400;
                    userInfoFuture.completeExceptionally(new Exception("Missing OAuth parameters"));
                } else if (expectedState == null || !expectedState.equals(state)) {
                    System.err.println("❌ State mismatch - Expected: '" + expectedState + "', Received: '" + state + "'");
                    response = "<html><body style='font-family: Arial, sans-serif; text-align: center; padding: 50px;'>" +
                              "<h1 style='color: #ff4444;'>⚠️ Security Error</h1>" +
                              "<p style='font-size: 18px;'>Invalid state parameter</p>" +
                              "<p style='color: #666;'>Expected: " + expectedState + "</p>" +
                              "<p style='color: #666;'>Received: " + state + "</p>" +
                              "<p>This is likely a configuration issue. Please check the console output.</p>" +
                              "</body></html>";
                    statusCode = 400;
                    userInfoFuture.completeExceptionally(new Exception("Invalid state parameter"));
                } else {
                    System.out.println("✓ OAuth callback received with valid parameters");
                    System.out.println("📋 Authorization code: " + code.substring(0, 10) + "...");
                    System.out.println("🔐 State verified: " + state.equals(expectedState));
                    
                    try {
                        System.out.println("🔄 About to exchange code for user info...");
                        System.out.println("🚨 DEBUG: Calling authService.exchangeCodeForUserInfo() now!");
                        
                        // Exchange code for tokens using real Google API
                        GoogleAuthService.GoogleUserInfo userInfo = authService.exchangeCodeForUserInfo(code);
                        System.out.println("✅ SUCCESS: User info received: " + userInfo.getName() + " (" + userInfo.getEmail() + ")");
                        
                        response = "<html><body style='font-family: Arial, sans-serif; text-align: center; padding: 50px;'>" +
                                  "<h1 style='color: #4CAF50;'>✅ Authentication Successful!</h1>" +
                                  "<p style='font-size: 18px;'>Welcome, " + userInfo.getName() + "!</p>" +
                                  "<p style='color: #666;'>You can close this window and return to the application.</p>" +
                                  "<script>setTimeout(function(){window.close();}, 3000);</script>" +
                                  "</body></html>";
                        statusCode = 200;
                        
                        System.out.println("🚀 Completing userInfoFuture with user info...");
                        userInfoFuture.complete(userInfo);
                        System.out.println("✓ UserInfoFuture completed successfully");
                        
                    } catch (Exception tokenException) {
                        System.err.println("❌ Error during token exchange: " + tokenException.getMessage());
                        tokenException.printStackTrace();
                        response = "<html><body style='font-family: Arial, sans-serif; text-align: center; padding: 50px;'>" +
                                  "<h1 style='color: #ff4444;'>⚠️ Token Exchange Error</h1>" +
                                  "<p style='font-size: 18px;'>Failed to get user information</p>" +
                                  "<p style='color: #666;'>Error: " + tokenException.getMessage() + "</p>" +
                                  "</body></html>";
                        statusCode = 500;
                        userInfoFuture.completeExceptionally(tokenException);
                    }
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
