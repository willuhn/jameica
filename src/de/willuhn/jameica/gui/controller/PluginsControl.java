/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/controller/Attic/PluginsControl.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/06/30 20:58:39 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.controller;

import java.rmi.RemoteException;
import java.util.Enumeration;

import de.willuhn.jameica.AbstractPlugin;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.InfoReader;
import de.willuhn.jameica.PluginContainer;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Controller fuer den Dialog "installierte Plugins".
 */
public class PluginsControl extends AbstractControl {

	private FormTextPart libList = null;

  /**
   * ct.
   * @param view
   */
  public PluginsControl(AbstractView view) {
    super(view);
  }

	/**
	 * Liefert eine Liste mit allen installierten Plugins.
   * @return Liste der Plugins.
   * @throws RemoteException
   */
  public FormTextPart getLibList() throws RemoteException
	{
		if (libList != null)
			return libList;
					
		I18N i18n = Application.getI18n();
		

		StringBuffer buffer = new StringBuffer();
		buffer.append("<form>");
		buffer.append("<p><span color=\"header\" font=\"header\">" + i18n.tr("Installierte Plugins") + "</span></p>");

		Enumeration e = PluginLoader.getPluginContainers();
		while (e.hasMoreElements())
		{
			PluginContainer container = (PluginContainer) e.nextElement();
			AbstractPlugin plugin = container.getPlugin();
			InfoReader ir = null;
			try {
				ir = container.getInfo();
			}
			catch (Exception e1)
			{
				Logger.error("unable to read info.xml from plugin " + plugin.getName(),e1);
			}

			if (ir == null)
			{
				Logger.warn("info.xml for plugin " + plugin.getName() + " not found, skipping");
				continue;
			}

			buffer.append("<p>");
			buffer.append("<b>" + (ir == null ? plugin.getName() : ir.getName()) + (container.isInstalled() ? "" : (" (" +i18n.tr("nicht aktiv") + ")")) + "</b>");
			buffer.append("<br/>" + i18n.tr("installiert in") + ": " + container.getFile().getAbsolutePath());
			buffer.append("<br/>" + i18n.tr("Version") + ": " + plugin.getVersion() + "-" + plugin.getBuildnumber());
			if (ir != null)
			{
				buffer.append("<br/>" + i18n.tr("Beschreibung") + ": " + ir.getDescription());
				buffer.append("<br/>" + i18n.tr("URL") + ": " + ir.getUrl());
				buffer.append("<br/>" + i18n.tr("Lizenz") + ": " + ir.getLicense());
			}
			buffer.append("</p>");
		}
		buffer.append("</form>");

		libList = new FormTextPart(buffer.toString());
		return libList;
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleDelete()
   */
  public void handleDelete() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
   */
  public void handleCancel() {
  	GUI.startPreviousView();
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public void handleStore() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
   */
  public void handleOpen(Object o) {
  }

}


/**********************************************************************
 * $Log: PluginsControl.java,v $
 * Revision 1.3  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/05/27 23:38:25  willuhn
 * @B deadlock in swt event queue while startGUITimeout
 *
 * Revision 1.1  2004/04/26 22:42:18  willuhn
 * @N added InfoReader
 *
 **********************************************************************/