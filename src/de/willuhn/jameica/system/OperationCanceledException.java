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
 * Diese Exception wird geworfen, wenn der User eine Aktion abgebrochen hat.
 * Passiert zum Beispiel wenn er in einem Dialog den Abbrechen-Knopf drueckt,
 * waehrend eine Aktion laeuft.
 * Hinweis: Die Exception ist von RuntimeException abgeleitet und muss
 * daher nicht explizit gefangen werden. 
 */
public class OperationCanceledException extends RuntimeException
{

  private static final long serialVersionUID = 245212333678410413L;

  /**
   * ct.
   */
  public OperationCanceledException()
  {
    super();
  }

  /**
   * ct.
   * @param message
   */
  public OperationCanceledException(String message)
  {
    super(message);
  }

  /**
   * ct.
   * @param cause
   */
  public OperationCanceledException(Throwable cause)
  {
    super(cause);
  }

  /**
   * ct.
   * @param message
   * @param cause
   */
  public OperationCanceledException(String message, Throwable cause)
  {
    super(message, cause);
  }

}


/**********************************************************************
 * $Log: OperationCanceledException.java,v $
 * Revision 1.1  2004/10/19 23:33:44  willuhn
 * *** empty log message ***
 *
 **********************************************************************/