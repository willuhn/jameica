/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
  
  /**
   * @see de.willuhn.jameica.gui.AbstractView#canBookmark()
   */
  public boolean canBookmark()
  {
    return false;
  }
}

