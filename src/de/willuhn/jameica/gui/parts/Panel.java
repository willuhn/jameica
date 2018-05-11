/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Das ist ein Container, der weitere Parts aufnehmen kann, jedoch
 * die Anzeige um einen Titel und Rahmen erweitert.
 * @author willuhn
 */
public class Panel implements Part
{
  private Part child               = null;
  private TitlePart title          = null;
  private List<Listener> listeners = new LinkedList<Listener>();
  private PanelButton minimize     = null;
  
  /**
   * ct.
   * @param title anzuzeigender Titel.
   * @param child Kind-Part welches angezeigt werden soll.
   */
  public Panel(String title, Part child)
  {
    this(title,child,true);
  }

  /**
   * ct.
   * @param title anzuzeigender Titel.
   * @param child Kind-Part welches angezeigt werden soll.
   * @param border legt fest, ob ein Rahmen um das Panel gezeichnet werden soll.
   */
  public Panel(String title, Part child, boolean border)
  {
    this.title = new TitlePart(title,border);
    this.child = child;
  }

  /**
   * Fuegt dem Panel einen Listener zum Minimieren hinzu.
   * Wird ein solcher angegeben, wird automatisch ein Knopf zum
   * Minimieren angezeigt, der sonst ausgeblendet ist.
   * @param l der auszuloesende Listener.
   */
  public void addMinimizeListener(final Listener l)
  {
    // Den Button duerfen wir ja nur einmal hinzufuegen
    if (this.minimize == null)
    {
      this.minimize = new PanelButton("minimize.png",new Action() {
        public void handleAction(Object context) throws ApplicationException
        {
          Event e = new Event();
          e.data = context;
          
          // Einmal fuer jeden Listener ausloesen
          for (Listener l:listeners)
            l.handleEvent(e);
        }
      },Application.getI18n().tr("Minimieren"));
      this.addButton(this.minimize);
    }
    this.listeners.add(l);
  }

  /**
   * Fuegt einen Panel-Button hinzu.
   * @param b der Panel-Button.
   */
  public void addButton(PanelButton b)
  {
    this.title.addButton(b);
  }

  /**
   * Setzt den anzuzeigenden Titel.
   * Dies kann auch nachtraeglich noch ausgefuehrt werden, wenn das
   * Panel schon angezeigt wird.
   * @param title
   */
  public void setTitle(String title)
  {
    this.title.setTitle(title);
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    this.title.paint(parent);
    this.child.paint(this.title.getComposite());
  }
}


/*********************************************************************
 * $Log: Panel.java,v $
 * Revision 1.18  2011/08/18 16:38:08  willuhn
 * @B Minimize-Button nur einmal hinzufuegen
 * @N Speichern-Button im Syslog via neuem Panel-Button
 *
 * Revision 1.17  2011-08-18 16:28:22  willuhn
 * @N Neue Funktion zum Hinzufuegen eines frei definierbaren Buttons
 *
 * Revision 1.16  2011-08-18 16:03:38  willuhn
 * @N BUGZILLA 286 - Panel-Code komplett refactored und in eine gemeinsame neue Klasse "TitlePart" verschoben. Damit muss der Code (incl. Skalieren der Panel) nur noch an einer Stelle gewartet werden. Und wir haben automatisch Panelbutton-Support an allen Stellen - nicht nur in der View, sondern jetzt auch im Snapin, in der Navi und sogar in Dialogen ;)
 *
 * Revision 1.15  2011-08-18 09:17:10  willuhn
 * @N BUGZILLA 286 - Testcode
 *
 * Revision 1.14  2011-05-03 10:13:10  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.13  2011-04-06 16:13:16  willuhn
 * @N BUGZILLA 631
 **********************************************************************/