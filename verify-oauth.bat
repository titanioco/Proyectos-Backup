@echo off
echo 🔍 OAuth Configuration Verification
echo ===================================

echo.
echo 📋 Checking oauth.properties file...
if exist "oauth.properties" (
    echo ✅ oauth.properties file found
    echo.
    echo 🔑 Client ID Check:
    findstr "google.oauth.client.id=" oauth.properties >nul
    if errorlevel 1 (
        echo ❌ Client ID not found in oauth.properties
        echo Please add: google.oauth.client.id=YOUR_ACTUAL_CLIENT_ID_HERE
    ) else (
        echo ✅ Client ID property found
        findstr "google.oauth.client.id=" oauth.properties | findstr "YOUR_CLIENT_ID_HERE YOUR_GOOGLE_CLIENT_ID" >nul
        if errorlevel 1 (
            echo ✅ Client ID appears to be configured
        ) else (
            echo ⚠️ Client ID still has placeholder value
        )
    )
    
    echo.
    echo 🔐 Client Secret Check:
    findstr "google.oauth.client.secret=" oauth.properties >nul
    if errorlevel 1 (
        echo ❌ Client Secret not found in oauth.properties
        echo Please add: google.oauth.client.secret=YOUR_ACTUAL_CLIENT_SECRET_HERE
    ) else (
        echo ✅ Client Secret property found
        findstr "google.oauth.client.secret=" oauth.properties | findstr "YOUR_CLIENT_SECRET_HERE YOUR_GOOGLE_CLIENT_SECRET" >nul
        if errorlevel 1 (
            echo ✅ Client Secret appears to be configured
        ) else (
            echo ⚠️ Client Secret still has placeholder value
        )
    )
) else (
    echo ❌ oauth.properties file not found!
    echo.
    echo 📝 To create oauth.properties:
    echo 1. Copy oauth.properties.template to oauth.properties ✅ DONE
    echo 2. Get OAuth credentials from Google Cloud Console
    echo 3. Replace YOUR_CLIENT_ID_HERE with your actual Google OAuth Client ID
    echo 4. For desktop apps: Leave client secret empty
    echo 5. For web apps: Add your client secret
    echo 6. See GOOGLE_OAUTH_SETUP_GUIDE.md for detailed instructions
)

echo.
echo 🚀 Next Steps:
echo 1. If all checks pass, run: ant clean compile
echo 2. Then run: ant run
echo 3. Click "Sign in with Google" to test OAuth flow
echo.
echo ⚠️  SECURITY NOTE: Never commit oauth.properties to git!
echo    It should be in .gitignore to protect your credentials.
echo.
pause
