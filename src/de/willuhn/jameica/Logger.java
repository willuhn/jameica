/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Logger.java,v $
 * $Revision: 1.2 $
 * $Date: 2003/11/13 00:37:35 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * Kleiner System-Logger.
 * @author willuhn
 */
public class Logger
{

  private OutputStream target;

  private final static String DEBUG  = "DEBUG";
  private final static String INFO   = "INFO";
  private final static String WARN   = "WARN";
  private final static String ERROR  = "ERROR";
  
  /**
   * ct.
   * @param target Outputstream, in den die Log-Ausgaben geschrieben werden sollen.
   */
  public Logger(OutputStream target)
  {
    if (target == null)
      this.target = System.out;
    else 
      this.target = target;
  }
  
  /**
   * Schreibt eine Message vom Typ "debug" ins Log.
   * @param message zu loggende Nachricht.
   */
  public void debug(String message)
  {
    if (!Application.DEBUG) return;
    write(DEBUG,message);
  }

  /**
   * Schreibt eine Message vom Typ "info" ins Log.
   * @param message zu loggende Nachricht.
   */
  public void info(String message)
  {
    write(INFO,message);
  }

  /**
   * Schreibt eine Message vom Typ "warn" ins Log.
   * @param message zu loggende Nachricht.
   */
  public void warn(String message)
  {
    write(WARN,message);
  }

  /**
   * Schreibt eine Message vom Typ "error" ins Log.
   * @param message zu loggende Nachricht.
   */
  public void error(String message)
  {
    write(ERROR,message);
  }

  /**
   * Schliesst den Logger und die damit verbundene Log-Datei.
   */
  public void close()
	{
		try {
			target.flush();
			target.close();
		}
		catch (IOException io)
		{
		}
	}

  /**
   * Interne Methode zum Formatieren und Schreiben der Meldungen.
   * @param level Name des Log-Levels.
   * @param message zu loggende Nachricht.
   */
  private void write(String level, String message)
  {
    String s = "["+new Date().toString()+"] ["+level+"] " + message + "\n";
    try
    {
      target.write(s.getBytes());
    } catch (IOException e) {}
  }
}

/*********************************************************************
 * $Log: Logger.java,v $
 * Revision 1.2  2003/11/13 00:37:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
