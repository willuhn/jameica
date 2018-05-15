/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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

  private ArrayList<StatusBarItem> items = new ArrayList<StatusBarItem>();
  
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
      StatusBarItem item = this.items.get(i);
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
   * Zeigt die angegebene Nachricht als Erfolgsmeldung an.
   * @param message anzuzeigender Text.
   */
  public void setSuccessText(final String message)
  {
    Application.getMessagingFactory().sendMessage(new StatusBarMessage(message,StatusBarMessage.TYPE_SUCCESS));
  }

  /**
   * Zeigt die angegebene Nachricht als Fehlermeldung an.
   * @param message anzuzeigender Text.
   */
  public void setErrorText(final String message)
  {
    Application.getMessagingFactory().sendMessage(new StatusBarMessage(message,StatusBarMessage.TYPE_ERROR));
  }
}
