/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/AbstractView.java,v $
 * $Revision: 1.2 $
 * $Date: 2003/10/23 22:36:34 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.views;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public abstract class AbstractView
{

	Composite parent;

  public abstract void bind();

  public abstract void unbind();

  protected void createMainGrid()
  { 
    GridLayout layout = new GridLayout();
    layout.horizontalSpacing = 5;
    layout.verticalSpacing   = 5;
    layout.marginHeight      = 5;
    layout.marginWidth       = 10;
    parent.setLayout(layout);
  }	
  
  public Composite getParent()
	{
		return parent;
	}

  public void setParent(Composite parent)
	{
		this.parent = parent;
	}

}



/***************************************************************************
 * $Log: AbstractView.java,v $
 * Revision 1.2  2003/10/23 22:36:34  willuhn
 * @N added Menu
 *
 * Revision 1.1  2003/10/23 21:50:06  willuhn
 * initial checkin
 *
 ***************************************************************************/
