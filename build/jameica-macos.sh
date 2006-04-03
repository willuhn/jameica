#!/bin/sh

# MacOS Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

BASEDIR=`dirname $0`

exec java -XstartOnFirstThread -Djava.library.path=$BASEDIR/lib/swt/macos -jar $BASEDIR/jameica-macos.jar $@