#!/bin/sh

# Linux Start-Script fuer regulaeren Standalone-Betrieb, jedoch 64-Bit Version fuer Athlon64.
# Jameica wird hierbei mit GUI gestartet.

cd `dirname $(readlink -f $0)`

# https://www.willuhn.de/bugzilla/show_bug.cgi?id=774
# https://www.willuhn.de/bugzilla/show_bug.cgi?id=798

GDK_NATIVE_WINDOWS=1 java -Djava.net.preferIPv4Stack=true -Xmx256m -jar jameica-linux64.jar $@
