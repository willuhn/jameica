/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/OperationCanceledException.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/10/19 23:33:44 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
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