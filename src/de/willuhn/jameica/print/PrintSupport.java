/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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