@echo off

REM run the loading view
start "" /B java -jar ApplicationLoading.jar

REM run the server
start "" /B FrameProcessorServer.exe

REM Wait for a few seconds to allow the server to start
timeout /t 5 /nobreak

REM Run the main Java JAR file
start "" /B java -jar FrameHopper.jar