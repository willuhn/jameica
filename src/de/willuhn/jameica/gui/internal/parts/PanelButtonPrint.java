/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/PanelButtonPrint.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/04/14 17:18:10 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
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
  private PrintListener listener = new PrintListener();
  
  /**
   * ct.
   * @param job der Druck-Job.
   */
  public PanelButtonPrint(final PrintSupport job)
  {
    super("document-print.png", new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new Print().handleAction(job);
      }
    }, Application.getI18n().tr("Drucken"));
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.PanelButton#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);

    // Shortcut aktivieren
    // Damit das immer funktioniert, musste ich in GUI#startView
    // noch ein "parent.setFocus()" vor dem bind() der View machen,
    // damit irgendwas den Focus hat. Wenn gar kein Control einen Focus
    // hat, werden naemlich auch keinerlei Key-Listener ausgeloest
    GUI.getDisplay().addFilter(SWT.KeyDown,listener);
    
    // Wieder deaktivieren
    getControl().addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        GUI.getDisplay().removeFilter(SWT.KeyDown,listener);
      }
    });
  }

  /**
   * Listener fuer den Shortchut CTRL+P.
   */
  private class PrintListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
      if (!isEnabled())
        return;
      
      if((event.stateMask & SWT.CTRL) != 0 && event.keyCode == 'p')
      {
        GUI.getDisplay().syncExec(new Runnable() {
          public void run()
          {
            handleClick();
          }
        });
        event.doit = false; // wir haben die Sache ja jetzt behandelt
      }
    }
  }
}



/**********************************************************************
 * $Log: PanelButtonPrint.java,v $
 * Revision 1.5  2011/04/14 17:18:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2011-04-14 16:58:48  willuhn
 * @N Globaler Shortcut <CTRL><P> zum Drucken (falls PanelButtonPrint aktiv ist)
 *
 * Revision 1.3  2011-04-08 13:38:13  willuhn
 * @B Falscher Modifier
 *
 * Revision 1.2  2011-04-08 13:37:35  willuhn
 * @N Neues PrintSupport-Interface - andernfalls muesste man den Druck-Auftrag vor Ausfuehrung der Action - und damit vor dem Klick auf den Button - erstellen
 *
 * Revision 1.1  2011-04-07 16:49:56  willuhn
 * @N Rudimentaere GUI-Klassen fuer die Druck-Anbindung
 *
 **********************************************************************/