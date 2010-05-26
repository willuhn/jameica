#!/bin/sh

# MacOS Start-Script fuer Server-Betrieb.
# Jameica wird hierbei OHNE GUI gestartet.

JAVAVERSION="`readlink /System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK`"
JAVACMD="/System/Library/Frameworks/JavaVM.framework/Versions/${JAVAVERSION}/Commands/java"

if [ -z "$JAVACMD" ]; then
  echo Fehler: Java nicht installiert.
  exit 1
fi

BASEDIR=$(dirname "$0")
cd "${BASEDIR}"

${JAVACMD} -Xmx256m -XstartOnFirstThread -jar "${BASEDIR}/jameica-macos64.jar" -d -o $@
