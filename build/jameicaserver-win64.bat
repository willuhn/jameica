@ECHO OFF

REM Windows Start-Script (64 Bit) fuer Server-Betrieb.
REM Jameica wird hierbei OHNE GUI gestartet.

java.exe -Xmx256m -jar jameica-win64.jar -d %1 %2 %3 %4 %5 %6 %7
