#!/bin/sh

# OpenBSD Start-Script fuer Server-Betrieb.
# Jameica wird hierbei OHNE GUI gestartet.

cd `dirname $(readlink -f $0)`

java -Xmx128m -jar jameica-openbsd.jar -d $@
