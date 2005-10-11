@ECHO OFF

REM Windows Start-Script fuer regulaeren Standalone-Betrieb.
REM Jameica wird hierbei mit GUI gestartet.

javaw.exe -Djava.library.path=lib/swt/win32 -jar jameica-win32.jar %1 %2 %3 %4 %5 %6 %7
