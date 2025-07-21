@echo off
echo 🛡️ Security Verification - Pre-Commit Check
echo =============================================

echo.
echo 🔍 Checking for credential leaks...

echo.
echo ✅ Checking .gitignore protection:
if exist ".gitignore" (
    findstr "oauth.properties" .gitignore >nul
    if errorlevel 1 (
        echo ❌ oauth.properties NOT protected in .gitignore
        echo ⚠️  SECURITY RISK: Credentials may be committed!
    ) else (
        echo ✅ oauth.properties is git-ignored (SECURE)
    )
) else (
    echo ❌ No .gitignore file found!
    echo ⚠️  SECURITY RISK: Create .gitignore to protect credentials
)

echo.
echo 🔒 Checking for exposed credentials:
git status --porcelain 2>nul | findstr "oauth.properties" >nul
if errorlevel 1 (
    echo ✅ No oauth.properties files staged for commit (SECURE)
) else (
    echo ❌ WARNING: oauth.properties files are staged for commit!
    echo ⚠️  SECURITY RISK: Remove them before committing!
    echo.
    echo 💡 Fix with: git reset HEAD oauth.properties*
)

echo.
echo 📄 Checking current staged files:
git status --porcelain 2>nul | findstr /E "\.properties$\|\.credentials$\|\.secret$\|\.key$" >nul
if errorlevel 1 (
    echo ✅ No credential files detected in staging area
) else (
    echo ❌ WARNING: Potential credential files found in staging:
    git status --porcelain 2>nul | findstr /E "\.properties$\|\.credentials$\|\.secret$\|\.key$"
    echo ⚠️  Review these files before committing!
)

echo.
echo 🔐 Configuration status:
if exist "oauth.properties" (
    findstr "YOUR_GOOGLE_CLIENT_ID\|YOUR_GOOGLE_CLIENT_SECRET\|YOUR_CLIENT_ID_HERE\|YOUR_CLIENT_SECRET_HERE" oauth.properties >nul
    if errorlevel 1 (
        echo ✅ oauth.properties contains real credentials (CONFIGURED)
    ) else (
        echo ⚠️  oauth.properties still has placeholder values
        echo 💡 Replace placeholders with real Google OAuth credentials
    )
) else (
    echo ⚠️  oauth.properties not found
    echo 💡 Copy oauth.properties.template to oauth.properties and configure
)

echo.
echo 🚀 Overall Status:
if exist "oauth.properties" (
    findstr "oauth.properties" .gitignore >nul
    if errorlevel 1 (
        echo ❌ SECURITY RISK: oauth.properties exists but not git-ignored!
    ) else (
        git status --porcelain 2>nul | findstr "oauth.properties" >nul
        if errorlevel 1 (
            echo ✅ SECURE: Ready for safe repository sync
        ) else (
            echo ❌ SECURITY RISK: oauth.properties staged for commit!
        )
    )
) else (
    echo ✅ SECURE: No credential files to leak
)

echo.
echo 📚 Quick Commands:
echo   • To unstage credential files: git reset HEAD *.properties
echo   • To check git status: git status
echo   • To view .gitignore: type .gitignore
echo.
pause
