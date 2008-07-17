/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/reminder/Attic/ToStringRenderer.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/07/17 23:21:27 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.reminder;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.GUI;


/**
 * Ein generischer Dummy-Renderer, der auf die Nutzdaten lediglich ein
 * toString() anwendet, um die Daten zu rendern.
 */
public class ToStringRenderer implements Renderer
{

  /**
   * @see de.willuhn.jameica.reminder.Renderer#render(org.eclipse.swt.widgets.Composite, de.willuhn.jameica.reminder.Reminder)
   */
  public void render(Composite composite, Reminder reminder) throws RemoteException
  {
    String text = reminder.getData().toString();
    Label label = GUI.getStyleFactory().createLabel(composite,SWT.NONE);
    label.setText(text);
  }

}


/**********************************************************************
 * $Log: ToStringRenderer.java,v $
 * Revision 1.1  2008/07/17 23:21:27  willuhn
 * @N Generische Darstellung von Remindern mittels "Renderer"-Interface geloest. Es fehlt noch eine Box fuer die Startseite, welche die faelligen Reminder anzeigt.
 * @N Laden und Speichern der Reminder mittels XMLEncoder/XMLDecoder
 *
 **********************************************************************/
