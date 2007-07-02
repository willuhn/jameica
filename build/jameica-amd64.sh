#!/bin/sh

# Linux Start-Script fuer regulaeren Standalone-Betrieb, jedoch 64-Bit Version fuer Athlon64.
# Jameica wird hierbei mit GUI gestartet.

cd `dirname $0`

java -Xmx128m -Djava.library.path=lib/swt/linux-amd64 -jar jameica-linux-amd64.jar $@
