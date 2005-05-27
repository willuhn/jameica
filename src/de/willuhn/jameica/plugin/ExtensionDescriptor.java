/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/ExtensionDescriptor.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/27 17:31:46 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.plugin;

/**
 * Ein Extension-Deskriptor beschreibt eine Extension, welche
 * bei einem Plugin beliegen kann, um die Funktionalitaet eines
 * anderen Plugins zu erweitern.
 * Diese finden sich in der plugin.xml eines Plugins.
 * Bsp.:
 * <extensions>
 *   <extension class="de.willuhn.jameica.plugin.Extension1" extends="id.extendable.1,id.extendable.2" />
 *   <extension class="de.willuhn.jameica.plugin.Extension2" extends="id.extendable.3" />
 * </extensions>
 */
public interface ExtensionDescriptor
{
	/**
	 * Liefert den Namen der Java-Klasse der Extension.
   * @return Name der Klasse.
   * Diese muss das Interface Extension implementieren.
   */
  public String getClassname();

	/**
	 * Liefert eine Liste von Extendable-IDs, welche diese Extension
   * erweitert.
   * @return Liste von Extendables.
   * Die Funktion darf nie <code>null</code> liefern, da sie dann kein
   * Extendable erweitern wuerde. Die Extension waere damit nutzlos.
   */
  public String[] getExtendableIDs();
}


/**********************************************************************
 * $Log: ExtensionDescriptor.java,v $
 * Revision 1.1  2005/05/27 17:31:46  web0
 * @N extension system
 *
 **********************************************************************/