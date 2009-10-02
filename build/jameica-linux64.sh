#!/bin/sh

# Linux Start-Script fuer regulaeren Standalone-Betrieb, jedoch 64-Bit Version fuer Athlon64.
# Jameica wird hierbei mit GUI gestartet.

cd `dirname $(readlink -f $0)`

java -Xmx256m -jar jameica-linux64.jar $@
