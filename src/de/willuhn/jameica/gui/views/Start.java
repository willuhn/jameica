/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/Start.java,v $
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.util.Style;

public class Start extends AbstractView
{

	private Composite comp;

  public void bind()
  {
		init();

		{
			final Label label = new Label(comp, SWT.NONE);

			final GridData gridData = new GridData();
			gridData.horizontalSpan = 3;

			label.setLayoutData(gridData);
			label.setText("welcome");
			label.setBackground(Style.COLOR_WHITE);
		}


  }


	private void init()
	{
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 5;
		layout.marginWidth = 10;
		layout.makeColumnsEqualWidth = true;
		comp = new Composite(parent,SWT.NONE);
		comp.setBackground(Style.COLOR_WHITE);
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

  public void unbind()
  {
  }

}

/***************************************************************************
 * $Log: Start.java,v $
 * Revision 1.1  2003/10/23 21:50:06  willuhn
 * initial checkin
 *
 ***************************************************************************/