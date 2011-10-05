/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/StatusBar.java,v $
 * $Revision: 1.56 $
 * $Date: 2011/10/05 16:53:22 $
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
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;

/**
 * Bildet die Statusleiste der Anwendung ab.
 * @author willuhn
 */
public class StatusBar implements Part
{

  private ArrayList items = new ArrayList();
  
  private Composite status;

	private StackLayout progressStack;
		private Composite progressComp;
		private ProgressBar progress;
		private ProgressBar noProgress;
  
  /**
   * Fuegt der Statusbar ein neues Element hinzu.
   * @param item das hinzufuegende Element.
   */
  public void addItem(StatusBarItem item)
  {
    this.items.add(item);
  }
  
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    int height = 20;
    try
    {
      FontData font = Font.DEFAULT.getSWTFont().getFontData()[0];
      int h = SWTUtil.pt2px(font.getHeight());
      if (h > 0)
        height = h;
    }
    catch (Throwable t)
    {
      // ignore
    }
    
		this.status = new Composite(parent, SWT.NONE);
		GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = height + 12; // 12 Pixel fuer den Rand
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
		gd.heightHint = height + 10; // hier nochmal 10 Pixel
		progressComp.setLayoutData(gd);
		progressStack = new StackLayout();
		progressComp.setLayout(progressStack);
		
		progress = new ProgressBar(progressComp, SWT.INDETERMINATE);
		progress.setToolTipText(Application.getI18n().tr("Vorgang wird bearbeitet..."));
		noProgress = new ProgressBar(progressComp, SWT.NONE);
		progressStack.topControl = noProgress;


    int size = this.items.size();

		Composite tComp = new Composite(status,SWT.BORDER);
		tComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout tgd = new GridLayout((2 * size) - 1,false);
		tgd.marginHeight = 0;
		tgd.marginWidth = 0;
		tgd.horizontalSpacing = 0;
		tgd.verticalSpacing = 0;
		tComp.setLayout(tgd);
    
    for (int i=0;i<size;++i)
    {
      StatusBarItem item = (StatusBarItem) this.items.get(i);
      item.paint(tComp);
      if (i < (size - 1))
      {
        final Label sep = GUI.getStyleFactory().createLabel(tComp, SWT.SEPARATOR | SWT.VERTICAL);
        final GridData sepgd = new GridData(GridData.FILL_VERTICAL);
        sepgd.widthHint = 5;
        sep.setLayoutData(sepgd);
      }
    }

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
   * Ersetzt den aktuellen Statustext rechts unten gegen den uebergebenen.
   * @param message anzuzeigender Text.
   * Nachrichten sollten direkt ueber die MessagingFactory mit
   * dem Nachrichtentyp StatusBarMessage gesendet werden.
   */
  public void setSuccessText(final String message)
  {
    Application.getMessagingFactory().sendMessage(new StatusBarMessage(message,StatusBarMessage.TYPE_SUCCESS));
  }

  /**
   * Ersetzt den aktuellen Statustext rechts unten gegen den uebergebenen.
   * Formatiert die Anzeige hierbei aber rot als Fehler.
   * @param message anzuzeigender Text.
   * Nachrichten sollten direkt ueber die MessagingFactory mit
   * dem Nachrichtentyp StatusBarMessage gesendet werden.
   */
  public void setErrorText(final String message)
  {
    Application.getMessagingFactory().sendMessage(new StatusBarMessage(message,StatusBarMessage.TYPE_ERROR));
  }
}


/*********************************************************************
 * $Log: StatusBar.java,v $
 * Revision 1.56  2011/10/05 16:53:22  willuhn
 * @C Messages an "jameica.popup" werden jetzt sowohl im GUI- als auch im Server-Mode vom gemeinsamen Consumer "PopupMessageConsumer" behandelt
 *
 * Revision 1.55  2008-07-18 17:12:22  willuhn
 * @N ReminderPopupAction zum Anzeigen von Remindern als Popup
 * @C TextMessage serialisierbar
 *
 * Revision 1.54  2008/07/18 13:01:08  willuhn
 * @N Popup
 *
 * Revision 1.53  2007/05/14 11:18:09  willuhn
 * @N Hoehe der Statusleiste abhaengig von DPI-Zahl und Schriftgroesse
 * @N Default-Schrift konfigurierbar und Beruecksichtigung dieser an mehr Stellen
 *
 * Revision 1.52  2007/04/01 22:15:22  willuhn
 * @B Breite des Statusbarlabels
 * @B Redraw der Statusleiste
 **********************************************************************/
