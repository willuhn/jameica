/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/StatusBar.java,v $
 * $Revision: 1.4 $
 * $Date: 2003/12/11 21:00:54 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Bildet die Statusleiste der Anwendung ab.
 * @author willuhn
 */
public class StatusBar {

	private Label statusText;
  private Label actionText;
  private Composite status;

  /**
   * Erzeugt eine neue Statusleiste.
   */
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
	
  /**
   * Ersetzt den aktuellen Statustext links unten gegen den uebergebenen.
   * @param message anzuzeigender Text.
   */
  protected void setStatusText(String message)
	{
		statusText.setText(" " + (message == null ? "" : message));
    status.layout();
	}

  /**
   * Ersetzt den aktuellen Statustext rechts unten gegen den uebergebenen.
   * @param message anzuzeigender Text.
   */
  protected void setActionText(String message)
  {
    actionText.setText(" " + (message == null ? "" : message));
    status.layout();
  }

}


/*********************************************************************
 * $Log: StatusBar.java,v $
 * Revision 1.4  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.3  2003/11/13 00:37:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
