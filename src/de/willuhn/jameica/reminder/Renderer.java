/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/reminder/Attic/Renderer.java,v $
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

import org.eclipse.swt.widgets.Composite;


/**
 * In Jameica koennen ReminderMessages verschickt werden. Abhaengig
 * von Faelligkeit werden die dann auf der Startseite angzeigt.
 * Hierbei kann es sich um ganz verschiedene Arten von Erinnerungen
 * handeln. Es koennte nur ein Hinweistext sein (z.Bsp. "Ueberweisung
 * XY nicht vergessen") aber auch interaktive Reminder, bei denen
 * es dem Programmierer ueberlassen ist, wie der Reminder angezeigt
 * wird und welche Aktionen daraufhin moeglich sind.
 * Um hier einen moeglichst grossen Freiraum zu schaffen, kuemmert
 * sich Jameica lediglich um das Speichern der Reminder. Fuer die
 * Anzeige der Reminder steht auf der Startseite von Jameica eine
 * Box zur Verfuegung, in der alle faelligen Reminder angezeigt
 * werden. Durch Implementierung des Renderer-Interfaces
 * kann der Programmierer selbst festlegen, wie die Erinnerung
 * angezeigt wird.
 * 
 * WICHTIG: Der Programmierer muss sich selbst darum kuemmern,
 * den Reminder zu loeschen, wenn die Erinnerung als erledigt angesehen
 * werden kann. Das geschieht wie folgt:
 *
 * <pre>
 *   ReminderService service = (ReminderService) Application.getBootloader().getBootable(ReminderService.class);
 *   service.delete(reminder);
 * </pre>
 */
public interface Renderer
{
  /**
   * Rendert den uebergebenen Reminder auf das Composite.
   * @param composite das Composite.
   * @param reminder die Erinnerung.
   * @throws RemoteException
   */
  public void render(Composite composite, Reminder reminder) throws RemoteException;
}


/**********************************************************************
 * $Log: Renderer.java,v $
 * Revision 1.1  2008/07/17 23:21:27  willuhn
 * @N Generische Darstellung von Remindern mittels "Renderer"-Interface geloest. Es fehlt noch eine Box fuer die Startseite, welche die faelligen Reminder anzeigt.
 * @N Laden und Speichern der Reminder mittels XMLEncoder/XMLDecoder
 *
 **********************************************************************/
