#!/bin/sh

# Linux Start-Script fuer Server-Betrieb.
# Jameica wird hierbei OHNE GUI gestartet.

cd `dirname $0`

java -Xmx128m -jar jameica-linux.jar -d $@
