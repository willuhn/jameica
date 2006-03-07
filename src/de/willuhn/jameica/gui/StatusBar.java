/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/StatusBar.java,v $
 * $Revision: 1.49 $
 * $Date: 2006/03/07 23:00:55 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui;

import java.rmi.RemoteException;

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

import de.willuhn.jameica.gui.internal.parts.LogList;
import de.willuhn.jameica.gui.parts.Panel;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

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
  
	private boolean snapIn     = false;
  
  /**
   * Erzeugt eine neue Statusleiste.
   * @param parent Das Composite, in den die Statusbar gemalt werden soll.
   */
  protected StatusBar(Composite parent)
  {

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

    actionText = new CLabel(tComp, SWT.SHADOW_IN);
    GridData at = new GridData(GridData.FILL_HORIZONTAL);
		actionText.setAlignment(SWT.RIGHT);
    actionText.setLayoutData(at);
    actionText.setText("");


    MouseAdapter ma = new MouseAdapter()
    {
      public void mouseUp(MouseEvent e)
      {
        if (e.button != 1)
        {
          // Bei rechter oder mittlerer Maustaste machen wir nur den Text leer
          setStatusText("");
          return;
        }
        
        if (GUI.getView().snappedIn())
          GUI.getView().snapOut();

        if (snapIn)
        {
          // wir werden schon angezeigt, dann zoomen wir uns wieder raus
          snapIn = false;
          return;         
        }
        try
        {
          Panel panel = new Panel(Application.getI18n().tr("System-Meldungen"),new LogList(),false);
          panel.addMinimizeListener(new Listener() {
            public void handleEvent(Event event)
            {
              if (GUI.getView().snappedIn())
                GUI.getView().snapOut();
              snapIn = false;
            }
          });
          panel.paint(GUI.getView().getSnapin());
          GUI.getView().snapIn();
          snapIn = true;
        }
        catch (RemoteException re)
        {
          Logger.error("unable to display log list",re);
          setErrorText(Application.getI18n().tr("Fehler beim Anzeigen der System-Meldungen"));
        }
      }
    };

    String s = Application.getI18n().tr("Klicken Sie hier, um die letzten Zeilen des System-Logs anzuzeigen.");
    statusText.setToolTipText(s);
    statusText.addMouseListener(ma);
    actionText.setToolTipText(s);
    actionText.addMouseListener(ma);
	}
	
	/**
   * Schaltet den Progress-Balken ein.
   */
  public synchronized void startProgress()
	{
		GUI.getDisplay().syncExec(new Runnable() {
      public void run()
      {
        if (progressComp == null || progressComp.isDisposed())
          return;
        progressStack.topControl = progress;
        progressComp.layout();
      }
    });
	}

	/**
	 * Schaltet den Progress-Balken aus.
	 */
	public synchronized void stopProgress()
	{
		GUI.getDisplay().syncExec(new Runnable() {
      public void run()
      {
        if (progressComp == null || progressComp.isDisposed())
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
        if (statusText != null && !statusText.isDisposed())
          statusText.setText(" " + (message == null ? "" : message));
        if (status != null && !status.isDisposed())
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
    setActionText(message,Color.SUCCESS);
  }

  /**
   * Ersetzt den aktuellen Statustext rechts unten gegen den uebergebenen.
   * Formatiert die Anzeige hierbei aber rot als Fehler.
   * @param message anzuzeigender Text.
   */
  public void setErrorText(final String message)
  {
    setActionText(message,Color.ERROR);
  }

  /**
   * Private Hilfs-Funktion, die den Action-Text setzt.
   * @param message anzuzeigender Text.
   * @param color Farbe.
   */
  private void setActionText(final String message, final Color color)
  {
    if (message == null)
      return;

		final long currentClick = System.currentTimeMillis();
    GUI.getDisplay().asyncExec(new Runnable() {
      public void run() {
        if (actionText != null && !actionText.isDisposed())
        actionText.setForeground(color.getSWTColor());
        actionText.setText(message);
				lastClick = currentClick;
      }
    });
    GUI.getDisplay().asyncExec(new Runnable() {
      public void run()
      {
        GUI.getDisplay().timerExec(10000,new Runnable()
            {
              public void run()
              {
                if (currentClick == lastClick && !actionText.isDisposed()) // nur entfernen, wenn wir der letzte Klick waren
                  actionText.setText("");
              }
            });
      }
    });
  }
	private long lastClick;
}


/*********************************************************************
 * $Log: StatusBar.java,v $
 * Revision 1.49  2006/03/07 23:00:55  web0
 * @C no border around log panel
 *
 * Revision 1.48  2006/03/07 22:43:14  web0
 * *** empty log message ***
 *
 * Revision 1.47  2006/03/07 18:24:04  web0
 * @N Statusbar and logview redesign
 *
 * Revision 1.46  2005/11/18 12:14:12  web0
 * @B dispose check
 *
 * Revision 1.45  2005/08/25 21:18:24  web0
 * @C changes accoring to findbugs eclipse plugin
 *
 * Revision 1.44  2005/07/26 22:58:34  web0
 * @N background task refactoring
 *
 * Revision 1.43  2005/07/11 08:31:24  web0
 * *** empty log message ***
 *
 * Revision 1.42  2005/06/27 15:35:52  web0
 * @N ability to store last table order
 *
 * Revision 1.41  2005/06/21 20:02:02  web0
 * @C cvs merge
 *
 * Revision 1.40  2005/06/10 22:13:09  web0
 * @N new TabGroup
 * @N extended Settings
 *
 * Revision 1.39  2005/06/03 17:14:41  web0
 * @N Livelog
 *
 * Revision 1.38  2004/12/31 19:33:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.37  2004/12/13 22:48:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.36  2004/11/17 19:02:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.35  2004/11/15 00:38:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.34  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.33  2004/11/10 17:48:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.32  2004/10/08 00:19:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.31  2004/08/30 15:03:28  willuhn
 * @N neuer Security-Manager
 *
 * Revision 1.30  2004/08/27 17:46:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.29  2004/08/18 23:14:19  willuhn
 * @D Javadoc
 *
 * Revision 1.28  2004/08/11 23:37:21  willuhn
 * @N Navigation ist jetzt modular erweiterbar
 *
 * Revision 1.27  2004/07/23 15:51:20  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.26  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.25  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.24  2004/06/17 22:07:12  willuhn
 * @C cleanup in tablePart and statusBar
 *
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
