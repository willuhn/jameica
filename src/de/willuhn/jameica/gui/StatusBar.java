/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/StatusBar.java,v $
 * $Revision: 1.2 $
 * $Date: 2003/10/29 00:41:26 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class StatusBar {

	private Label statusText;
  private Label actionText;
  private Composite status;

  protected StatusBar() {

		status = new Composite(GUI.shell, SWT.BORDER);

		GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = 14;
		status.setLayoutData(data);

    GridLayout layout = new GridLayout();
    layout.makeColumnsEqualWidth = true;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.numColumns = 2;
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 0;
		status.setLayout(layout);

		this.statusText = new Label(status, SWT.BOLD);
    this.statusText.setLayoutData(new GridData());
		this.statusText.setText("");

    this.actionText = new Label(status, SWT.RIGHT);
    GridData right = new GridData(GridData.HORIZONTAL_ALIGN_END);
    this.actionText.setLayoutData(right);
    this.actionText.setText("");

	}
	
  protected void setStatusText(String message)
	{
		statusText.setText(" " + (message == null ? "" : message));
    status.layout();
	}

  protected void setActionText(String message)
  {
    actionText.setText(" " + (message == null ? "" : message));
    status.layout();
  }

}


/*********************************************************************
 * $Log: StatusBar.java,v $
 * Revision 1.2  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
