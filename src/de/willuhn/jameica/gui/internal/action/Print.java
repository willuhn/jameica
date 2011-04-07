/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/Print.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/04/07 16:49:56 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import net.sf.paperclips.PaperClips;
import net.sf.paperclips.PrintJob;

import org.eclipse.swt.SWT;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.PrinterData;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Aktion zum Drucken von Daten.
 */
public class Print implements Action
{
  /**
   * Erwartet ein Objekt vom Typ <code>PrintJob</code>.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof PrintJob))
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Bitte wählen Sie die zu druckenden Daten aus"),StatusBarMessage.TYPE_ERROR));
      return;
    }
    
    PrintDialog dialog = new PrintDialog(GUI.getShell(), SWT.NONE);
    PrinterData printerData = dialog.open();
    if (printerData == null)
    {
      Logger.info("no printer choosen");
      return;
    }
    
    PaperClips.print((PrintJob) context, printerData);
    Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Gedruckt an {0}",printerData.name),StatusBarMessage.TYPE_SUCCESS));
  }
}



/**********************************************************************
 * $Log: Print.java,v $
 * Revision 1.1  2011/04/07 16:49:56  willuhn
 * @N Rudimentaere GUI-Klassen fuer die Druck-Anbindung
 *
 **********************************************************************/