#!/bin/sh

# Linux Start-Script fuer Server-Betrieb.
# Jameica wird hierbei OHNE GUI gestartet.

cd `dirname $(readlink -f $0)`

#_JCONSOLE="-Dcom.sun.management.jmxremote"

java -Xmx256m $_JCONSOLE -jar jameica-linux.jar -d $@
