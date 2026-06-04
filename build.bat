@echo off
chcp 65001 >nul
title Building Captain Simulator Distribution

echo ========================================
echo  Building Captain Simulator
echo ========================================

:: 1. 编译
echo [1/4] Compiling...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo FAILED: Compilation error.
    pause
    exit /b 1
)

:: 2. 创建 dist 目录
echo [2/4] Creating dist directory...
if not exist dist mkdir dist
if not exist dist\lib mkdir dist\lib

:: 3. 复制 jar
echo [3/4] Copying files...
copy /Y target\captain_simulator-1.0-SNAPSHOT.jar dist\CaptainSimulator.jar >nul

set REPO=%USERPROFILE%\.m2\repository

copy /Y "%REPO%\org\openjfx\javafx-base\21\javafx-base-21.jar" dist\lib\ >nul 2>&1
copy /Y "%REPO%\org\openjfx\javafx-base\21\javafx-base-21-win.jar" dist\lib\ >nul 2>&1
copy /Y "%REPO%\org\openjfx\javafx-controls\21\javafx-controls-21.jar" dist\lib\ >nul 2>&1
copy /Y "%REPO%\org\openjfx\javafx-controls\21\javafx-controls-21-win.jar" dist\lib\ >nul 2>&1
copy /Y "%REPO%\org\openjfx\javafx-graphics\21\javafx-graphics-21.jar" dist\lib\ >nul 2>&1
copy /Y "%REPO%\org\openjfx\javafx-graphics\21\javafx-graphics-21-win.jar" dist\lib\ >nul 2>&1
copy /Y "%REPO%\org\openjfx\javafx-fxml\21\javafx-fxml-21.jar" dist\lib\ >nul 2>&1
copy /Y "%REPO%\org\openjfx\javafx-fxml\21\javafx-fxml-21-win.jar" dist\lib\ >nul 2>&1
copy /Y "%REPO%\com\google\code\gson\gson\2.10.1\gson-2.10.1.jar" dist\lib\ >nul 2>&1

:: 4. 生成 run.bat
echo [4/4] Generating run.bat...

(
echo @echo off
echo chcp 65001 ^>nul
echo set DIR=%%~dp0
echo set FX_DIR=%%DIR%%lib
echo java --module-path "%%FX_DIR%%" --add-modules javafx.controls,javafx.fxml -cp "%%DIR%%CaptainSimulator.jar;%%FX_DIR%%\gson-2.10.1.jar" org.captainsim.CaptainSimulatorApp
echo pause
) > dist\run.bat

echo ========================================
echo  DONE! Distribution ready in:
echo  %CD%\dist\
echo ========================================
echo.
echo  Run it: double-click dist\run.bat
echo.
pause
