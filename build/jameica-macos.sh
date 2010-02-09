#!/bin/sh

# MacOS Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

TERM="xterm"
JAVACMD=""

for i in "1.4.2" "1.5" "1.6"; do
  [ -x "/System/Library/Frameworks/JavaVM.framework/Versions/${i}/Commands/java" ] && \
    JAVACMD="/System/Library/Frameworks/JavaVM.framework/Versions/${i}/Commands/java"
done

if [ -z "$JAVACMD" ]; then
  echo Fehler: Es wird Java 1.4.2 oder 1.5 benoetigt.
  exit 1
fi

BASEDIR=$(dirname "$0")
cd "${BASEDIR}"

exec ${JAVACMD} -d32 -Xdock:name="Jameica" -Xmx256m -XstartOnFirstThread -jar "${BASEDIR}/jameica-macos.jar" -o $@  >/dev/null
