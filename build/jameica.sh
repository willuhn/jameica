#!/bin/sh

# Linux Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

cd `dirname $(readlink -f $0)`

#_JCONSOLE="-Dcom.sun.management.jmxremote"

java -Xmx256m $_JCONSOLE -jar jameica-linux.jar $@
