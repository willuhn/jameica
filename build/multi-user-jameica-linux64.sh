#!/bin/sh
# Nachfolgende drei Zeilen bitte anpassen
# JAMEICA_DATADIR muss angegeben werden, selbst wenn der Standard-Pfad verwendet wird. 
#    Es reicht dabei den Pfad bis zur jameica.app anzupassen, falls notwendig.
JAMEICA_APP_PATH="/Applications/jameica.app/"
# Pfad zu dem Ort, an dem der jameica-Benutzerordner liegt. Ersetze <DeinUserName> durch Deinen Mac-Benutzernamen. 
#    Eine Liste aller Benutzer kann man sehen, wenn man unter /Users bzw. 
#    /Benutzer sich die dort liegenden Ordner ansieht.
JAMEICA_DATADIR="/Users/<DeinUserName>/Desktop/jameica/"
# Kopiert aus dem Original jameica-Startskript. Entspricht dem Standard aus OSX 10.10
JAVACMD="/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java"

######################################### Ab hier keine Änderungen mehr notwendig
LOCKFILE_MY="${JAMEICA_DATADIR}/my.lock"
# Prüfen, ob Lockfile existiert
if [ -f "$LOCKFILE_MY" ];then #Jameica in Verwendung, da Lock-File existiert
   JAMEICA_USER=$(cat $LOCKFILE_MY)
   echo "**********"
   echo "* Jameica wird gerade benutzt von"
   echo "*      $JAMEICA_USER"
   echo "**********"
   echo "* Jameica kann erst gestartet"
   echo "* werden, wenn obiger Benutzer das"
   echo "* Programm beendet hat."
   echo "**********"
else #Jameica nicht in Verwendung, da das Lock-File nicht existiert
   echo "**********"
   echo "* Jameica wird gestartet"
   echo "**********"
   echo "* ACHTUNG:"
   echo "*    Dieses Fenster NICHT schließen, es wird"
   echo "*    automatisch geschlossen sobald Jameica"
   echo "*    beendet wurde."
   echo "**********"
   echo "${USER}@${HOSTNAME}\r\n" > $LOCKFILE_MY
   # In das .app-Verzeichnis wechseln
   cd $JAMEICA_APP_PATH
   # Jameica nicht über die jameica.app aufrufen sondern mit java das JAR-Archiv aufrufen
   # da sonst der Batch gleich weiterläuft und nicht wartet bis Jameica beendet wurde.
   java -Xmx256m -jar jameica-linux64.jar $@ > access.log
   rm -f $LOCKFILE_MY
fi