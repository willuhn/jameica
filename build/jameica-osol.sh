#!/bin/sh

# OpenSolaris Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

link=$(readlink -f "$0")
dir=$(dirname "$link")
cd "$dir" 

java -Xmx256m -Djava.library.path=/usr/lib/swt -jar jameica-osol.jar $@
