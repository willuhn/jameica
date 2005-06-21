#!/bin/sh

# Open-BSD Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

java -Djava.library.path=lib/swt/openbsd -jar jameica-openbsd.jar $@
