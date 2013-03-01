#!/bin/sh

# MacOS Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

TERM="xterm"

JAVACMD="/usr/libexec/java_home/../java"

if [ ! -x "$JAVACMD" ]; then
  JAVACMD="/System/Library/Frameworks/JavaVM.framework/Versions/Current/Commands/java"
fi

# Java 7 - siehe http://docs.oracle.com/javase/7/docs/webnotes/install/mac/mac-jre.html
# BUGZILLA 1337 - Gott, deren Pfad-Angaben werden ja auch immer schlimmer
if [ ! -x "$JAVACMD" ]; then
  JAVACMD="/Library/Internet\ Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java"
fi

if [ ! -x "$JAVACMD" ]; then
  JAVACMD="`which java`"
fi

if [ ! -x "$JAVACMD" ]; then
  echo Fehler: Java nicht gefunden.
  exit 1
fi

BASEDIR=$(dirname "$0")
cd "${BASEDIR}"

exec ${JAVACMD} -Xdock:name="Jameica" -Xmx256m -XstartOnFirstThread -jar "${BASEDIR}/jameica-macos.jar" -o "$@"  >/dev/null
