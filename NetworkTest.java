import java.net.*;
import java.io.*;

public class NetworkTest {
    public static void main(String[] args) {
        System.out.println("🧪 Testing Java Network Connectivity...\n");
        
        // Test 1: Basic Google connectivity
        testConnection("https://www.google.com", "Google Homepage");
        
        // Test 2: Google OAuth endpoint
        testConnection("https://accounts.google.com/o/oauth2/v2/auth", "Google OAuth");
        
        // Test 3: Google API endpoint
        testConnection("https://www.googleapis.com/oauth2/v2/userinfo", "Google API");
        
        // Test 4: Token endpoint
        testConnection("https://oauth2.googleapis.com/token", "OAuth Token Endpoint");
        
        // Test 5: Local server capability
        testLocalServer();
    }
    
    private static void testConnection(String urlString, String description) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200 || responseCode == 405 || responseCode == 401) {
                System.out.println("✅ " + description + ": SUCCESS (" + responseCode + ")");
            } else {
                System.out.println("⚠️  " + description + ": Unexpected response (" + responseCode + ")");
            }
            conn.disconnect();
        } catch (Exception e) {
            System.out.println("❌ " + description + ": FAILED - " + e.getMessage());
        }
    }
    
    private static void testLocalServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("✅ Port 8080: Available for OAuth callback server");
            serverSocket.close();
        } catch (Exception e) {
            System.out.println("❌ Port 8080: " + e.getMessage());
        }
    }
}
