/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/ProgressBar.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/10/07 18:05:26 $
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
import de.willuhn.jameica.util.BackgroundTask;
import de.willuhn.util.ProgressMonitor;

/**
 * Implementierung eines Progress-Monitors, der seine Ausgaben in
 * Form eines Fortschrittsbalkens anzeigt.
 */
public class ProgressBar implements ProgressMonitor, Part
{
	private static int BAR_MAXIMUM	= 300;

	private int status	= BackgroundTask.STATUS_NONE;

	private TextPart log																= null;
	private Composite parent														= null;
	private org.eclipse.swt.widgets.ProgressBar bar			= null;
	private Label barLabel															= null;

  /**
   * @see de.willuhn.jameica.util.ProgressMonitor#percentComplete(int)
   */
  public void percentComplete(final int percent)
  {
		if (bar != null && !bar.isDisposed())
		{
			GUI.getDisplay().syncExec(new Runnable()
			{
				public void run()
				{
					if (percent < 0)
					{
						bar.setSelection(0);
						return;
					} 
					if (percent > 100)
					{
						bar.setSelection(100);
						return;
					}

					bar.setSelection(percent * (BAR_MAXIMUM / 100));
				}
			});
		}

  }

  /**
   * @see de.willuhn.jameica.util.ProgressMonitor#setStatus(int)
   */
  public void setStatus(int status)
  {
  	this.status = status;
  }

  /**
   * @see de.willuhn.jameica.util.ProgressMonitor#setStatusText(java.lang.String)
   */
  public void setStatusText(final String text)
  {
  	if (text == null)
  		return;

		if (barLabel != null && !barLabel.isDisposed())
		{
			GUI.getDisplay().syncExec(new Runnable()
			{
				public void run()
				{
					barLabel.setText(text);
					if (status == BackgroundTask.STATUS_DONE)
						barLabel.setForeground(Color.SUCCESS.getSWTColor());
					else if (status == BackgroundTask.STATUS_ERROR || status == BackgroundTask.STATUS_CANCEL)
						barLabel.setForeground(Color.ERROR.getSWTColor());
					else
						barLabel.setForeground(Color.WIDGET_FG.getSWTColor());
				}
			});
		}
  }

  /**
   * @see de.willuhn.jameica.util.ProgressMonitor#log(java.lang.String)
   */
  public void log(final String msg)
  {
  	if (msg == null)
  		return;

		GUI.getDisplay().syncExec(new Runnable()
		{
			public void run()
			{
				log.appendText(msg);
			}
		});
  }

	/**
   * Loescht das Log.
   */
  public void clearLog()
	{
		if (this.log == null)
			return;
		this.log.clear();
	}

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
  	this.parent = new Composite(parent,SWT.NONE);
		this.parent.setBackground(Color.BACKGROUND.getSWTColor());
  	this.parent.setLayout(new GridLayout(2,false));
  	this.parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		bar = new org.eclipse.swt.widgets.ProgressBar(this.parent, SWT.SMOOTH);
		GridData g = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		g.widthHint = BAR_MAXIMUM;
		bar.setLayoutData(g);
		bar.setMaximum(BAR_MAXIMUM);
		bar.setSelection(0);

		barLabel = new Label(this.parent,SWT.NONE);
		barLabel.setBackground(Color.BACKGROUND.getSWTColor());
		barLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite comp = new Composite(this.parent,SWT.BORDER);
		comp.setBackground(Color.BACKGROUND.getSWTColor());
		GridLayout gl = new GridLayout(2,false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		comp.setLayout(gl);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		comp.setLayoutData(gd);
		
		log = new TextPart();
		log.setAutoscroll(true);
		log.setWordWrap(false);
		log.paint(comp);
  }
}


/**********************************************************************
 * $Log: ProgressBar.java,v $
 * Revision 1.4  2004/10/07 18:05:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/08/18 23:14:19  willuhn
 * @D Javadoc
 *
 * Revision 1.2  2004/08/11 00:39:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/08/09 22:24:16  willuhn
 * *** empty log message ***
 *
 **********************************************************************/