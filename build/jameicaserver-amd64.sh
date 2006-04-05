#!/bin/sh

# Linux Start-Script fuer Server-Betrieb, jedoch 64-Bit Version fuer Athlon64.
# Jameica wird hierbei OHNE GUI gestartet.

cd `dirname $0`

java -jar jameica-linux-amd64.jar -d $@
