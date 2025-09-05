@echo off
REM L2Loot Setup Script for Windows
REM This script sets up the L2Loot CLI tool in the project directory

echo L2Loot Setup Script
echo ====================

REM Check if Java 17+ is installed
echo Checking Java installation...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH.
    echo Please install Java 17+ from:
    echo   - Oracle JDK: https://www.oracle.com/java/technologies/downloads/
    echo   - OpenJDK: https://adoptium.net/
    echo   - Or use chocolatey: choco install openjdk17
    pause
    exit /b 1
)

REM Get Java version (simplified check)
echo [OK] Java found:
java -version 2>&1 | findstr /C:"version"

REM Get current directory (project directory)
set "PROJECT_DIR=%CD%"
set "BIN_DIR=%PROJECT_DIR%\bin"

echo Setting up L2Loot in project directory...
if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"
if not exist "%PROJECT_DIR%\database" mkdir "%PROJECT_DIR%\database"

REM Build the application if artifacts don't exist
if not exist "app\build\libs\l2loot.jar" (
    echo Build artifacts not found. Building application...
    if exist "gradlew.bat" (
        call gradlew.bat build
    ) else (
        gradle build
    )
    
    if not exist "app\build\libs\l2loot.jar" (
        echo [ERROR] Build failed. Please check for build errors.
        pause
        exit /b 1
    )
)

REM Create wrapper script in project bin directory
set "WRAPPER_SCRIPT=%BIN_DIR%\l2loot.bat"
echo @echo off > "%WRAPPER_SCRIPT%"
echo REM L2Loot wrapper script - runs from project directory >> "%WRAPPER_SCRIPT%"
echo set PROJECT_ROOT=%PROJECT_DIR% >> "%WRAPPER_SCRIPT%"
echo cd /D "%%PROJECT_ROOT%%" >> "%WRAPPER_SCRIPT%"
echo java -jar "app\build\libs\l2loot.jar" %%* >> "%WRAPPER_SCRIPT%"

REM Initialize database with seed data
echo Initializing database with NPC and price data...
cd /D "%PROJECT_DIR%"
java -jar "app\build\libs\l2loot.jar" --seed-if-empty >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Database initialized successfully
) else (
    echo [WARNING] Database initialization had issues. You can retry with: .\bin\l2loot.bat --seed-if-empty
)

echo [OK] L2Loot set up successfully in project directory

echo.
echo PATH Configuration Options:
echo.
echo Option 1: Add project bin directory to your PATH (recommended)
echo   Run this in PowerShell:
echo   [Environment]::SetEnvironmentVariable("Path", [Environment]::GetEnvironmentVariable("Path", "User") + ";%BIN_DIR%", "User")
echo   Then restart Command Prompt or PowerShell
echo.
echo Option 2: Run directly from project directory
echo   cd %PROJECT_DIR%
echo   .\bin\l2loot.bat --help
echo.
echo Option 3: Create a batch file in a directory that's already in PATH
echo   Create a batch file that calls "%BIN_DIR%\l2loot.bat" %%*
echo.
echo [OK] Setup complete!
echo.
echo Quick start:
echo   .\bin\l2loot.bat --help                    # Show help
echo   .\bin\l2loot.bat farm-analysis --min-level 20 --max-level 40  # Find profitable mobs
echo   .\bin\l2loot.bat update-prices             # Update prices after editing seed-data\sellable_items.json
echo.
echo Your data files are in the project directory:
echo   - Database: .\database\
echo   - Seed data: .\seed-data\
echo.
echo For detailed usage instructions, see the README.md file.
echo.
pause
