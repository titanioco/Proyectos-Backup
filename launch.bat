@echo off
cls
echo ============================================
echo  University Project Management System v2.0
echo ============================================
echo.
echo Select which application to run:
echo.
echo 1) Main University System (Data Structures)
echo 2) AI Assistant (Enhanced Academic Helper)
echo 3) Academic AI Test (Development Testing)
echo 4) Exit
echo.
set /p choice="Enter your choice (1-4): "

if "%choice%"=="1" goto main_system
if "%choice%"=="2" goto ai_assistant
if "%choice%"=="3" goto ai_test
if "%choice%"=="4" goto exit
goto invalid_choice

:main_system
echo.
echo Starting Main University System...
call run.bat
goto end

:ai_assistant
echo.
echo Starting AI Assistant with Enhanced Features...
echo.
echo Available Features:
echo - Real-time AI Chat with OpenAI
echo - Conversation History Saving (HTML Export)
echo - Academic Mode for Engineering Students
echo - Paid/Free Model Indicators
echo - Document Analysis (TXT files)
echo - Calculus and Japanese Learning Support
echo.
echo Compiling latest version...
ant compile > nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Compilation failed
    pause
    goto end
)
echo Starting AI Assistant...
java -cp "build\classes;lib\*" com.raven.ui.AIAssistantFrame
goto end

:ai_test
echo.
echo Running Academic AI Test Suite...
echo This will test the AI with specific academic scenarios:
echo - Calculus help for engineering students
echo - Japanese learning assistance
echo - Code development support
echo.
ant compile > nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Compilation failed
    pause
    goto end
)
java -cp "build\classes;lib\*" com.raven.test.AcademicAITest
pause
goto end

:invalid_choice
echo.
echo Invalid choice. Please enter 1, 2, 3, or 4.
pause
goto start

:exit
echo.
echo Goodbye!
goto end

:start
goto :eof

:end
echo.
echo Press any key to exit...
pause > nul
