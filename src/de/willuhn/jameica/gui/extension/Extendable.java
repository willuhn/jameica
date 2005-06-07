/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/extension/Extendable.java,v $
 * $Revision: 1.4 $
 * $Date: 2005/06/07 21:57:32 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.extension;



/**
 * Basis-Interface einer erweiterbaren Komponente-
 */
public interface Extendable
{
  /**
   * Liefert die ID, ueber die das Extandable von den Extensions identifiziert wird.
   * @return
   */
  public String getExtendableID();
}


/*********************************************************************
 * $Log: Extendable.java,v $
 * Revision 1.4  2005/06/07 21:57:32  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/06/06 10:10:43  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/05/30 12:01:33  web0
 * @R removed gui packages from rmic.xml
 *
 * Revision 1.1  2005/05/25 16:11:47  web0
 * @N first code for extension system
 *
 *********************************************************************/