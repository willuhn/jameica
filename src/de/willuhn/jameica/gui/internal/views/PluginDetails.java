/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/views/PluginDetails.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/06/14 23:15:30 $
 * $Author: web0 $
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
import de.willuhn.jameica.gui.internal.action.Back;
import de.willuhn.jameica.gui.internal.controller.PluginControl;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
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

		LabelGroup group = new LabelGroup(getParent(),i18n.tr("Eigenschaften"));
		group.addLabelPair(i18n.tr("Name"),control.getName());
		group.addLabelPair(i18n.tr("Version"),control.getVersion());
		group.addLabelPair(i18n.tr("Lizenz"),control.getLicense());
		group.addLabelPair(i18n.tr("URL"),control.getUrl());

		LabelGroup path = new LabelGroup(getParent(),i18n.tr("Pfade"));
		path.addLabelPair(i18n.tr("Installations-Verzeichnis"),control.getPath());
		path.addLabelPair(i18n.tr("Arbeitsverzeichnis"),control.getWorkPath());

		LabelGroup services = new LabelGroup(getParent(),i18n.tr("Services"));
		services.addPart(control.getServiceList());

		ButtonArea buttons = new ButtonArea(getParent(),1);
		buttons.addButton(i18n.tr("Zurück"), new Back());
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
  }

}


/**********************************************************************
 * $Log: PluginDetails.java,v $
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