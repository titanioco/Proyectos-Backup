package com.raven.config;

/**
 * OAuth Configuration Verification Utility
 * This utility helps verify that OAuth is properly configured with valid credentials
 */
public class OAuthVerifier {
    
    public static void main(String[] args) {
        System.out.println("🔍 OAuth Configuration Verification");
        System.out.println("=====================================");
        
        // Print diagnostics
        OAuthConfig.printDiagnostics();
        
        // Validate configuration
        if (OAuthConfig.isConfigured()) {
            System.out.println("\n✅ OAuth Configuration Status: READY");
            System.out.println("📋 All required OAuth settings are properly configured");
            System.out.println("🚀 Application should be able to authenticate with Google");
            
            // Additional validations
            String clientId = OAuthConfig.getClientId();
            String clientSecret = OAuthConfig.getClientSecret();
            
            if (clientId.endsWith(".apps.googleusercontent.com")) {
                System.out.println("✅ Client ID format appears correct");
            }
            
            if (clientSecret.startsWith("GOCSPX-")) {
                System.out.println("✅ Client Secret format appears correct");
            }
            
            System.out.println("\n📝 Google Cloud Console Requirements:");
            System.out.println("   - Authorized Redirect URI: http://127.0.0.1:8080/oauth2callback");
            System.out.println("   - Application Type: Desktop Application");
            System.out.println("   - Scopes: openid, email, profile");
            
        } else {
            System.out.println("\n❌ OAuth Configuration Status: NOT READY");
            System.out.println("⚠️ Please check oauth.properties file and ensure valid credentials");
            
            // Provide troubleshooting tips
            System.out.println("\n🔧 Troubleshooting Steps:");
            System.out.println("1. Verify oauth.properties file exists in project root");
            System.out.println("2. Check that Client ID ends with '.apps.googleusercontent.com'");
            System.out.println("3. Verify Client Secret starts with 'GOCSPX-'");
            System.out.println("4. Ensure Google Cloud Console project is properly configured");
        }
        
        System.out.println("\n=====================================");
    }
}
