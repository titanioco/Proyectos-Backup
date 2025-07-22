@echo off
echo ======================================
echo    Google OAuth Setup Helper
echo ======================================
echo.

echo This script will help you configure Google OAuth for your application.
echo.

echo Step 1: Go to Google Cloud Console
echo URL: https://console.cloud.google.com/apis/credentials
echo.
echo Step 2: Create or select a project
echo Step 3: Enable the Google+ API (if not already enabled)
echo Step 4: Create OAuth 2.0 Client ID
echo   - Application type: Desktop application
echo   - Name: Your App Name (e.g., "University Management System")
echo.
echo Step 5: Add redirect URI in OAuth consent screen:
echo   - http://127.0.0.1:8080/oauth2callback
echo.

set /p CLIENT_ID="Enter your Google OAuth Client ID [YOUR_CLIENT_ID.apps.googleusercontent.com]: "

if "%CLIENT_ID%"=="" (
    set CLIENT_ID=YOUR_CLIENT_ID.apps.googleusercontent.com
    echo Using default Client ID: %CLIENT_ID%
)

set /p CLIENT_SECRET="Enter your Google OAuth Client Secret [YOUR_CLIENT_SECRET]: "

if "%CLIENT_SECRET%"=="" (
    set CLIENT_SECRET=YOUR_CLIENT_SECRET
    echo Using default Client Secret: %CLIENT_SECRET%
)

echo.
echo Updating oauth.properties file...

(
echo # Google OAuth Configuration - Auto-generated on %date% %time%
echo # Client ID configured through setup-oauth.bat
echo.
echo google.oauth.client.id=%CLIENT_ID%
echo google.oauth.client.secret=%CLIENT_SECRET%
echo google.oauth.redirect.uri=http://127.0.0.1:8080/oauth2callback
echo google.oauth.scope=openid email profile
echo google.oauth.auth.url=https://accounts.google.com/o/oauth2/v2/auth
echo google.oauth.token.url=https://oauth2.googleapis.com/token
echo google.oauth.userinfo.url=https://www.googleapis.com/oauth2/v2/userinfo
echo.
echo # Configuration completed on %date% %time%
) > oauth.properties

echo.
echo ✅ SUCCESS: OAuth configuration updated!
echo.
echo 📋 Configuration Summary:
echo   - Client ID: %CLIENT_ID%
echo   - Client Secret: %CLIENT_SECRET%
echo   - Redirect URI: http://127.0.0.1:8080/oauth2callback
echo   - Scopes: openid email profile
echo.
echo 🚀 You can now restart your application and test Google Sign-In!
echo.
echo 📖 If you encounter issues, check GOOGLE_OAUTH_SETUP_GUIDE.md
echo.
pause
