@echo off
REM CardDemo Quick Start Script

echo.
echo ======================================
echo CardDemo Phase 1 and Phase 2 Setup
echo ======================================
echo.
echo This script will:
echo  1. Install all required tools (Java, Maven, Node.js)
echo  2. Build Phase 1 (Java backend)
echo  3. Install Phase 2 (React) dependencies
echo.
echo Prerequisites: Windows 10+ with admin rights
echo.
pause

echo.
echo Step 1: Installing tools...
powershell -ExecutionPolicy Bypass -File "INSTALL_ALL.ps1"

echo.
echo Step 2: Building CardDemo...
powershell -ExecutionPolicy Bypass -File "RUN_CARDDEMO.ps1"

echo.
echo ======================================
echo Setup Complete!
echo ======================================
echo.
pause
