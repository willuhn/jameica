/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/controller/LicenseControl.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/04/26 12:15:49 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.internal.controller;

import java.io.File;
import java.io.FileInputStream;

import de.willuhn.io.FileFinder;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.InfoReader;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Controller fuer den Dialog Lizenzinformationen.
 */
public class LicenseControl extends AbstractControl {

	private FormTextPart libList = null;

  /**
   * ct.
   * @param view
   */
  public LicenseControl(AbstractView view) {
    super(view);
  }

	/**
	 * Liefert eine Liste mit allen direkt von Jameica verwendeten Komponenten.
   * @return Liste der Komponenten.
   */
  public FormTextPart getLibList()
	{
		if (libList != null)
			return libList;
					
		I18N i18n = Application.getI18n();
		

		StringBuffer buffer = new StringBuffer();
		buffer.append("<form>");
		buffer.append("<p><span color=\"header\" font=\"header\">" + i18n.tr("Verwendete Komponenten") + "</span></p>");

		FileFinder finder = new FileFinder(new File("lib"));
		finder.matches(".*?info\\.xml$");
		File[] infos = finder.findRecursive();
		for (int i=0;i<infos.length;++i)
		{
      if (!infos[i].isFile() || !infos[i].canRead())
      {
        Logger.warn("unable to read " + infos[i] + ", skipping");
        continue;
      }

      try {
				InfoReader ir = new InfoReader(new FileInputStream(infos[i]));
				buffer.append("<p>");
				buffer.append("<b>" + ir.getName() + "</b>");
				buffer.append("<br/>" + i18n.tr("Beschreibung") + ": " + ir.getDescription());
				buffer.append("<br/>" + i18n.tr("Verzeichnis") + ": " + infos[i].getParentFile().getAbsolutePath());
				buffer.append("<br/>" + i18n.tr("URL") + ": " + ir.getUrl());
				buffer.append("<br/>" + i18n.tr("Lizenz") + ": " + ir.getLicense());
				buffer.append("</p>");
			}
			catch (Exception e)
			{
				Logger.error("unable to parse " + infos[0],e);
			}
		}
		buffer.append("</form>");

		libList = new FormTextPart(buffer.toString());
		return libList;
	}

}


/**********************************************************************
 * $Log: LicenseControl.java,v $
 * Revision 1.5  2011/04/26 12:15:49  willuhn
 * @B Potentielle Bugs gemaess Code-Checker
 *
 * Revision 1.4  2005/03/09 01:06:36  web0
 * @D javadoc fixes
 *
 * Revision 1.3  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/20 12:08:16  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.1  2004/10/08 13:38:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/10/07 18:05:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.4  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/06/08 22:28:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/04/27 00:04:44  willuhn
 * @D javadoc
 *
 * Revision 1.1  2004/04/26 22:42:18  willuhn
 * @N added InfoReader
 *
 **********************************************************************/