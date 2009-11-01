#!/bin/sh

# Linux Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

cd `dirname $(readlink -f $0)`

#_JCONSOLE="-Dcom.sun.management.jmxremote"

# https://www.willuhn.de/bugzilla/show_bug.cgi?id=774
DK_NATIVE_WINDOWS=1

java -Xmx256m $_JCONSOLE -jar jameica-linux.jar $@
