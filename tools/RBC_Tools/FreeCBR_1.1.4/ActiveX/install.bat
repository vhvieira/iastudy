@echo off
if "%windir%" == "" goto nowin
copy FreeCBR.dll %windir%
regsvr32 %windir%\FreeCBR.dll
goto exit

:nowin
regsvr32 FreeCBR.dll

:exit
