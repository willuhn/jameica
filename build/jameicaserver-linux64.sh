#!/bin/sh

# Linux Start-Script fuer Server-Betrieb, jedoch 64-Bit Version fuer Athlon64.
# Jameica wird hierbei OHNE GUI gestartet.

cd `dirname $(readlink -f $0)`

#_JCONSOLE="-Dcom.sun.management.jmxremote"

# https://www.willuhn.de/bugzilla/show_bug.cgi?id=798

java -Djava.net.preferIPv4Stack=true -Xmx256m $_JCONSOLE -jar jameica-linux64.jar -d $@
