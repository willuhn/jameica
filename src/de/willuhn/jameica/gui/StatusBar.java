/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/StatusBar.java,v $
 * $Revision: 1.23 $
 * $Date: 2004/06/10 20:56:53 $
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.util.ArrayEnumeration;
import de.willuhn.util.History;

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

	private boolean statusIn = false;
	private boolean actionIn = false;

  /**
   * Erzeugt eine neue Statusleiste.
   * @param parent Das Composite, in den die Statusbar gemalt werden soll.
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
		progress.setToolTipText(Application.getI18n().tr("Vorgang wird bearbeitet..."));
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
		statusText.setToolTipText(Application.getI18n().tr("Klicken Sie hier, um die letzten Zeilen des System-Logs anzuzeigen."));
		statusText.addMouseListener(new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				if (e.button != 1)
				{
					// Bei rechter oder mittlerer Maustaste machen wir nur den Text leer
					setStatusText("");
					return;
				}

				if (actionIn)
				{
					// huh, die andere Seite ist schon offen, machen wir erstmal zu
					actionIn = false;
					GUI.getView().snapOut();
				}
				if (statusIn)
				{
					// wir werden schon angezeigt, dann zoomen wir uns wieder raus
					statusIn = false;
					GUI.getView().snapOut();
					return;					
				}
				showLastMessages(new ArrayEnumeration(Application.getLog().getLastLines()),true);
				statusIn = true;
			}
		});

    actionText = new CLabel(tComp, SWT.SHADOW_IN);
    GridData at = new GridData(GridData.FILL_HORIZONTAL);
		actionText.setAlignment(SWT.RIGHT);
    actionText.setLayoutData(at);
    actionText.setText("");
		actionText.setToolTipText(Application.getI18n().tr("Klicken Sie hier, um die letzten Meldungen der Anwendung anzuzeigen."));
    actionText.addMouseListener(new MouseAdapter()
    {
      public void mouseUp(MouseEvent e)
      {
				if (e.button != 1)
				{
					// Bei rechter oder mittlerer Maustaste machen wir nur den Text leer
					setSuccessText("");
					return;
				}

				if (statusIn)
				{
					// huh, die andere Seite ist schon offen, machen wir erstmal zu
					statusIn = false;
					GUI.getView().snapOut();
				}
				if (actionIn)
				{
					// wir werden schon angezeigt, dann zoomen wir uns wieder raus
					actionIn = false;
					GUI.getView().snapOut();
					return;					
				}
				showLastMessages(lastActionMessages.elements(),false);
				actionIn = true;
      }
    });
    
	}
	
	/**
   * Schaltet den Progress-Balken ein.
   */
  public synchronized void startProgress()
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
	public synchronized void stopProgress()
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
  public void setStatusText(final String message)
	{
		GUI.getDisplay().asyncExec(new Runnable() {
			public void run() {
				statusText.setText(" " + (message == null ? "" : message));
		    status.layout();
			}
		});
	}

  /**
   * Ersetzt den aktuellen Statustext rechts unten gegen den uebergebenen.
   * @param message anzuzeigender Text.
   */
  public void setSuccessText(final String message)
  {
		if (message == null)
			return;

		if (!"".equals(message))
	    lastActionMessages.push("[" + new Date().toString() + "] " + message);

		GUI.getDisplay().asyncExec(new Runnable() {
      public void run() {
				actionText.setForeground(Color.SUCCESS.getSWTColor());
				actionText.setText(message);
      }
    });
		SWTUtil.startGUITimeout(10000l,new Listener() {
			public void handleEvent(Event event) {
				actionText.setText("");
			}
		});
  }

	/**
	 * Ersetzt den aktuellen Statustext rechts unten gegen den uebergebenen.
	 * Formatiert die Anzeige hierbei aber rot als Fehler.
	 * @param message anzuzeigender Text.
	 */
	public void setErrorText(final String message)
	{
		if (message == null)
			return;

		if (!"".equals(message))
			lastActionMessages.push("[" + new Date().toString() + "] " + message);

		GUI.getDisplay().asyncExec(new Runnable() {
			public void run() {
				actionText.setForeground(Color.ERROR.getSWTColor());
				actionText.setText(message);
			}
		});
		SWTUtil.startGUITimeout(10000l,new Listener() {
			public void handleEvent(Event event) {
				actionText.setText("");
			}
		});
	}

  /**
   * Zeigt die letzten Meldungen an.
   * @param e Enumeration mit den Melduingen.
   * @param alignRight Ausrichtung.
   */
  private void showLastMessages(Enumeration e, boolean alignRight)
	{
		Composite snapin = GUI.getView().getSnapin();

		TablePart table = new TablePart(e,null);
		table.addColumn(Application.getI18n().tr("Meldungen"),null);
		try
    {
      table.paint(snapin);
      GUI.getView().snapIn();
    }
    catch (RemoteException re) {}

	}
}


/*********************************************************************
 * $Log: StatusBar.java,v $
 * Revision 1.23  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.22  2004/05/26 23:23:23  willuhn
 * @N Timeout fuer Messages in Statusbars
 *
 * Revision 1.21  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.20  2004/04/29 23:05:54  willuhn
 * @N new snapin feature
 *
 * Revision 1.19  2004/04/29 21:21:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/04/12 19:16:00  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.17  2004/03/30 22:08:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/03/24 00:46:03  willuhn
 * @C refactoring
 *
 * Revision 1.15  2004/03/05 00:40:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
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
