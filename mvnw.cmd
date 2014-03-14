@REM ----------------------------------------------------------------------------
@REM Maven Wrapper for Windows - simplified stub
@REM ----------------------------------------------------------------------------
@echo off
set BASE_DIR=%~dp0
set WRAPPER_JAR=%BASE_DIR%.mvn\wrapper\maven-wrapper.jar
if not exist "%WRAPPER_JAR%" (
    echo Downloading Maven Wrapper...
    javac "%BASE_DIR%.mvn\wrapper\MavenWrapperDownloader.java"
    pushd "%BASE_DIR%.mvn\wrapper"
    java MavenWrapperDownloader "%BASE_DIR%"
    popd
)
java -jar "%WRAPPER_JAR%" %*
