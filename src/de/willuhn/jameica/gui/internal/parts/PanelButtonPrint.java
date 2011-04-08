/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/PanelButtonPrint.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/04/08 13:37:35 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.internal.action.Print;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.print.PrintSupport;
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
  private PanelButtonPrint(final PrintSupport job)
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
 * Revision 1.2  2011/04/08 13:37:35  willuhn
 * @N Neues PrintSupport-Interface - andernfalls muesste man den Druck-Auftrag vor Ausfuehrung der Action - und damit vor dem Klick auf den Button - erstellen
 *
 * Revision 1.1  2011-04-07 16:49:56  willuhn
 * @N Rudimentaere GUI-Klassen fuer die Druck-Anbindung
 *
 **********************************************************************/