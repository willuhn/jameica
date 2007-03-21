/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/WaitDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/03/21 13:48:52 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

/**
 * Warte-Dialog, der einen Text und einen Fortschrittsbalken fuer ein Timeout anzeigt.
 * Zwei Anwendungsbeispiele:
 * <ul>
 *   <li>Der User soll einen USB-Stick mit einer Schluesseldatei einstecken. Waehrenddessen
 *       wird dieser Dialog anagezeigt.</li>
 *   <li>Der User soll eine PIN im Kartenleser eingeben.</li>
 * </ul>
 * Die Klasse ist abstrakt, damit in der abgeleiteten Klasse die Funktion <code>check()</code>
 * implementiert wird. Der Rueckgabe-Wert der Funktion entscheidet darueber, ob der Dialog
 * geschlossen werden soll oder weiter gewartet wird. Diese Funktion wird nach Ablauf jeder Sekunde
 * erneut aufgerufen. Solange, bis sie <code>true</code> zurueckliefert, oder das Timeout
 * abgelaufen ist.
 */
public abstract class WaitDialog extends AbstractDialog
{
  private long intervall = 1000l;
  private long timeout   = 60 * intervall;
  private long current   = 0;
  private Thread t       = null;

  /**
   * Erzeugt einen Wartedialog mit einem Standard-Timeout von 60 Sekunden.
   * @param pos die Position des Dialogs.
   */
  public WaitDialog(int pos)
  {
    this(-1,pos);
  }
  
  /**
   * ct.
   * @param timeout Timeout in Millisekunden, nach deren Ablauf der Dialog mit einer
   * OperationCancelledException abbrechen soll.
   * @param pos die Position des Dialogs.
   */
  public WaitDialog(long timeout, int pos)
  {
    super(pos);
    if (timeout > intervall) // Nur uebernehmen, wenn eine gewisse Mindestgroesse uebergeben
      this.timeout = timeout;

    super.addCloseListener(new Listener() {
    
      public void handleEvent(Event event)
      {
        if (t != null)
        {
          try
          {
            t.interrupt();
          }
          catch (OperationCanceledException oce)
          {
            throw oce;
          }
          catch (Exception e)
          {
            // ignore
          }
        }
      }
    
    });
  }

  /**
   * Die Funktion liefert immer <code>null</code>.
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }
  
  /**
   * Liefert den anzuzeigenden Text.
   * Sollte von der abgeleiteten Klasse ueberschrieben werden.
   * @return text der anzuzeigende Text.
   */
  public String getText()
  {
    return null;
  }
  
  /**
   * Wird nach Ablauf jeder Sekunde aufgerufen.
   * Hier kann die abgeleitete Klasse entscheiden, ob die gewuenschte
   * Aktion vom User durchgefuehrt wurde und der Dialog geschlossen werden kann
   * oder ob noch laenger gewartet wird.
   * @return true, wenn die Aktion durchgefuehrt wurde und der Dialog geschlossen werden kann.
   */
  protected abstract boolean check();

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    String text = getText();
    if (text != null && text.length() > 0)
    {
      Container container = new SimpleContainer(parent);
      container.addText(text,true);
    }

    final ProgressBar bar = new org.eclipse.swt.widgets.ProgressBar(parent, SWT.SMOOTH);
    GridData g = new GridData(GridData.FILL_HORIZONTAL);
    g.horizontalSpan = 2;
    bar.setLayoutData(g);
    bar.setMaximum((int)(timeout / 1000));
    bar.setSelection(0);


    ButtonArea buttons = new ButtonArea(parent,2);
    buttons.addButton("  " + i18n.tr("OK") + "  ",new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    
    },null,true);
    buttons.addButton(i18n.tr("Abbrechen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException("operation cancelled");
      }
    });

    t = new Thread() {
    
      public void run()
      {
        try
        {
          while (current < timeout)
          {
            if (check())
            {
              close();
              return;
            }
           
            current += intervall;

            GUI.getDisplay().syncExec(new Runnable()
            {
              public void run()
              {
                if (bar != null && !bar.isDisposed())
                  bar.setSelection((int)(current / 1000));
              }
            });
            sleep(intervall);
          }
          throw new OperationCanceledException("operation cancelled/timed out");
        }
        catch (InterruptedException e)
        {
          // ignore
        }
      }
    };
    t.start();
  }

}


/*********************************************************************
 * $Log: WaitDialog.java,v $
 * Revision 1.1  2007/03/21 13:48:52  willuhn
 * @N new abstract "WaitDialog"
 * @N force redraw in backgroundtask monitor/statusbar
 *
 **********************************************************************/