#!/bin/sh

# Open-BSD Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

link=$(readlink -f "$0")
dir=$(dirname "$link")
cd "$dir" 

export JDK_JAVA_OPTIONS='--add-modules=java.se.ee'
java -Xmx512m -Xss64m -jar jameica-openbsd.jar $@
