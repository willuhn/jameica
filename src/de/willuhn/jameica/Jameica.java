/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Jameica.java,v $
 * $Revision: 1.7 $
 * $Date: 2004/03/06 18:24:24 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica;

import java.io.File;


/**
 * Ja, Jameica ist selbst auch nur ein Plugin ;).
 */
public class Jameica extends AbstractPlugin
{

  /**
   * @param file
   */
  public Jameica(File file) {
    super(file);
  }

  /**
   * @see de.willuhn.jameica.AbstractPlugin#init()
   */
  public boolean init() {
    return true;
  }

  /**
   * @see de.willuhn.jameica.AbstractPlugin#install()
   */
  public boolean install() {
    return true;
  }

  /**
   * @see de.willuhn.jameica.AbstractPlugin#update(double)
   */
  public boolean update(double oldVersion) {
    return true;
  }

  /**
   * @see de.willuhn.jameica.AbstractPlugin#shutDown()
   */
  public void shutDown() {
  }

}

/*********************************************************************
 * $Log: Jameica.java,v $
 * Revision 1.7  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.6  2004/03/03 22:27:11  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.5  2004/02/09 13:06:33  willuhn
 * @C added support for uncompressed plugins
 *
 * Revision 1.4  2003/12/30 02:35:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/12/29 18:10:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/12/29 17:44:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 **********************************************************************/