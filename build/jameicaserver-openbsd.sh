#!/bin/sh

# OpenBSD Start-Script fuer Server-Betrieb.
# Jameica wird hierbei OHNE GUI gestartet.

java -jar jameica-openbsd.jar -d $@
