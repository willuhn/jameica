/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/StatusBar.java,v $
 * $Revision: 1.7 $
 * $Date: 2004/01/08 20:50:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.Enumeration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.views.parts.Table;
import de.willuhn.util.ArrayEnumeration;
import de.willuhn.util.History;
import de.willuhn.util.I18N;

/**
 * Bildet die Statusleiste der Anwendung ab.
 * @author willuhn
 */
public class StatusBar {

	private Label statusText;
  private Label actionText;
  private Composite status;
  
  private History lastActionMessages;

  /**
   * Erzeugt eine neue Statusleiste.
   */
  protected StatusBar() {

		// init lastActionMessage queue
		lastActionMessages = new History(20);

		status = new Composite(GUI.getShell(), SWT.BORDER);

		GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = 17;
		status.setLayoutData(data);

    GridLayout layout = new GridLayout();
    layout.makeColumnsEqualWidth = true;
    layout.marginHeight = 1;
    layout.marginWidth = 1;
    layout.numColumns = 2;
    layout.horizontalSpacing = 1;
    layout.verticalSpacing = 1;
		status.setLayout(layout);

		statusText = new Label(status, SWT.NONE);
    statusText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		statusText.setText("");
		statusText.setToolTipText(I18N.tr("Klicken Sie hier, um die letzten Zeilen des System-Logs anzuzeigen."));
		statusText.addMouseListener(new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				showLastMessages(new ArrayEnumeration(Application.getLog().getLastLines()),true);
			}
		});

    actionText = new Label(status, SWT.NONE);
    GridData right = new GridData(GridData.HORIZONTAL_ALIGN_END);
    actionText.setLayoutData(right);
    actionText.setText("");
		actionText.setToolTipText(I18N.tr("Klicken Sie hier, um die letzten Meldungen der Anwendung anzuzeigen."));
    actionText.addMouseListener(new MouseAdapter()
    {
      public void mouseUp(MouseEvent e)
      {
      	showLastMessages(lastActionMessages.elements(),false);
      }
    });
    
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
		if (message == null || "".equals(message))
			return;

    lastActionMessages.push("[" + new Date().toString() + "] " + message);

		// String lines[] = (String[]) lastActionMessages.toArray(new String[lastActionMessages.size()]);
    actionText.setText(message);
    status.layout();
  }

	/**
   * Zeigt die letzten Meldungen an.
   */
  private void showLastMessages(Enumeration e, boolean alignRight)
	{
		Display display = GUI.getDisplay();
		Shell shell = new Shell(GUI.getShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText(I18N.tr("letzte Meldungen"));
		shell.setLayout(new GridLayout(1,false));

		Table table = new Table(e,null);
		table.addColumn(I18N.tr("Meldungen"),null);
		try
    {
      table.paint(shell);
    }
    catch (RemoteException re) {}

		shell.pack();
		shell.setSize(shell.getBounds().width,150);
		if (alignRight)
			shell.setLocation(display.getCursorLocation());
		else 
			shell.setLocation(display.getCursorLocation().x-shell.getBounds().width,display.getCursorLocation().y);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
	}
}


/*********************************************************************
 * $Log: StatusBar.java,v $
 * Revision 1.7  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.6  2004/01/06 20:11:22  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
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
