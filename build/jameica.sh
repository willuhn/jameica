#!/bin/sh

# Linux Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

#_JCONSOLE="-Dcom.sun.management.jmxremote"

# https://www.willuhn.de/bugzilla/show_bug.cgi?id=774
# https://www.willuhn.de/bugzilla/show_bug.cgi?id=798

link=$(readlink -f "$0")
dir=$(dirname "$link")
cd "$dir" 

if uname -m |grep -q 64; then
	archsuffix="64"
else
	archsuffix=""
fi

LIBOVERLAY_SCROLLBAR=0 GDK_NATIVE_WINDOWS=1 SWT_GTK3=0 exec java --add-modules=java.se.ee -Djava.net.preferIPv4Stack=true -Xmx512m $_JCONSOLE -jar jameica-linux${archsuffix}.jar $@
