#!/bin/sh

TERM="xterm"

BASEDIR=$(dirname "$0")
cd "${BASEDIR}"

## Wir verwenden generell nur noch die embedded Java-Version unter MacOS
JAVACMD="${BASEDIR}/jre-macosaarch64/Contents/Home/bin/java"

exec "${JAVACMD}" -Dsun.security.smartcardio.library=/System/Library/Frameworks/PCSC.framework/Versions/Current/PCSC -Xdock:name="Jameica" -Xmx512m -Xss64m -XstartOnFirstThread -jar "${BASEDIR}/jameica-macos64.jar" -o "$@"  >/dev/null
