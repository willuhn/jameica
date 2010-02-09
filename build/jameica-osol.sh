#!/bin/sh

# OpenSolaris Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

cd `dirname $(readlink -f $0)`

java -Xmx256m -Djava.library.path=/usr/lib/swt -jar jameica-osol.jar $@
