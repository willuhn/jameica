/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/controller/Attic/LicenseControl.java,v $
 * $Revision: 1.5 $
 * $Date: 2004/07/21 23:54:54 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.controller;

import java.io.File;
import java.io.FileInputStream;
import java.rmi.RemoteException;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.InfoReader;
import de.willuhn.util.FileFinder;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

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
   * @throws RemoteException
   */
  public FormTextPart getLibList() throws RemoteException
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
			try {
				InfoReader ir = new InfoReader(new FileInputStream(infos[i]));
				if (ir == null)
				{
					Logger.warn("inforeader is null, skipping lib");
					continue;
				}
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
 * $Log: LicenseControl.java,v $
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