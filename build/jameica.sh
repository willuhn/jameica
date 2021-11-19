#!/bin/sh

# Linux Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

#_JCONSOLE="-Dcom.sun.management.jmxremote"

link=$(readlink -f "$0")
dir=$(dirname "$link")
cd "$dir" 

_JLP=""

if uname -m |grep -q aarch64; then
	archsuffix="arm64"
	_JLP=" -Djava.library.path=lib/swt/linux-arm64"
elif uname -m |grep -q 64; then
	archsuffix="64"
else
	archsuffix=""
fi

# Zur Anpassung der Skalierung auf HiDPI-Displays kann der Parameter "-Dswt.autoScale" verwendet werden.
# Siehe https://www.willuhn.de/wiki/doku.php?id=develop:highdpi
LIBOVERLAY_SCROLLBAR=0 GDK_NATIVE_WINDOWS=1 SWT_GTK3=1 exec java $_JLP -Djava.net.preferIPv4Stack=true -Xmx512m -Xss64m $_JCONSOLE -jar jameica-linux${archsuffix}.jar $@
