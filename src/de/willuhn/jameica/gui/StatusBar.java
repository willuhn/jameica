/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/StatusBar.java,v $
 * $Revision: 1.13 $
 * $Date: 2004/02/23 20:30:34 $
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
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.parts.Table;
import de.willuhn.util.ArrayEnumeration;
import de.willuhn.util.History;
import de.willuhn.util.I18N;

/**
 * Bildet die Statusleiste der Anwendung ab.
 * @author willuhn
 */
public class StatusBar {

	private CLabel statusText;
  private CLabel actionText;
  private Composite status;

	private StackLayout progressStack;
		private Composite progressComp;
		private ProgressBar progress;
		private ProgressBar noProgress;
  
  private History lastActionMessages;

  /**
   * Erzeugt eine neue Statusleiste.
   */
  protected StatusBar(Composite parent) {

		// init lastActionMessage queue
		lastActionMessages = new History(20);

		status = new Composite(parent, SWT.BORDER);
		GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = 20;
		status.setLayoutData(data);

    GridLayout layout = new GridLayout(2,false);
    layout.marginHeight = 1;
    layout.marginWidth = 1;
    layout.horizontalSpacing = 1;
    layout.verticalSpacing = 1;
		status.setLayout(layout);

		progressComp = new Composite(status, SWT.NONE);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint = 60;
		gd.heightHint = 18;
		progressComp.setLayoutData(gd);
		progressStack = new StackLayout();
		progressComp.setLayout(progressStack);
		
		progress = new ProgressBar(progressComp, SWT.INDETERMINATE);
		progress.setToolTipText(I18N.tr("Vorgang wird bearbeitet..."));
		noProgress = new ProgressBar(progressComp, SWT.NONE);
		progressStack.topControl = noProgress;



		Composite tComp = new Composite(status,SWT.NONE);
		tComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout tgd = new GridLayout(2,true);
		tgd.marginHeight = 0;
		tgd.marginWidth = 0;
		tgd.horizontalSpacing = 0;
		tgd.verticalSpacing = 0;
		tComp.setLayout(tgd);

		statusText = new CLabel(tComp, SWT.SHADOW_IN);
		statusText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		statusText.setText("");
		statusText.setToolTipText(I18N.tr("Klicken Sie hier, um die letzten Zeilen des System-Logs anzuzeigen."));
		statusText.addMouseListener(new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				showLastMessages(new ArrayEnumeration(Application.getLog().getLastLines()),true);
			}
		});

    actionText = new CLabel(tComp, SWT.SHADOW_IN);
    GridData at = new GridData(GridData.FILL_HORIZONTAL);
		actionText.setAlignment(SWT.RIGHT);
    actionText.setLayoutData(at);
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
   * Schaltet den Progress-Balken ein.
   */
  protected synchronized void startProgress()
	{
		Thread t = new Thread("progress")
		{
			public void run() {
				progressStack.topControl = progress;
				progressComp.layout();
			}
		};
		GUI.getDisplay().asyncExec(t);
	}

	/**
	 * Schaltet den Progress-Balken aus.
	 */
	protected synchronized void stopProgress()
	{
		GUI.getDisplay().syncExec(new Runnable() {
      public void run() {
				if (progressComp.isDisposed())
					return;
				progressStack.topControl = noProgress;
				progressComp.layout();
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
  protected void setActionText(final String message)
  {
		if (message == null)
			return;

		if (!"".equals(message))
	    lastActionMessages.push("[" + new Date().toString() + "] " + message);

		GUI.getDisplay().asyncExec(new Runnable() {
      public void run() {
				actionText.setText(message);
				status.layout();
      }
    });
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
 * Revision 1.13  2004/02/23 20:30:34  willuhn
 * @C refactoring in AbstractDialog
 *
 * Revision 1.12  2004/02/20 01:25:06  willuhn
 * @N nice dialog
 * @N busy indicator
 * @N new status bar
 *
 * Revision 1.11  2004/02/12 23:46:27  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.9  2004/01/25 18:39:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
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
