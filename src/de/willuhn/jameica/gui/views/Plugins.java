/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/Plugins.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/04/14 23:53:44 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.views;

import java.util.Enumeration;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.willuhn.jameica.AbstractPlugin;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Listet die installierten Plugins auf.
 */
public class Plugins extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getI18n();
		GUI.getView().setTitle(i18n.tr("Installierte Plugins"));
    
		LabelGroup group = new LabelGroup(getParent(),i18n.tr("installierte Plugins"));
		Control c = group.getControl();
		StringBuffer buffer = new StringBuffer();
		buffer.append("<form>");
		
		Enumeration installedPlugins = PluginLoader.getInstalledPlugins();
		AbstractPlugin plugin = null;
		while (installedPlugins.hasMoreElements())
		{
			plugin = (AbstractPlugin) installedPlugins.nextElement();
			buffer.append("<li>" + plugin.getName() + "</li>");
			buffer.append("<li style=\"text\" value=\"-\" bindent=\"10\">" + i18n.tr("Version") 		 + " " + plugin.getVersion() + "</li>");
			buffer.append("<li style=\"text\" value=\"-\" bindent=\"10\">" + i18n.tr("Buildnummer") + " " + plugin.getBuildnumber() + "</li>");
		}

		buffer.append("</form>");

		FormTextPart text = new FormTextPart(buffer.toString());

		text.paint((Composite)c);
		
		ButtonArea buttons = group.createButtonArea(1);
		buttons.addCustomButton(i18n.tr("Zurück"), new MouseAdapter() {
      public void mouseUp(MouseEvent e) {
      	GUI.startPreviousView();
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: Plugins.java,v $
 * Revision 1.1  2004/04/14 23:53:44  willuhn
 * *** empty log message ***
 *
 **********************************************************************/