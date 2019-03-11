@echo off
if "%windir%" == "" goto nowin
regsvr32 %windir%\FreeCBR.dll /u
del /q %windir%\FreeCBR.dll
goto exit

:nowin
regsvr32 FreeCBR.dll /u

:exit
