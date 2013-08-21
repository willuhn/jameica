#!/bin/sh

# Linux Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

#_JCONSOLE="-Dcom.sun.management.jmxremote"

# https://www.willuhn.de/bugzilla/show_bug.cgi?id=774
# https://www.willuhn.de/bugzilla/show_bug.cgi?id=798

# Ergänzung für PCSC
_PCSC64="-Dsun.security.smartcardio.library=/usr/lib64/libpcsclite.so.1"
_PCSC32="-Dsun.security.smartcardio.library=/usr/lib/libpcsclite.so.1"

link=$(readlink -f "$0")
dir=$(dirname "$link")
cd "$dir" 

bit=`uname -m |grep 64`
if [ $? = 0 ]
 then LIBOVERLAY_SCROLLBAR=0 GDK_NATIVE_WINDOWS=1 java -Xmx256m $_JCONSOLE $_PCSC64 -jar jameica-linux64.jar $@
 else LIBOVERLAY_SCROLLBAR=0 GDK_NATIVE_WINDOWS=1 java -Xmx256m $_JCONSOLE $_PCSC32 -jar jameica-linux.jar $@
fi
