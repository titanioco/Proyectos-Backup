@echo off
REM ========================================
REM OAuth Credentials Cleanup Script
REM Automatically removes credential files to prevent git issues
REM ========================================

echo 🧹 Cleaning up OAuth credential files...

REM Define files to clean up (actual credential files, not templates)
set FILES_TO_CLEAN=oauth.properties oauth.properties.fixed oauth.properties.local oauth.properties.backup

REM Clean up OAuth credential files (keep templates and guides)
for %%f in (%FILES_TO_CLEAN%) do (
    if exist "%%f" (
        echo 🗑️  Removing: %%f
        del /f /q "%%f" 2>nul
        if exist "%%f" (
            echo ❌ Failed to remove: %%f
        ) else (
            echo ✅ Removed: %%f
        )
    )
)

REM Clean up any credential backup files
if exist "*.credentials" (
    echo 🗑️  Removing credential backup files...
    del /f /q "*.credentials" 2>nul
)

if exist "*.secret" (
    echo 🗑️  Removing secret files...
    del /f /q "*.secret" 2>nul
)

REM Clean up any generated client secret files
if exist "client_secret*.json" (
    echo 🗑️  Removing client secret JSON files...
    del /f /q "client_secret*.json" 2>nul
)

echo.
echo ✅ OAuth credential cleanup complete!
echo 📝 Note: Template files (oauth.properties.template) are preserved
echo 📖 Guides and documentation are preserved

REM Optional: Show git status after cleanup
if "%1"=="--show-status" (
    echo.
    echo 📊 Git status after cleanup:
    git status --porcelain 2>nul | findstr "oauth\|\.properties\|\.credentials\|\.secret" || echo "✅ No OAuth files in git staging area"
)

exit /b 0
