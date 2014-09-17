#!/bin/sh

# Linux Start-Script fuer regulaeren Standalone-Betrieb.
# Jameica wird hierbei mit GUI gestartet.

#_JCONSOLE="-Dcom.sun.management.jmxremote"

# https://www.willuhn.de/bugzilla/show_bug.cgi?id=774
# https://www.willuhn.de/bugzilla/show_bug.cgi?id=798

link=$(readlink -f "$0")
dir=$(dirname "$link")
cd "$dir" 

bit=`uname -m |grep 64`
if [ $? = 0 ]
 then LIBOVERLAY_SCROLLBAR=0 java -Djava.net.preferIPv4Stack=true -Xmx256m $_JCONSOLE -jar jameica-linux64.jar $@
 else LIBOVERLAY_SCROLLBAR=0 java -Djava.net.preferIPv4Stack=true -Xmx256m $_JCONSOLE -jar jameica-linux.jar $@
fi
