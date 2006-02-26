#!/bin/sh

# MacOS Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

java -XstartOnFirstThread -Djava.library.path=lib/swt/macos -jar jameica-macos.jar $@
