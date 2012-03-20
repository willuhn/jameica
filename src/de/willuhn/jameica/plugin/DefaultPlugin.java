/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/DefaultPlugin.java,v $
 * $Revision: 1.1 $
 * $Date: 2012/03/20 23:48:32 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.plugin;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;

/**
 * Default-Implementierung der Plugin-Klasse fuer Plugins, die
 * keinen eigenen Java-Code mitbringen sondern z.Bsp. nur Jars oder
 * Javascripts. Im Manifest dieser Plugins kann das Attribut "class"
 * des XML-Elements "plugin" dann einfach weggelassen werden.
 * 
 * Der Request-Lifecycle stellt sicher, dass jedesmal eine neue Instanz erzeugt wird.
 */
@Lifecycle(Type.REQUEST)
public class DefaultPlugin extends AbstractPlugin
{

}



/**********************************************************************
 * $Log: DefaultPlugin.java,v $
 * Revision 1.1  2012/03/20 23:48:32  willuhn
 * @N BUGZILLA 1208: Erster Code fuer "Dummy-Plugins", die keinen eignen Java-Code mitbringen sondern z.Bsp. nur Jars oder Javascript-Dateien. Noch offen: "PluginLoader#getManifest(...)" wird nicht unterscheiden koennen, wenn mehrere solcher Dummy-Plugins installiert sind, da alle das gleiche "DefaultPlugin" verwenden. Muss ich mal noch evaluieren
 *
 **********************************************************************/