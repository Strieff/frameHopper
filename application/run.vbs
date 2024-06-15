Set WshShell = CreateObject("WScript.Shell")
strPath = WScript.ScriptFullName
strFolder = Left(strPath, InStrRev(strPath, "\"))
WshShell.Run chr(34) & strFolder & "runUser.bat" & chr(34), 0
Set WshShell = Nothing