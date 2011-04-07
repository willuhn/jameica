/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/PanelButtonPrint.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/04/07 16:49:56 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import net.sf.paperclips.PrintJob;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.internal.action.Print;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Vorkonfigurierter Button fuer Druck-Support.
 */
public class PanelButtonPrint extends PanelButton
{
  /**
   * ct.
   * @param job der Druck-Job.
   */
  public PanelButtonPrint(final PrintJob job)
  {
    super("document-print.png", new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new Print().handleAction(job);
      }
    }, Application.getI18n().tr("Drucken"));
  }
}



/**********************************************************************
 * $Log: PanelButtonPrint.java,v $
 * Revision 1.1  2011/04/07 16:49:56  willuhn
 * @N Rudimentaere GUI-Klassen fuer die Druck-Anbindung
 *
 **********************************************************************/