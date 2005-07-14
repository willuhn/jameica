/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/ServiceDescriptor.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/07/14 22:58:36 $
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
 * Ein Service-Deskriptor beschreibt Name und Klasse
 * eines Services, welcher von einem Plugin zur Verfuegung
 * gestellt werden kann.
 * Diese finden sich in der plugin.xml eines jeden Plugins.
 * Bsp.:
 * <services>
 *   <service name="service 1" class="de.willuhn.jameica.plugin.Service1"
 *            autostart="true" depends="service 2" />
 *   <service name="service 2" class="de.willuhn.jameica.plugin.Service2"
 *            autostart="false" />
 * </services>
 */
public interface ServiceDescriptor
{
	/**
	 * Liefert den Namen des Services.
   * @return Name des Services.
   */
  public String getName();

	/**
	 * Liefert den Namen der Java-Klasse des Services.
   * @return Name der Klasse.
   * Hier muss keine Implementierung angegeben werden. Ist ein
   * Interface eingetragen, sucht Jameica selbstaendig die
   * zugehoerige Implementierung.
   */
  public String getClassname();

	/**
	 * Legt fest, ob der Service beim Starten von Jameica automatisch
	 * gestartet werden soll.
   * @return true, wenn er automatisch gestartet werden soll.
   */
  public boolean autostart();

	/**
	 * Liefert eine Liste von Service-Namen, von denen dieser Service
	 * abhaengig ist.
   * @return Liste von Services, die vorher instanziiert werden muessen.
   * Kann <code>null</code> liefern, wenn der Service von keinen
   * anderen Services abhaengig ist.
   */
  public String[] depends();
  
  /**
   * Legt fest, ob der Service im Netzwerk freigegeben werden soll.
   * @return true, wenn er freigegeben werden soll.
   */
  public boolean share();
  
}


/**********************************************************************
 * $Log: ServiceDescriptor.java,v $
 * Revision 1.2  2005/07/14 22:58:36  web0
 * *** empty log message ***
 *
 * Revision 1.1  2004/12/21 01:08:01  willuhn
 * @N new service configuration system in plugin.xml with auostart and dependencies
 *
 **********************************************************************/