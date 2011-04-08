/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/print/PrintSupport.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/04/08 13:37:35 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.print;

import net.sf.paperclips.PrintJob;
import de.willuhn.util.ApplicationException;

/**
 * Generisches Interface zur Erzeugung von Druck-Jobs.
 */
public interface PrintSupport
{
  /**
   * Erzeugt den Druck-Job.
   * @return der Druck-Job.
   * @throws ApplicationException
   */
  public PrintJob print() throws ApplicationException;
}



/**********************************************************************
 * $Log: PrintSupport.java,v $
 * Revision 1.1  2011/04/08 13:37:35  willuhn
 * @N Neues PrintSupport-Interface - andernfalls muesste man den Druck-Auftrag vor Ausfuehrung der Action - und damit vor dem Klick auf den Button - erstellen
 *
 **********************************************************************/