# Google OAuth Configuration
# To enable live Google authentication, follow these steps:

## 1. Create Google Cloud Project
1. Go to https://console.cloud.google.com/
2. Create a new project or select an existing one
3. Enable the Google+ API (or Google Identity API)

## 2. Create OAuth 2.0 Credentials
1. Go to "Credentials" in the left sidebar
2. Click "Create Credentials" > "OAuth 2.0 Client IDs"
3. Choose "Desktop application" as the application type
4. Add authorized redirect URI: http://localhost:8080/oauth/callback

## 3. Configure the Application
1. Copy your Client ID from Google Cloud Console
2. Open src/com/raven/service/GoogleAuthService.java
3. Replace "YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com" with your actual Client ID

## Example Client ID format:
# 123456789-abcdefghijklmnopqrstuvwxyz123456.apps.googleusercontent.com

## Security Notes:
- Keep your Client ID secure
- For production, consider storing credentials in environment variables
- The redirect URI must match exactly what's configured in Google Cloud Console

## Current Status:
The application will show a setup dialog if the Client ID is not configured.
It will fall back to demo mode for testing purposes.
