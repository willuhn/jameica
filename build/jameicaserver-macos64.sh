#!/bin/sh

# MacOS64 Start-Script fuer Server-Betrieb.
# Jameica wird hierbei OHNE GUI gestartet.

cd `dirname "$0"`

java -Xmx256m -jar jameica-macos64.jar -d $@
