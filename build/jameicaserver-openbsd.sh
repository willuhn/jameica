#!/bin/sh

# OpenBSD Start-Script fuer Server-Betrieb.
# Jameica wird hierbei OHNE GUI gestartet.

cd `dirname $0`

java -jar jameica-openbsd.jar -d $@
