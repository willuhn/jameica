/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/AbstractView.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/10/23 21:50:06 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.Application;

public abstract class AbstractView
{

	Composite parent;

  public abstract void bind();

  public abstract void unbind();

  public void setHeadline(String headline)
	{
		final Label label = new Label(getParent(), SWT.NONE);
		FontData format = new FontData();
		format.height = 8;
		format.setName("Verdana");
		format.setStyle(SWT.BOLD);
		label.setFont(new Font(Application.display,format));
		label.setForeground(new Color(Application.display,0,0,0));
		label.setText(headline);
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
 * Revision 1.1  2003/10/23 21:50:06  willuhn
 * initial checkin
 *
 ***************************************************************************/
