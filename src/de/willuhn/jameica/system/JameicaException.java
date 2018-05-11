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

/**
 * Jameica-eigene Runtime-Exception.
 * Wurde eingefuehrt, damit Exceptions nicht immer von den Plugins
 * behandelt werden muessen. Diese Exception wird von Jameica selbst im Server-
 * bzw. GUI-Loop gefangen.
 */
public class JameicaException extends RuntimeException
{
  /**
   * ct.
   */
  public JameicaException()
  {
    super();
  }

  /**
   * ct.
   * @param message
   * @param cause
   */
  public JameicaException(String message, Throwable cause)
  {
    super(message, cause);
  }

  /**
   * ct.
   * @param message
   */
  public JameicaException(String message)
  {
    super(message);
  }

  /**
   * ct.
   * @param cause
   */
  public JameicaException(Throwable cause)
  {
    super(cause);
  }

}



/**********************************************************************
 * $Log: JameicaException.java,v $
 * Revision 1.1  2011/07/12 15:21:30  willuhn
 * @N JameicaException
 *
 **********************************************************************/