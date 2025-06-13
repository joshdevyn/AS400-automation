@echo off
echo Starting AS400 Simulator Server...
cd c:\AS400-automation\automation

:: Compile the server if needed
echo Compiling AS400 Simulator Server...
mvn compile test-compile -q

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

:: Start the server
echo Starting server on port 23 (Telnet)...
echo Press Ctrl+C to stop the server
java -cp "target/test-classes;target/classes;%USERPROFILE%\.m2\repository\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar;%USERPROFILE%\.m2\repository\ch\qos\logback\logback-classic\1.2.12\logback-classic-1.2.12.jar;%USERPROFILE%\.m2\repository\ch\qos\logback\logback-core\1.2.12\logback-core-1.2.12.jar" ro.nn.qa.automation.server.AS400SimulatorServer 23

pause
