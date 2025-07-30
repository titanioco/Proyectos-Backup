@echo off
echo Starting AI Assistant with Enhanced Features...
echo.
echo Features Available:
echo - Conversation History Saving (HTML Export)
echo - Academic Mode for Student Support
echo - Paid/Free Model Indicators
echo - Real-time AI Chat with OpenAI
echo - Document Analysis (TXT files)
echo.

cd /d "%~dp0"
java -cp "build\classes;lib\*" com.raven.ui.AIAssistantFrame

if %errorlevel% neq 0 (
    echo.
    echo Error: Failed to start AI Assistant
    echo Make sure the project is compiled and dependencies are available
    pause
)
