/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/ServiceSettings.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/08 20:50:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.views;

import de.willuhn.jameica.gui.views.parts.Headline;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * 
 */
public class ServiceSettings extends AbstractView
{

  /**
   * @param o
   */
  public ServiceSettings(Object o)
  {
    super(o);
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind()
  {
  	String name = (String) getCurrentObject();
  	new Headline(getParent(),I18N.tr("Eigenschaften des Services") + " " + name);
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
  }

}


/**********************************************************************
 * $Log: ServiceSettings.java,v $
 * Revision 1.2  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.1  2004/01/06 01:27:30  willuhn
 * @N table order
 *
 **********************************************************************/