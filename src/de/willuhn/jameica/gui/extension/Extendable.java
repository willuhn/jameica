/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
   * @return Liefert die ID des Extendable.
   */
  public String getExtendableID();
}


/*********************************************************************
 * $Log: Extendable.java,v $
 * Revision 1.5  2005/06/15 16:10:57  web0
 * @B javadoc fixes
 *
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