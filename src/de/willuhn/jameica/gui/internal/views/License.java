/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/views/License.java,v $
 * $Revision: 1.6 $
 * $Date: 2011/04/26 11:38:28 $
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
import de.willuhn.jameica.gui.internal.controller.LicenseControl;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * View fuer die Lizenz-Informationen
 */
public class License extends AbstractView
{

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {

		I18N i18n = Application.getI18n();
		GUI.getView().setTitle(i18n.tr("Lizenzinformationen"));
    
		LicenseControl control = new LicenseControl(this);

		FormTextPart libs = control.getLibList();
		libs.paint(getParent());
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: License.java,v $
 * Revision 1.6  2011/04/26 11:38:28  willuhn
 * @R Back-Button entfernt
 *
 * Revision 1.5  2009-01-20 10:51:51  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 * Revision 1.4  2005/03/01 22:58:24  web0
 * @B typo
 *
 * Revision 1.3  2004/11/05 20:00:43  willuhn
 * @D javadoc fixes
 *
 * Revision 1.2  2004/10/20 12:33:53  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.1  2004/10/08 13:38:20  willuhn
 * *** empty log message ***
 *
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