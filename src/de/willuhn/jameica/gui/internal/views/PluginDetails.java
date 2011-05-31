/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/views/PluginDetails.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/05/31 16:39:04 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.internal.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.controller.PluginControl;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 */
public class PluginDetails extends AbstractView
{

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
		I18N i18n = Application.getI18n();

		PluginControl control = new PluginControl(this);

		GUI.getView().setTitle(i18n.tr("Details des Plugins"));

		Container container = new SimpleContainer(getParent());
		container.addHeadline(i18n.tr("Eigenschaften"));
		container.addLabelPair(i18n.tr("Name"),control.getName());
		container.addLabelPair(i18n.tr("Version"),control.getVersion());
		container.addLabelPair(i18n.tr("Lizenz"),control.getLicense());
		container.addLabelPair(i18n.tr("URL"),control.getUrl());

		container.addHeadline(i18n.tr("Pfade"));
		container.addLabelPair(i18n.tr("Installationsordner"),control.getPath());
		container.addLabelPair(i18n.tr("Ordner der Benutzerdaten"),control.getWorkPath());

		container.addHeadline(i18n.tr("Services"));
		container.addPart(control.getServiceList());
  }
}


/**********************************************************************
 * $Log: PluginDetails.java,v $
 * Revision 1.3  2011/05/31 16:39:04  willuhn
 * @N Funktionen zum Installieren/Deinstallieren von Plugins direkt in der GUI unter Datei->Einstellungen->Plugins
 *
 * Revision 1.2  2011-04-26 11:54:51  willuhn
 * @R Back-Button entfernt
 *
 * Revision 1.1  2005/06/14 23:15:30  web0
 * @N added settings for plugins/services
 *
 * Revision 1.2  2004/12/17 01:10:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/09/13 23:27:07  willuhn
 * *** empty log message ***
 *
 **********************************************************************/