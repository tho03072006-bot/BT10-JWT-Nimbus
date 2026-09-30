@echo off
setlocal
cd /d "%~dp0"
where mvn.cmd >nul 2>&1
if errorlevel 1 goto wrapper
call mvn.cmd "-Dmaven.repo.local=%USERPROFILE%\.m2\repository" -pl 01-jjwt-bai-giang -Dmaven.test.skip=true spring-boot:run
goto finished
:wrapper
call "%~dp0mvnw.cmd" "-Dmaven.repo.local=%USERPROFILE%\.m2\repository" -pl 01-jjwt-bai-giang -Dmaven.test.skip=true spring-boot:run
:finished
set "APP_EXIT_CODE=%ERRORLEVEL%"
if not "%APP_EXIT_CODE%"=="0" echo Application failed. Read the error above. Check Java 21 and whether the port is already in use.
pause
exit /b %APP_EXIT_CODE%
