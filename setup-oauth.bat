@echo off
echo 🚀 Google OAuth Setup Helper
echo ===============================

echo.
echo 📋 Steps to configure Google OAuth:
echo.
echo 1. 🌐 Opening Google Cloud Console...
start https://console.cloud.google.com/apis/credentials

echo.
echo 2. 📝 In Google Cloud Console:
echo    - Create new project or select existing
echo    - Enable Google+ API or People API
echo    - Create OAuth 2.0 Client ID
echo    - Choose "Desktop Application" (recommended)
echo    - Copy the Client ID

echo.
echo 3. ✏️  Edit oauth.properties file:
echo    - Replace YOUR_CLIENT_ID_HERE with your actual Client ID
echo    - Leave client secret empty for desktop apps

echo.
echo 4. 🧪 Test the setup:
echo    - Run: ant clean compile
echo    - Run: ant run
echo    - Click "Sign in with Google"

echo.
echo 📖 For detailed instructions, see: GOOGLE_OAUTH_SETUP_GUIDE.md
echo.
pause
