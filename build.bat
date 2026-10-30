@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"
rem Builds Javadable into build\javadable.jar

set "JDK=%JAVA_HOME%"
if not exist "%JDK%\bin\javac.exe" set "JDK=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
if not exist "%JDK%\bin\javac.exe" (
  for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk-*") do set "JDK=%%d"
)
if not exist "!JDK!\bin\javac.exe" (
  echo Could not find a JDK. Set JAVA_HOME to one and run this again.
  pause
  exit /b 1
)

if exist build\classes rmdir /s /q build\classes
mkdir build\classes

set "FILES="
for /r "%~dp0src" %%f in (*.java) do set "FILES=!FILES! "%%f""

"!JDK!\bin\javac.exe" -encoding UTF-8 -d build\classes !FILES!
if errorlevel 1 (
  echo Build failed.
  pause
  exit /b 1
)

"!JDK!\bin\jar.exe" --create --file build\javadable.jar --main-class javadable.Main -C build\classes .
if errorlevel 1 (
  echo Could not package the jar.
  pause
  exit /b 1
)
echo Done: build\javadable.jar
