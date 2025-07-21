# Project Setup Guide

## Prerequisites
- Java 11 or higher
- Apache Ant
- Google Cloud Console project with OAuth2 configured

## Configuration Steps

1. **Google OAuth Setup:**
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create a new project or select existing one
   - Enable Google+ API
   - Create OAuth 2.0 credentials (Desktop Application)
   - Add redirect URI: `http://127.0.0.1:8080/oauth2callback`

2. **Application Configuration:**
   ```bash
   # Copy the template configuration
   cp oauth.properties.template oauth.properties
   
   # Edit oauth.properties with your actual credentials
   # Replace YOUR_CLIENT_ID_HERE and YOUR_CLIENT_SECRET_HERE
   ```

3. **Build and Run:**
   ```bash
   ant clean compile
   ant run
   ```

## Security Notes
- Never commit `oauth.properties` with real credentials
- Keep your client secret secure
- Regularly rotate your OAuth credentials