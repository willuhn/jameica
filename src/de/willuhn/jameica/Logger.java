/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Logger.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/10/23 21:49:46 $
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

public class Logger
{

  private OutputStream target;

  private final static String DEBUG  = "DEBUG";
  private final static String INFO   = "INFO";
  private final static String WARN   = "WARN";
  private final static String ERROR  = "ERROR";
  
  public Logger(OutputStream target)
  {
    if (target == null)
      this.target = System.out;
    else 
      this.target = target;
  }
  
  public void debug(String message)
  {
    if (!Application.DEBUG) return;
    write(DEBUG,message);
  }

  public void info(String message)
  {
    write(INFO,message);
  }

  public void warn(String message)
  {
    write(WARN,message);
  }

  public void error(String message)
  {
    write(ERROR,message);
  }

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
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
