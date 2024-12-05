@echo off

REM run the loading view
start "" /B java -jar ApplicationLoading.jar

REM run the server
start "" FrameProcessorServer.exe

REM Run the main Java JAR file
java --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.swing --add-opens java.base/sun.launcher=ALL-UNNAMED -Dprism.order=sw -Dprism.forceGPU=false -jar FrameHopper.jar