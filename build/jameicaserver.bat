@ECHO OFF

REM Windows Start-Script fuer Server-Betrieb.
REM Jameica wird hierbei OHNE GUI gestartet.

java.exe -jar jameica-win32.jar -d %1 %2 %3 %4 %5 %6 %7
