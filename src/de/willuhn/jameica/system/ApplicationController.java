/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/ApplicationController.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/03/15 16:25:32 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.system;

import de.willuhn.util.ApplicationException;

/**
 * Gemeinsames Interface des Controllers, welcher ueber den
 * Main-Loop wacht. Den gibt es in zwei Ausfuehrungen. Einmal
 * als Server und einmal als GUI.
 * @author willuhn
 */
public interface ApplicationController
{
  
  /**
   * Initialisiert den Controller.
   * @throws ApplicationException
   */
  public void init() throws ApplicationException;

  /**
   * Beendet den Controller
   */
  public void shutDown();
  
  /**
   * Erzeugt ein Callback-Objekt ueber das die Anwendung mit
   * dem User kommunizieren kann.
   * @return Callback.
   */
  public ApplicationCallback getApplicationCallback();
  
  /**
   * Startet einen Task im Hintergrund.
   * @param task der zu startende Task.
   */
  public void start(final BackgroundTask task);
}


/*********************************************************************
 * $Log: ApplicationController.java,v $
 * Revision 1.2  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 * Revision 1.1  2006/01/18 18:40:21  web0
 * @N Redesign des Background-Task-Handlings
 *
 *********************************************************************/