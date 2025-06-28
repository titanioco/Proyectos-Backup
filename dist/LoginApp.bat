@echo off
title Login Application
echo Starting Login Application...

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Java is not installed or not in PATH!
    echo Please install Java 11 or higher.
    pause
    exit /b 1
)

REM Run the application
java -Xmx512m -jar "LoginApp-portable.jar"

if %errorlevel% neq 0 (
    echo Application encountered an error!
    pause
)
