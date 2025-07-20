# Google OAuth Setup Guide

## ✅ **Current Status:**
Your NEW OAuth Client ID has been successfully configured in the application:
```
639717052187-nao616khtqhnt7a1lesq2plo0eu3vaok.apps.googleusercontent.com
```

## 🔧 **Required Google Cloud Console Configuration:**

### **Step 1: Configure Authorized Redirect URIs**
In your Google Cloud Console OAuth 2.0 Client:

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Navigate to **APIs & Services** → **Credentials**
3. Click on your OAuth 2.0 client ID: `639717052187-nao616khtqhnt7a1lesq2plo0eu3vaok`
4. In the **Authorized redirect URIs** section, add:
   ```
   http://localhost:8080/oauth/callback
   ```
5. Click **Save**

### **Step 2: Enable Required APIs**
1. Go to **APIs & Services** → **Library**
2. Search for and enable:
   - **Google+ API** (for user profile)
   - **Google OAuth2 API** (for authentication)

### **Step 3: Test OAuth Flow**
1. Run the application (currently running ✅)
2. Try to register/login with Google
3. The application should:
   - Open your browser
   - Show Google sign-in page
   - Redirect back to the app after authorization

## 🚨 **Important Notes:**

### **Redirect URI Must Match Exactly:**
- Application expects: `http://localhost:8080/oauth/callback`
- Google Console must have: `http://localhost:8080/oauth/callback`
- No trailing slashes, must be exactly the same

### **OAuth Consent Screen:**
If you haven't configured it yet:
1. Go to **APIs & Services** → **OAuth consent screen**
2. Choose **External** (for testing)
3. Fill in:
   - App name: "Interactive Data Structures Learning Suite"
   - User support email: Your email
   - Developer contact: Your email
4. Add test users (your email) in the **Test users** section

## 🧪 **Testing:**

### **Expected Behavior:**
1. Click "Sign in with Google" in the app
2. Browser opens to Google's OAuth page
3. User grants permissions
4. Browser redirects to `http://localhost:8080/oauth/callback?code=...`
5. App receives authorization code and completes login

### **Common Issues:**
- **400 redirect_uri_mismatch**: URI in Console doesn't match app
- **403 access_blocked**: App not approved or user not in test users
- **401 invalid_client**: Client ID incorrect or project not configured

## ✅ **Current Configuration:**
```properties
Client ID: 639717052187-nao616khtqhnt7a1lesq2plo0eu3vaok.apps.googleusercontent.com
Redirect URI: http://localhost:8080/oauth/callback
Scopes: openid email profile
Auth URL: https://accounts.google.com/o/oauth2/v2/auth
Token URL: https://oauth2.googleapis.com/token
```

The OAuth client ID is now properly configured in your application! Complete the Google Cloud Console setup steps above and you'll have fully functional Google OAuth authentication.
