/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/ProgressBar.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/08/09 22:24:16 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.ProgressMonitor;

/**
 * Implementierung eines Progress-Monitors, der seine Ausgaben in
 * Form eines Fortschrittsbalkens anzeigt.
 */
public class ProgressBar implements ProgressMonitor, Part
{
	private static int BAR_MAXIMUM	= 300;

	private int percentComplete = 0;
	private int status					= BackgroundTask.STATUS_NONE;
	private String statusText 	= "";
	private String logText			= null;

	private TextPart log																= null;
	private Composite parent														= null;
	private org.eclipse.swt.widgets.ProgressBar bar			= null;
	private Label barLabel															= null;

  /**
   * @see de.willuhn.jameica.system.ProgressMonitor#percentComplete(int)
   */
  public void percentComplete(int percent)
  {
  	if (percent < 0) 		percent = 0;
  	if (percent > 100) 	percent = 100;
  	this.percentComplete = percent;
		refresh();
  }

  /**
   * @see de.willuhn.jameica.system.ProgressMonitor#setStatus(int)
   */
  public void setStatus(int status)
  {
  	this.status = status;
  	refresh();
  }

  /**
   * @see de.willuhn.jameica.system.ProgressMonitor#setStatusText(java.lang.String)
   */
  public void setStatusText(String text)
  {
  	if (text == null)
  		return;
  	this.statusText = text;
  	refresh();
  }

  /**
   * @see de.willuhn.jameica.system.ProgressMonitor#log(java.lang.String)
   */
  public void log(final String msg)
  {
  	if (msg == null)
  		return;
		this.logText = msg;
		refresh();
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
  	this.parent = new Composite(parent,SWT.NONE);
  	this.parent.setLayout(new GridLayout(2,false));
  	this.parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		bar = new org.eclipse.swt.widgets.ProgressBar(this.parent, SWT.SMOOTH);
		GridData g = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		g.widthHint = BAR_MAXIMUM;
		bar.setLayoutData(g);
		bar.setMaximum(BAR_MAXIMUM);
		bar.setSelection(percentComplete);

		barLabel = new Label(parent,SWT.NONE);
		barLabel.setBackground(Color.BACKGROUND.getSWTColor());
		barLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		barLabel.setText(statusText);

		log = new TextPart();
		log.setAutoscroll(true);
		log.setWordWrap(false);
  }

	/**
   * Aktualisiert die Anzeige.
   */
  private synchronized void refresh()
	{
		if (bar != null && !bar.isDisposed())
		{
			GUI.getDisplay().syncExec(new Runnable()
			{
				public void run()
				{
					bar.setSelection(percentComplete * (BAR_MAXIMUM / 100));
				}
			});
		}

		if (barLabel != null && !barLabel.isDisposed())
		{
			GUI.getDisplay().syncExec(new Runnable()
			{
				public void run()
				{
					barLabel.setText(statusText);
					if (status == BackgroundTask.STATUS_DONE)
						barLabel.setForeground(Color.SUCCESS.getSWTColor());
					if (status == BackgroundTask.STATUS_ERROR)
					barLabel.setForeground(Color.ERROR.getSWTColor());
				}
			});
		}

		
		if (log != null)
		{
			GUI.getDisplay().syncExec(new Runnable()
      {
        public void run()
        {
					log.appendText(logText);
        }
      });
		}

	}
}


/**********************************************************************
 * $Log: ProgressBar.java,v $
 * Revision 1.1  2004/08/09 22:24:16  willuhn
 * *** empty log message ***
 *
 **********************************************************************/