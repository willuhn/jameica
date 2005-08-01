/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/ProgressBar.java,v $
 * $Revision: 1.11 $
 * $Date: 2005/08/01 23:27:52 $
 * $Author: web0 $
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
import de.willuhn.util.ProgressMonitor;

/**
 * Implementierung eines Progress-Monitors, der seine Ausgaben in
 * Form eines Fortschrittsbalkens anzeigt.
 */
public class ProgressBar implements ProgressMonitor, Part
{
	private int status	= STATUS_NONE;

	private TextPart log																= null;
	private Composite parent														= null;
	private org.eclipse.swt.widgets.ProgressBar bar			= null;
	private Label barLabel															= null;
  private Label percentLabel                          = null;

  private int current = 0;

  /**
   * @see de.willuhn.util.ProgressMonitor#setPercentComplete(int)
   */
  public void setPercentComplete(int percent)
  {
    if (percent < 0)
      percent = 0;
    if (percent > 100)
      percent = 100;
    
    current = percent;
    
		GUI.getDisplay().syncExec(new Runnable()
		{
			public void run()
			{
        if (bar != null && !bar.isDisposed())
          bar.setSelection(current);
			}
		});

    GUI.getDisplay().syncExec(new Runnable()
    {
      public void run()
      {
        if (percentLabel != null && !percentLabel.isDisposed())
          percentLabel.setText(" [" + current + " %]");
      }
    });
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#setStatus(int)
   */
  public void setStatus(int status)
  {
  	this.status = status;
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#setStatusText(java.lang.String)
   */
  public void setStatusText(final String text)
  {
  	if (text == null)
  		return;

		GUI.getDisplay().syncExec(new Runnable()
		{
			public void run()
			{
        if (barLabel != null && !barLabel.isDisposed())
        {
					barLabel.setText(text);
					if (status == STATUS_DONE)
						barLabel.setForeground(Color.SUCCESS.getSWTColor());
					else if (status == STATUS_ERROR || status == STATUS_CANCEL)
						barLabel.setForeground(Color.ERROR.getSWTColor());
					else
						barLabel.setForeground(Color.WIDGET_FG.getSWTColor());
        }
			}
		});
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#log(java.lang.String)
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
  	this.parent.setLayout(new GridLayout(3,false));
  	this.parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		bar = new org.eclipse.swt.widgets.ProgressBar(this.parent, SWT.SMOOTH);
		GridData g = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		g.widthHint = 100;
		bar.setLayoutData(g);
		bar.setMaximum(100);
		bar.setSelection(0);

    percentLabel = new Label(this.parent,SWT.NONE);
    percentLabel.setBackground(Color.BACKGROUND.getSWTColor());
    GridData gd1 = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    gd1.widthHint = 80;
    percentLabel.setLayoutData(gd1);
    percentLabel.setText("[0 %]");

    barLabel = new Label(this.parent,SWT.NONE);
		barLabel.setBackground(Color.BACKGROUND.getSWTColor());
		barLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    barLabel.setText("");

		Composite comp = new Composite(this.parent,SWT.BORDER);
		comp.setBackground(Color.BACKGROUND.getSWTColor());
		GridLayout gl = new GridLayout(2,false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		comp.setLayout(gl);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		comp.setLayoutData(gd);
		
		log = new TextPart();
		log.setAutoscroll(true);
		log.setWordWrap(false);
		log.paint(comp);
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#addPercentComplete(int)
   */
  public void addPercentComplete(int percent)
  {
  	if (percent < 1)
  		return;
  	setPercentComplete(getPercentComplete() + percent);
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#getPercentComplete()
   */
  public int getPercentComplete()
  {
    return current;
  }

}


/**********************************************************************
 * $Log: ProgressBar.java,v $
 * Revision 1.11  2005/08/01 23:27:52  web0
 * *** empty log message ***
 *
 * Revision 1.10  2005/07/26 22:58:34  web0
 * @N background task refactoring
 *
 * Revision 1.9  2005/07/11 08:31:24  web0
 * *** empty log message ***
 *
 * Revision 1.8  2005/06/21 20:02:02  web0
 * @C cvs merge
 *
 * Revision 1.7  2004/11/05 20:00:44  willuhn
 * @D javadoc fixes
 *
 * Revision 1.6  2004/11/04 22:41:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/11/04 19:29:22  willuhn
 * @N TextAreaInput
 *
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