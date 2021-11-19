/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import org.eclipse.swt.SWT;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.PrinterData;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.print.PrintSupport;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import net.sf.paperclips.PaperClips;
import net.sf.paperclips.PrintJob;

/**
 * Aktion zum Drucken von Daten.
 */
public class Print implements Action
{
  /**
   * Erwartet ein Objekt vom Typ {@code PrintSupport}.
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof PrintSupport))
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Bitte wählen Sie die zu druckenden Daten aus"),StatusBarMessage.TYPE_ERROR));
      return;
    }

    final String key = "swt.autoScale";
    final String backup = System.getProperty(key);
    final int zoom = SWTUtil.getDeviceZoom();
    try
    {
      PrintJob job = ((PrintSupport) context).print();
      
      PrintDialog dialog = new PrintDialog(GUI.getShell(), SWT.NONE);
      dialog.setText(Application.getI18n().tr("Drucken"));
      PrinterData printerData = dialog.open();
      if (printerData == null)
      {
        Logger.info("no printer choosen");
        return;
      }
      
      if (zoom > 100)
        System.setProperty(key,"100"); // Scaling kurz auf 100% setzen
      PaperClips.print(job, printerData);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Gedruckt an \"{0}\"",printerData.name),StatusBarMessage.TYPE_SUCCESS));
    }
    finally
    {
      if (zoom > 100 && backup != null)
        System.setProperty(key,backup); // Jetzt wieder auf den vorherigen Wert setzen
    }
  }
}



/**********************************************************************
 * $Log: Print.java,v $
 * Revision 1.2  2011/04/08 13:37:35  willuhn
 * @N Neues PrintSupport-Interface - andernfalls muesste man den Druck-Auftrag vor Ausfuehrung der Action - und damit vor dem Klick auf den Button - erstellen
 *
 * Revision 1.1  2011-04-07 16:49:56  willuhn
 * @N Rudimentaere GUI-Klassen fuer die Druck-Anbindung
 *
 **********************************************************************/