/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Logger.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/01/03 18:08:05 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Vector;

/**
 * Kleiner System-Logger.
 * @author willuhn
 */
public class Logger
{

  private OutputStream target;

  // maximale Groesse des Log-Puffers (Zeilen-Anzahl)
  private final static int FIFO_SIZE = 40;

  // Ein FIFO mit den letzten Log-Eintraegen. Kann ganz nuetzlich sein,
  // wenn man irgendwo in der Anwendung mal die letzten Zeilen des Logs ansehen will.
  private Vector lastLines = new Vector(FIFO_SIZE);

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
	 * Schreibt den Fehler ins Log.
	 * @param message zu loggende Nachricht.
   * @param t Exception oder Error.
   */
  public void error(String message, Throwable t)
	{
		write(ERROR,message);
		ByteArrayOutputStream bos = null;
		try {
			bos = new ByteArrayOutputStream();
			t.printStackTrace(new PrintStream(bos));
			write(ERROR,bos.toString());
		}
		finally {
			try {
				bos.close();
			}
			catch (Exception npe) {}
		}
		
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
   * Liefert die letzten Zeilen des Logs.
   * @return String-Array mit den letzten Log-Eintraegen (einer pro Index).
   */
  public String[] getLastLines()
  {
    return (String[]) lastLines.toArray(new String[lastLines.size()]);
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
      synchronized(lastLines) { // wir wollen ja nicht, dass die FIFO aus'm Tritt kommt ;)
        lastLines.addElement(s);
        if (lastLines.size() >= FIFO_SIZE)
          lastLines.removeElementAt(0); // maximale Groesse erreicht, wir schneiden unten ab
      }

      if (!Application.DEBUG && Logger.DEBUG.equals(level))
      {
        // DEBUG-Meldungen werden nicht geschrieben, wenn die Anwendung nicht
        // im Debug-Mode laeuft. Im FIFO lastLines wollen wir sie aber dennoch haben.
        return;
      }
        

      target.write(s.getBytes());

    } catch (IOException e) {}
  }
}

/*********************************************************************
 * $Log: Logger.java,v $
 * Revision 1.4  2004/01/03 18:08:05  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.3  2003/12/10 00:47:12  willuhn
 * @N SearchDialog done
 * @N ErrorView
 *
 * Revision 1.2  2003/11/13 00:37:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
