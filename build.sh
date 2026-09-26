@echo off
REM JANSAARTHI Build Script for Windows
setlocal EnableDelayedExpansion

REM Find JAVA_HOME
where java >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    FOR /F "tokens=*" %%i IN ('registry query "HKLM\SOFTWARE\JavaSoft\Java Development Kit" /v JavaHome') DO SET JAVA_HOME="%%i"
)

REM Set PATH to include JAVA_HOME/bin if exists
if exist "%JAVA_HOME%\bin\java.exe" (
    set PATH=%JAVA_HOME%\bin;%PATH%
)

REM Clean and build project
call gradlew.bat clean assembleDebug --no-daemon

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================
    echo Build Complete!
    echo APK location: app\build\outputs\apk\debug\app-debug.apk
    echo ============================================
) else (
    echo.
    echo Build failed. Check logcat for errors.
)
