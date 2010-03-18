@ECHO OFF

REM Windows Start-Script (64Bit) fuer regulaeren Standalone-Betrieb.
REM Jameica wird hierbei mit GUI gestartet.

start javaw.exe -Xmx256m -jar jameica-win64.jar %1 %2 %3 %4 %5 %6 %7
