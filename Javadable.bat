@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"
rem Starts the game. A JDK is needed, because the game compiles your code while you play.

set "JDK=%JAVA_HOME%"
if not exist "%JDK%\bin\java.exe" set "JDK=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
if not exist "%JDK%\bin\java.exe" (
  for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk-*") do set "JDK=%%d"
)
if not exist "!JDK!\bin\java.exe" (
  echo Could not find a JDK. Install one, or set JAVA_HOME, then try again.
  pause
  exit /b 1
)

if not exist build\javadable.jar call "%~dp0build.bat"

start "Javadable" "!JDK!\bin\javaw.exe" -jar "%~dp0build\javadable.jar"
