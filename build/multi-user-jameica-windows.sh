@echo off
rem Quelle: http://www.jverein.de/wiki/index.php?title=Multiuser#Multiuser-Setup_per_SVN.2FSubversion
rem Nachfolgende drei set-Zeilen bitte anpassen
rem JAMEICA_DATADIR muss angegeben werden, selbst wenn der Standard-Pfad verwendet wird
rem JAMEICA_3264 muss -je nach eigener Umgebung- auf 32 oder 64 eingestellt werden
set JAMEICA_PROGDIR=C:\Program Files (x86)\jameica\
set JAMEICA_DATADIR=C:\Users\<DeinBenutzername>\Documents\jameica\
set JAMEICA_3264=32
rem
rem Ab hier keine Änderungen mehr nötig
set JAMEICA_BIN=jameica-win32.exe
set JAMEICA_JAR=jameica-win32.jar
if %JAMEICA_3264%==64 (
  set JAMEICA_BIN=jameica-win64.exe
  set JAMEICA_JAR=jameica-win64.jar
)
set LOCKFILE_MY="%JAMEICA_DATADIR%\my.lock"
rem Prüfen ob Lockfile exitiert
if exist %LOCKFILE_MY% goto jameica_in_use
  goto jameica_start
:jameica_start
echo **********
echo * Jameica wird gestartet
echo **********
echo * ACHTUNG:
echo *    Dieses Fenster NICHT schliessen, es wird
echo *    automatisch geschlossen sobald Jameica
echo *    beendet wurde.
echo **********
echo %USERNAME%@%COMPUTERNAME% > %LOCKFILE_MY%
rem Jameica nicht mit der Starter-EXE aufrufen sondern mit javaw das JAR-Archiv aufrufen
rem da sonst der Batch gleich weiterläuft und nicht wartet bis Jameica beendet wurde.
cd %JAMEICA_PROGDIR%
javaw -jar "%JAMEICA_PROGDIR%\%JAMEICA_JAR%" -f "%JAMEICA_DATADIR%"
del %LOCKFILE_MY%
goto ende
:jameica_in_use
echo %LOCKFILE_MY%
set /p JAMEICA_USER=<%LOCKFILE_MY%
echo **********
echo * Jameica wird gerade benutzt von
echo *      %JAMEICA_USER%
echo **********
echo * Jameica kann erst gestartet
echo * werden wenn obiger Benutzer das
echo * Programm beendet hat.
echo **********
pause
goto ende
:ende