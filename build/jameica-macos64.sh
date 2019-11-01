#!/bin/sh

TERM="xterm"

BASEDIR=$(dirname "$0")
cd "${BASEDIR}"

## Wir verwenden generell nur noch die embedded Java-Version unter MacOS
JAVACMD="${BASEDIR}/jre-macos64/Contents/Home/bin/java"

exec "${JAVACMD}" -Xdock:name="Jameica" -Xmx512m -Xss64m -XstartOnFirstThread -jar "${BASEDIR}/jameica-macos64.jar" -o "$@"  >/dev/null
