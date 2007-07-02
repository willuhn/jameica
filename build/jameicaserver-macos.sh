#!/bin/sh

# MacOS Start-Script fuer Server-Betrieb.
# Jameica wird hierbei OHNE GUI gestartet.

cd `dirname "$0"`

java -Xmx128m -jar jameica-macos.jar -d $@
