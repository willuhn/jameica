/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/Plugins.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/07/21 23:54:54 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.views;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.PluginsControl;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.system.Application;
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
    
		PluginsControl control = new PluginsControl(this);

		FormTextPart libs = control.getLibList();
		libs.paint(getParent());

		ButtonArea buttons = new ButtonArea(getParent(),1);
		buttons.addCancelButton(control);
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: Plugins.java,v $
 * Revision 1.3  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.2  2004/04/26 22:42:17  willuhn
 * @N added InfoReader
 *
 * Revision 1.1  2004/04/14 23:53:44  willuhn
 * *** empty log message ***
 *
 **********************************************************************/