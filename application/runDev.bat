@echo off

REM run the loading view
start "" /B java -jar ApplicationLoading.jar

REM run the server
start "" FrameProcessorServer.exe

REM Wait for a few seconds to allow the server to start
timeout /t 5 /nobreak

REM Run the main Java JAR file
java --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.swing --add-opens java.base/sun.launcher=ALL-UNNAMED -Dprism.order=sw -Dprism.forceGPU=false -jar FrameHopper.jar