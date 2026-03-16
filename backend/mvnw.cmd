@REM Maven Wrapper script for Windows
@REM This script downloads and runs Maven Wrapper

@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
set MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar
set MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties

if exist "%MAVEN_WRAPPER_JAR%" (
    "%JAVA_HOME%\bin\java" -jar "%MAVEN_WRAPPER_JAR%" %*
) else (
    mvn %*
)

endlocal
