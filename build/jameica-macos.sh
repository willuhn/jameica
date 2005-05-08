#!/bin/sh

# MacOS Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

java -Djava.library.path=lib/swt/macos -jar jameica-macos.jar $@
