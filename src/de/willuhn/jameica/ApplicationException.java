/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/ApplicationException.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/11/27 00:22:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica;

/**
 * Diese Exception muss geworfen werden, wenn Fehler auftreten
 * die dem Anwender gezeigt werden sollen. Klassicher Fall:
 * Benutzer gibt ein Datum ein. In der Business-Logik wird es
 * auf syntaktische Richtigkeit geprueft. Ist es falsch, wirft
 * die Prueffunktion diese Exception. Die Anzeige-Schicht
 * faengt sie, entnimmt den Fehlertext via getMessage() und
 * zeigt ihn in der Oberflaeche an.
 * Konsequenz: Fehlertexte in dieser Exception muessen fuer den
 * End-Benutzer! verstaendlich formuliert sein.
 * @author willuhn
 */
public class ApplicationException extends Exception
{

  /**
   * Erzeugt eine neue Exception.
   */
  public ApplicationException()
  {
    super();
  }

  /**
   * Erzeugt eine neue Exception.
   * @param message Fehlertext.
   */
  public ApplicationException(String message)
  {
    super(message);
  }

  /**
   * Erzeugt eine neue Exception.
   * @param cause urspruenglicher Grund.
   */
  public ApplicationException(Throwable cause)
  {
    super(cause);
  }

  /**
   * Erzeugt eine neue Exception.
   * @param message Fehlertext. 
   * @param cause urspruenglicher Grund.
   */
  public ApplicationException(String message, Throwable cause)
  {
    super(message, cause);
  }

  /**
   * @see java.lang.Throwable#getLocalizedMessage()
   */
  public String getLocalizedMessage()
  {
    return I18N.tr(getMessage());
  }

}

/*********************************************************************
 * $Log: ApplicationException.java,v $
 * Revision 1.1  2003/11/27 00:22:17  willuhn
 * @B paar Bugfixes aus Kombination RMI + Reflection
 * @N insertCheck(), deleteCheck(), updateCheck()
 * @R AbstractDBObject#toString() da in RemoteObject ueberschrieben (RMI-Konflikt)
 *
 **********************************************************************/