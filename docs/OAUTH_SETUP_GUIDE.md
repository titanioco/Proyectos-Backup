# OAuth Credentials Setup Guide

## 🔒 SECURITY WARNING
**This guide shows you how to set up OAuth credentials safely. Never commit real credentials to git!**

## 📋 Prerequisites
1. Google Cloud Console access
2. A Google Cloud Project
3. OAuth 2.0 credentials configured

## 🔧 Setup Steps

### Step 1: Google Cloud Console Setup
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project or create a new one
3. Enable the Google+ API or Google OAuth2 API
4. Go to "Credentials" → "Create Credentials" → "OAuth 2.0 Client IDs"
5. Choose "Desktop application" as the application type
6. Set authorized redirect URIs:
   - `http://127.0.0.1:8080/oauth2callback`
   - `http://localhost:8080/oauth2callback`

### Step 2: Download Credentials
1. Download the JSON file from Google Cloud Console
2. The file will contain your `client_id` and `client_secret`

### Step 3: Configure Local Application
1. Copy `oauth.properties.template` to `oauth.properties`
2. Edit `oauth.properties` and replace placeholders:
   ```properties
   google.oauth.client.id=YOUR_ACTUAL_CLIENT_ID_FROM_GOOGLE
   google.oauth.client.secret=YOUR_ACTUAL_CLIENT_SECRET_FROM_GOOGLE
   google.oauth.redirect.uri=http://127.0.0.1:8080/oauth2callback
   google.oauth.scope=openid email profile
   ```

### Step 4: Verify Configuration
1. Run `verify-oauth.bat` to check your setup
2. The script will verify that your credentials are properly configured

### Step 5: Test OAuth Flow
1. Compile the application: `ant clean compile`
2. Run the application: `ant run`
3. Click "Sign in with Google"
4. Complete the OAuth flow in your browser

## 🔐 Security Best Practices
- ✅ `oauth.properties` is in `.gitignore` - never commit it!
- ✅ Use placeholder values in template files
- ✅ Keep real credentials only in local `oauth.properties`
- ✅ Regularly rotate OAuth credentials if compromised

## 🛠️ Troubleshooting
1. **"OAuth not configured" error**: Check that `oauth.properties` exists and has real values
2. **"Invalid redirect URI" error**: Verify redirect URI matches Google Cloud Console settings
3. **Port conflicts**: The app will try ports 8080, 8081, 3000 automatically

## 📁 File Structure
```
oauth.properties.template  ✅ Safe to commit (has placeholders)
oauth.properties          ❌ NEVER COMMIT (has real credentials)
verify-oauth.bat          ✅ Safe to commit (no credentials)
```

## ⚠️ If You Need to Restore on Another Machine
1. Clone the repository (oauth.properties won't be included)
2. Copy your backed-up `oauth.properties` file
3. Run `verify-oauth.bat` to confirm setup
4. Test the application

---
**Remember: This guide contains no real credentials - it's safe to include in the repository.**
