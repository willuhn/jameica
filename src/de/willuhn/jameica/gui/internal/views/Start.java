/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/views/Start.java,v $
 * $Revision: 1.22 $
 * $Date: 2011/05/03 12:57:00 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.views;

import java.util.List;

import org.eclipse.swt.layout.GridLayout;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.boxes.Box;
import de.willuhn.jameica.gui.boxes.BoxRegistry;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.internal.dialogs.ChooseBoxesDialog;
import de.willuhn.jameica.gui.parts.ExpandPart;
import de.willuhn.jameica.gui.parts.PanelButton;
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

    if (!Customizing.SETTINGS.getBoolean("application.start.hidecustomize",false))
    {
      // Panel-Button zum Anpassen der Startseite
      PanelButton button = new PanelButton("document-properties.png",new Action() {
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
      },Application.getI18n().tr("Startseite anpassen"));
      GUI.getView().addPanelButton(button);
    }
    
    
    GridLayout layout = (GridLayout) getParent().getLayout();
    layout.horizontalSpacing = 0;
    layout.marginHeight = 0;
    layout.marginWidth = 0;

    ExpandPart expand = new ExpandPart();
    List<Box> boxes = BoxRegistry.getBoxes();
    
    for (Box b:boxes)
    {
      try
      {
        expand.add(b);
      }
      catch (Exception e)
      {
        Logger.error("unable to add box " + b.getName() + ", skipping",e);
      }
    }
    expand.paint(getParent());
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
 * Revision 1.22  2011/05/03 12:57:00  willuhn
 * @B Das komplette Ausblenden nicht-aktiver Boxen fuehrte zu ziemlichem Durcheinander in dem Dialog
 * @C Aendern der Sortier-Reihenfolge vereinfacht. Sie wird jetzt nicht mehr live sondern erst nach Klick auf "Uebernehmen" gespeichert - was fachlich ja auch richtiger ist
 *
 * Revision 1.21  2011-05-03 12:12:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.20  2011-05-03 11:33:57  willuhn
 * @N Button "Startseite anpassen" als Panel-Button
 * @B das Entfernen und Wiederhinzufuegen von Elementen im ChooseBoxDialog fuehrte unter OS X zu einer ArrayIndexOutOfBoundsException - warum auch immer
 *
 * Revision 1.19  2011-05-03 11:07:37  willuhn
 * @N Styling-Fixes fuer Windows (Background)
 *
 * Revision 1.18  2011-01-14 12:02:15  willuhn
 * @B Versehentlich Test-Code eingecheckt
 ***************************************************************************/