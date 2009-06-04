/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/views/Start.java,v $
 * $Revision: 1.16 $
 * $Date: 2009/06/04 10:34:59 $
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
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.boxes.Box;
import de.willuhn.jameica.gui.boxes.BoxRegistry;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.internal.dialogs.ChooseBoxesDialog;
import de.willuhn.jameica.gui.parts.ExpandPart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Customizing;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;


/**
 * Startseite von Jameica.
 */
public class Start extends AbstractView implements Extendable
{

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    GUI.getView().setTitle(Application.getI18n().tr("Willkommen"));

    Box[] boxes = BoxRegistry.getBoxes();

    ExpandPart expand = new ExpandPart();
    for (int i=0;i<boxes.length;++i)
    {
      try
      {
        expand.add(boxes[i]);
      }
      catch (Exception e)
      {
        Logger.error("unable to add box " + boxes[i].getClass().getName() + ", skipping",e);
      }
    }
    expand.paint(getParent());
    
    if (!Customizing.SETTINGS.getBoolean("application.start.hidecustomize",false))
    {
      ButtonArea buttons = new ButtonArea(getParent(),1);
      buttons.addButton(Application.getI18n().tr("Startseite anpassen"),new Action() {
        public void handleAction(Object context) throws ApplicationException
        {
          ChooseBoxesDialog d = new ChooseBoxesDialog(ChooseBoxesDialog.POSITION_CENTER);
          try
          {
            d.open();
          }
          catch (Exception e)
          {
            Logger.error("error while loading box config dialog",e);
          }
        }
      },null,true,"document-properties.png");
    }
  }        

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind()
  {
  }


  /**
   * @see de.willuhn.jameica.gui.extension.Extendable#getExtendableID()
   */
  public String getExtendableID()
  {
    return this.getClass().getName();
  }

}

/***************************************************************************
 * $Log: Start.java,v $
 * Revision 1.16  2009/06/04 10:34:59  willuhn
 * @N Customizing-Parameter zum Ausblenden des "Startseite anpassen..."-Button
 *
 * Revision 1.15  2009/01/20 10:51:51  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 * Revision 1.14  2008/12/10 23:54:41  willuhn
 * @B fehlerhafte Boxen ueberspringen. Sonst wird die komplette Startseite nicht angezeigt
 *
 * Revision 1.13  2007/12/18 17:10:14  willuhn
 * @N Neues ExpandPart
 * @N Boxen auf der Startseite koennen jetzt zusammengeklappt werden
 *
 * Revision 1.12  2007/03/29 15:29:48  willuhn
 * @N Uebersichtlichere Darstellung der Systemstart-Meldungen
 *
 * Revision 1.11  2006/06/30 13:51:34  willuhn
 * @N Pluginloader Redesign in HEAD uebernommen
 *
 * Revision 1.10  2006/06/29 23:10:01  willuhn
 * @N Box-System aus Hibiscus in Jameica-Source verschoben
 *
 * Revision 1.9  2005/12/29 00:25:59  web0
 * @N Anzeige der System-Meldungen auf der Start-Seite
 *
 * Revision 1.8  2005/10/17 14:01:15  web0
 * *** empty log message ***
 *
 * Revision 1.7  2005/08/25 21:18:24  web0
 * @C changes accoring to findbugs eclipse plugin
 *
 * Revision 1.6  2005/03/19 18:17:37  web0
 * @B bloeder CipherInputStream
 *
 * Revision 1.5  2004/12/13 22:48:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/11/05 20:00:43  willuhn
 * @D javadoc fixes
 *
 * Revision 1.3  2004/10/29 16:16:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/11 15:39:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/08 13:38:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.29  2004/08/18 23:14:19  willuhn
 * @D Javadoc
 *
 * Revision 1.28  2004/07/25 17:15:20  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.27  2004/07/23 15:51:20  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.26  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.25  2004/07/04 17:07:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.24  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2004/05/27 23:38:25  willuhn
 * @B deadlock in swt event queue while startGUITimeout
 *
 * Revision 1.22  2004/05/27 23:12:58  willuhn
 * @B NoSuchFieldError in Settings
 * @C s/java/javaw.exe in build/*.bat
 *
 * Revision 1.21  2004/05/27 21:35:02  willuhn
 * @N PGP signing in ant script
 * @N MD5 checksum in ant script
 *
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