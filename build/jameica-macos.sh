#!/bin/sh

# MacOS Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

TERM=xterm

if [ -x /System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Commands/java ]; then
  JAVACMD="/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Commands/java"
elif [ -x /System/Library/Frameworks/JavaVM.framework/Versions/1.4.2/Commands/java ]; then
  JAVACMD="/System/Library/Frameworks/JavaVM.framework/Versions/1.4.2/Commands/java"
else
  echo Fehler: Es wird Java 1.4.2 oder 1.5 benoetigt.
  exit 1
fi

BASEDIR=`dirname "$0"`
cd "$BASEDIR"

exec ${JAVACMD} -Xdock:name="Jameica" -Xmx128m -XstartOnFirstThread -Djava.library.path="$BASEDIR/lib/swt/macos" -jar "$BASEDIR/jameica-macos.jar" -o $@  >/dev/null
