@echo off
cd /d c:\Gouri\document\carddemo-modernization
mvn clean compile
echo BUILD_RESULT=%ERRORLEVEL%
pause
