#!/bin/sh

TERM="xterm"

JAVACMD="$JAVA_HOME/bin/java"

if [ ! -x $JAVACMD ] && [ -x /usr/libexec/java_home ] && /usr/libexec/java_home -F 2> /dev/null; then
  JAVACMD="`/usr/libexec/java_home 2> /dev/null`/bin/java"
fi

if [ ! -x $JAVACMD ]; then
  JAVACMD="/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java"
fi

if [ ! -x "$JAVACMD" ]; then
  JAVACMD="/System/Library/Frameworks/JavaVM.framework/Versions/Current/Commands/java"
fi

if [ ! -x $JAVACMD ] && [ -h /Library/Java/Home ]; then
  JAVACMD="/Library/Java/Home/bin/java"
fi

if [ ! -x "$JAVACMD" ]; then
  JAVACMD="`which java`"
fi

BASEDIR=$(dirname "$0")
cd "${BASEDIR}"

exec "${JAVACMD}" -Dsun.security.smartcardio.library=/System/Library/Frameworks/PCSC.framework/Versions/Current/PCSC -Xdock:name="Jameica" -Xmx512m -Xss64m -XstartOnFirstThread -jar "${BASEDIR}/jameica-macos-aarch64.jar" -o "$@"  >/dev/null
