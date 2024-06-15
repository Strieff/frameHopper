@echo off

REM run the loading view
start "" /B java -jar ApplicationLoading.jar

REM run the server
start "" FrameProcessorServer.exe

REM Wait for a few seconds to allow the server to start
timeout /t 5 /nobreak

REM Run the main Java JAR file
java -jar FrameHopper.jar