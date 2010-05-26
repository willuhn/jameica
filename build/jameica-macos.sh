#!/bin/sh

# MacOS Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

TERM="xterm"
JAVAVERSION="`readlink /System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK`"
JAVACMD="/System/Library/Frameworks/JavaVM.framework/Versions/${JAVAVERSION}/Commands/java"

if [ -z "$JAVACMD" ]; then
  echo Fehler: Java nicht installiert.
  exit 1
fi

BASEDIR=$(dirname "$0")
cd "${BASEDIR}"

exec ${JAVACMD} -Xdock:name="Jameica" -Xmx256m -XstartOnFirstThread -jar "${BASEDIR}/jameica-macos.jar" -o $@  >/dev/null
