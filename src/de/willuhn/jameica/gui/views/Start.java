/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/Start.java,v $
 * $Revision: 1.20 $
 * $Date: 2004/04/26 22:42:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.views;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.I18N;


/**
 * Startseite von Jameica.
 */
public class Start extends AbstractView
{

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception
  {
  	I18N i18n = Application.getI18n();
		GUI.getView().setTitle(i18n.tr("Start"));

		// TODO: Hier kontextsensitive Notizen anzeigen    

  }        

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind()
  {
  }

}

/***************************************************************************
 * $Log: Start.java,v $
 * Revision 1.20  2004/04/26 22:42:17  willuhn
 * @N added InfoReader
 *
 * Revision 1.19  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.18  2004/03/30 22:08:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/03/24 00:46:02  willuhn
 * @C refactoring
 *
 * Revision 1.16  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.15  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.14  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 * Revision 1.13  2004/02/20 20:45:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/02/20 01:25:06  willuhn
 * @N nice dialog
 * @N busy indicator
 * @N new status bar
 *
 * Revision 1.11  2004/01/29 00:07:23  willuhn
 * @N Text widget
 *
 * Revision 1.10  2004/01/28 20:51:25  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.9  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.7  2003/12/29 17:44:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.5  2003/12/05 17:12:23  willuhn
 * @C SelectInput
 *
 * Revision 1.4  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 * Revision 1.3  2003/10/29 00:41:27  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/10/23 22:36:34  willuhn
 * @N added Menu
 *
 * Revision 1.1  2003/10/23 21:50:06  willuhn
 * initial checkin
 *
 ***************************************************************************/