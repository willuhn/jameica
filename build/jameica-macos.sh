#!/bin/sh

# MacOS Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

TERM=xterm

BASEDIR=`dirname "$0"`
cd "$BASEDIR"
exec java -Xmx128m -XstartOnFirstThread -Djava.library.path="$BASEDIR/lib/swt/macos" -jar "$BASEDIR/jameica-macos.jar" -o $@
