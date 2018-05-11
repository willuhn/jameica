/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.system;

import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ProgressMonitor;

/**
 * Eine ProgressMonitor-Implementierung, die die Ausgaben ins Log schreibt.
 */
public class ConsoleMonitor implements ProgressMonitor
{
  private int completed = 0;
  private int status    = ProgressMonitor.STATUS_NONE;
  private String text   = null;
  
  /**
   * @see de.willuhn.util.ProgressMonitor#setPercentComplete(int)
   */
  public void setPercentComplete(int percent)
  {
    this.completed = percent;
    Logger.debug("completed: " + this.completed + " %");
  }
  
  /**
   * @see de.willuhn.util.ProgressMonitor#addPercentComplete(int)
   */
  public void addPercentComplete(int percent)
  {
    this.completed += percent;
    Logger.debug("completed: " + this.completed + " %");
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#getPercentComplete()
   */
  public int getPercentComplete()
  {
    return this.completed;
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#setStatus(int)
   */
  public void setStatus(int status)
  {
    this.status = status;
  }
  
  /**
   * Liefert den aktuellen Status-Code.
   * @return der aktuelle Status-Code.
   */
  public int getStatus()
  {
    return this.status;
  }
  
  /**
   * Liefert den aktuellen Statustext.
   * @return der aktuelle Statustext.
   */
  public String getStatusText()
  {
    return this.text;
  }
  
  /**
   * @see de.willuhn.util.ProgressMonitor#setStatusText(java.lang.String)
   */
  public void setStatusText(String text)
  {
    this.text = text;
    
    Level level = Level.INFO;
    switch (this.status)
    {
      case ProgressMonitor.STATUS_ERROR:
        level = Level.ERROR;
        break;
      case ProgressMonitor.STATUS_CANCEL:
        level = Level.WARN;
        break;
    }
    
    Logger.write(level,text);
  }
  
  /**
   * @see de.willuhn.util.ProgressMonitor#log(java.lang.String)
   */
  public void log(String msg)
  {
    Logger.info(msg);
  }
}

/*********************************************************************
 * $Log: ConsoleMonitor.java,v $
 * Revision 1.2  2011/09/07 17:18:57  willuhn
 * @N getStatus und getStatusText
 *
 * Revision 1.1  2011-08-02 15:06:47  willuhn
 * @N ConsoleMonitor in extra Klasse ausgelagert
 *
 **********************************************************************/