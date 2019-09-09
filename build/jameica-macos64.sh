#!/bin/sh

# MacOS64 Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

TERM="xterm"

JAVACMD="$JAVA_HOME/bin/java"

# use Apple's "/usr/libexec/java_home" utility to get installed JVMs
if [ ! -x $JAVACMD ] && [ -x /usr/libexec/java_home ] && /usr/libexec/java_home -F 2> /dev/null; then
	JAVACMD="`/usr/libexec/java_home 2> /dev/null`/bin/java"
fi

# try Oracle JRE (which now only is installed as internet plugin)
if [ ! -x $JAVACMD ]; then
	JAVACMD="/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java"
fi

# Siehe Mail von Volker vom 31.12.2014
if [ ! -x "$JAVACMD" ]; then
  JAVACMD="/System/Library/Frameworks/JavaVM.framework/Versions/Current/Commands/java"
fi

# try old Apple Java
if [ ! -x $JAVACMD ] && [ -h /Library/Java/Home ]; then
	JAVACMD="/Library/Java/Home/bin/java"
fi

# last resort - does not really help on mac as java in $PATH is a wrapper
if [ ! -x "$JAVACMD" ]; then
  JAVACMD="`which java`"
fi

BASEDIR=$(dirname "$0")
cd "${BASEDIR}"

if [ ! -x "$JAVACMD" ]; then
  JAVACMD="${BASEDIR}/jre-macos64/Contents/Home/bin/java"
fi


exec "${JAVACMD}" -Xdock:name="Jameica" -Xmx512m -Xss64m -XstartOnFirstThread -jar "${BASEDIR}/jameica-macos64.jar" -o "$@"  >/dev/null
