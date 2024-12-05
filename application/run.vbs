Set WshShell = CreateObject("WScript.Shell")
strPath = WScript.ScriptFullName
strFolder = Left(strPath, InStrRev(strPath, "\"))

' Run ApplicationLoading.jar in hidden mode
WshShell.Run "java -jar " & chr(34) & strFolder & "ApplicationLoading.jar" & chr(34), 0

' Run FrameProcessorServer.exe in hidden mode
WshShell.Run chr(34) & strFolder & "FrameProcessorServer.exe" & chr(34), 0

' Run FrameHopper.jar with JavaFX modules in hidden mode
javaCommand = "java --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.swing --add-opens java.base/sun.launcher=ALL-UNNAMED -Dprism.order=sw -Dprism.forceGPU=false -jar " & chr(34) & strFolder & "FrameHopper.jar" & chr(34)
WshShell.Run javaCommand, 0

Set WshShell = Nothing