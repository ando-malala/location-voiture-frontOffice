@echo off
REM deploy.bat - Build the project and deploy the generated WAR to Tomcat's webapps folder

SETLOCAL ENABLEDELAYEDEXPANSION

REM 1) Determine Tomcat home:
REM Priority: %1 argument, TOMCAT_HOME env, CATALINA_HOME env, known default path
IF "%~1"=="" (
  IF NOT "%TOMCAT_HOME%"=="" (
    SET "CATALINA=%TOMCAT_HOME%"
  ) ELSE IF NOT "%CATALINA_HOME%"=="" (
    SET "CATALINA=%CATALINA_HOME%"
  ) ELSE (
    REM fallback to common local Tomcat installation (adjust if necessary)
    IF EXIST "C:\apache-tomcat-10.1.49\bin\startup.bat" (
      SET "CATALINA=C:\apache-tomcat-10.1.49"
    ) ELSE (
      ECHO Tomcat home not provided. Usage: deploy.bat [path-to-tomcat]
      ECHO Or set environment variable TOMCAT_HOME or CATALINA_HOME.
      EXIT /B 1
    )
  )
) ELSE (
  SET "CATALINA=%~1"
)

ECHO Using Tomcat home: %CATALINA%

REM 2) Build the project (skip tests by default)
echo Building project...
call mvn -DskipTests package
IF ERRORLEVEL 1 (
  ECHO Maven build failed. Aborting.
  EXIT /B 1
)

REM 3) Find the WAR file in target/
SET "WARFILE="
FOR %%F IN (target\*.war) DO SET "WARFILE=%%~fF"

IF "%WARFILE%"=="" (
  ECHO No WAR file found in target\. Please ensure your project is packaged as a WAR for Tomcat deployment.
  ECHO If you intended to deploy a JAR, either change packaging to WAR or use a different deployment strategy.
  EXIT /B 1
)

ECHO Found WAR: %WARFILE%

REM 4) Extract file names
FOR %%F IN ("%WARFILE%") DO (
  SET "WARNAME=%%~nxF"   REM filename with extension, e.g. app.war
  SET "WARBASE=%%~nF"    REM filename without extension, e.g. app
)

REM 5) Stop Tomcat (best-effort)
echo Stopping Tomcat...
IF EXIST "%CATALINA%\bin\shutdown.bat" (
  call "%CATALINA%\bin\shutdown.bat"
  timeout /t 3 /nobreak >nul
) ELSE (
  ECHO shutdown.bat not found under %CATALINA%\bin. Skipping shutdown.
)

REM 6) Remove old deployment if present
echo Removing old deployment if exists...
IF EXIST "%CATALINA%\webapps\%WARNAME%" (
  del /F /Q "%CATALINA%\webapps\%WARNAME%"
)
IF EXIST "%CATALINA%\webapps\%WARBASE%" (
  rmdir /S /Q "%CATALINA%\webapps\%WARBASE%"
)

REM 7) Copy new WAR
echo Deploying %WARNAME% to Tomcat webapps...
copy /Y "%WARFILE%" "%CATALINA%\webapps\" >nul
IF ERRORLEVEL 1 (
  ECHO Failed to copy WAR to %CATALINA%\webapps\. Aborting.
  EXIT /B 1
)

REM 8) Start Tomcat
echo Starting Tomcat...
IF EXIST "%CATALINA%\bin\startup.bat" (
  call "%CATALINA%\bin\startup.bat"
) ELSE (
  ECHO startup.bat not found under %CATALINA%\bin. Deployment copied but Tomcat was not started.
)

ECHO Deployment finished successfully.
ENDLOCAL
EXIT /B 0























































