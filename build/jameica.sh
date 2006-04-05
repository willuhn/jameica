#!/bin/sh

# Linux Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

cd `dirname $0`

java -Djava.library.path=lib/swt/linux -jar jameica-linux.jar $@
