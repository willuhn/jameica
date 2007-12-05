#!/bin/sh

# Linux Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

cd `dirname $0`

#_JCONSOLE="-Dcom.sun.management.jmxremote"

java -Xmx128m $_JCONSOLE -Djava.library.path=lib/swt/linux -jar jameica-linux.jar $@
