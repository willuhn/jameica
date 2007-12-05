#!/bin/sh

# Linux Start-Script fuer Server-Betrieb.
# Jameica wird hierbei OHNE GUI gestartet.

cd `dirname $0`

#_JCONSOLE="-Dcom.sun.management.jmxremote"

java -Xmx128m $_JCONSOLE -jar jameica-linux.jar -d $@
