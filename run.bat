@echo off
cd /d "%~dp0"
echo ========================================================
echo   Launching Smart Tourism Management System (STMS)
echo ========================================================
java -cp "bin;lib\*" smarttourism.Main
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with an error. Press any key to close.
    pause >nul
)
