@echo off
echo ====================================
echo  University Project Management System
echo ====================================
echo.

echo Checking Java installation...
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java from https://adoptium.net/
    pause
    exit /b 1
)

echo Java found! Building and running the application...
echo.

echo Cleaning previous build...
ant clean >nul 2>&1

echo Compiling source code...
ant compile
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Compilation failed
    pause
    exit /b 1
)

echo Starting the application...
echo.
echo NOTE: If you want to use Google OAuth, please configure oauth.properties
echo       with your Google Cloud credentials first.
echo.
ant run

pause
