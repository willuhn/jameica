#!/bin/sh

# Linux Start-Script fuer Server-Betrieb, jedoch 64-Bit Version fuer Athlon64.
# Jameica wird hierbei OHNE GUI gestartet.

cd `dirname $(readlink -f $0)`

#_JCONSOLE="-Dcom.sun.management.jmxremote"

java -Xmx256m $_JCONSOLE -jar jameica-linux-amd64.jar -d $@
