@echo off
REM ========================================
REM Interactive Data Structures Suite - Secure Startup
REM Cleans OAuth credentials before starting application
REM ========================================

echo 🚀 Starting Interactive Data Structures Learning Suite...
echo ============================================================

REM Clean up any leftover OAuth credentials from previous sessions
echo 🧹 Pre-startup cleanup...
call cleanup-oauth.bat

echo.
echo 🔧 Compiling application...
ant clean compile

if errorlevel 1 (
    echo ❌ Compilation failed!
    echo 💡 Check the compile.log file for details
    pause
    exit /b 1
)

echo ✅ Compilation successful!
echo.
echo 🚀 Starting application...
echo 📝 Note: OAuth credentials will be automatically cleaned up on exit

REM Start the application
ant run

REM Clean up after application closes (fallback)
echo.
echo 🧹 Post-run cleanup...
call cleanup-oauth.bat

echo.
echo 👋 Application closed. OAuth credentials cleaned up.
pause
