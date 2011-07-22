/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/ProgressBar.java,v $
 * $Revision: 1.22 $
 * $Date: 2011/07/22 09:11:13 $
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
import de.willuhn.util.ProgressMonitor;

/**
 * Implementierung eines Progress-Monitors, der seine Ausgaben in
 * Form eines Fortschrittsbalkens anzeigt.
 */
public class ProgressBar implements ProgressMonitor, Part
{
	private int status	= STATUS_NONE;

  private boolean showLogs    = true;
  private boolean showPercent = false;

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
   * Liefert den aktuellen Status-Code.
   * @return Status-Code.
   */
  public int getStatus()
  {
    return this.status;
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
          log(text);
					if (status == STATUS_DONE)
						barLabel.setForeground(Color.SUCCESS.getSWTColor());
					else if (status == STATUS_ERROR || status == STATUS_CANCEL)
						barLabel.setForeground(Color.ERROR.getSWTColor());
					else
						barLabel.setForeground(Color.WIDGET_FG.getSWTColor());
          
          // Einige User berichteten, dass der Text nicht rechtzeitig
          // aktualisiert wird.
          barLabel.redraw();
          barLabel.update();
        }
			}
		});
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#log(java.lang.String)
   */
  public void log(final String msg)
  {
  	if (!showLogs || msg == null)
  		return;

		GUI.getDisplay().syncExec(new Runnable()
		{
			public void run()
			{
        if (status == STATUS_ERROR)
          log.appendText(msg, Color.ERROR);
        else
          log.appendText(msg);
			}
		});
  }

	/**
   * Loescht das Log.
   */
  public void clearLog()
	{
		if (!showLogs || this.log == null)
			return;
		this.log.clear();
	}

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
  	this.parent = new Composite(parent,SWT.NONE);
  	this.parent.setLayout(new GridLayout(showLogs ? 3 : 2,false));
  	this.parent.setLayoutData(new GridData(GridData.FILL_BOTH));

  	// Wenn wir keine Logs haben, zeichnen wir den Statustext ueber
  	// dem Progress-Balken. Das sieht schoener aus. Wenn wir aber Logs
  	// haben, zeichnen wir es rechts neben den Balken, um Platz zu sparen.
  	if (showLogs)
  	{
    	{
        bar = new org.eclipse.swt.widgets.ProgressBar(this.parent, SWT.SMOOTH);
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.widthHint = showPercent ? 100 : 150;
        bar.setLayoutData(gd);
        bar.setMaximum(100);
        bar.setSelection(0);
    	}
  
  		if (showPercent)
  		{
  	    percentLabel = GUI.getStyleFactory().createLabel(this.parent,SWT.NONE);
  	    GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
  	    gd.widthHint = 50;
  	    percentLabel.setLayoutData(gd);
  	    percentLabel.setText(" [0 %]");
  		}
  		
  		{
  	    barLabel = GUI.getStyleFactory().createLabel(this.parent,SWT.NONE);
  	    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
  	    gd.horizontalSpan = showPercent ? 1 : 2;
  	    barLabel.setLayoutData(gd);
  	    barLabel.setText("");
  		}
  
      {
    		Composite comp = new Composite(this.parent,SWT.BORDER);
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
  	}
  	else
  	{
      {
        barLabel = GUI.getStyleFactory().createLabel(this.parent,SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        gd.horizontalIndent = 2;
        barLabel.setLayoutData(gd);
        barLabel.setText("");
      }

  	  {
        bar = new org.eclipse.swt.widgets.ProgressBar(this.parent, SWT.SMOOTH);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = showPercent ? 1 : 2;
        bar.setLayoutData(gd);
        bar.setMaximum(100);
        bar.setSelection(0);
      }
  
      if (showPercent)
      {
        percentLabel = GUI.getStyleFactory().createLabel(this.parent,SWT.NONE);
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.widthHint = 50;
        percentLabel.setLayoutData(gd);
        percentLabel.setText(" [0 %]");
      }
      
  	}
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
  
  /**
   * Legt fest, ob die Log-Ausgaben angezeigt werden sollen.
   * @param show true, wenn sie angezeigt werden sollen (Default).
   */
  public void showLogs(boolean show)
  {
    this.showLogs = show;
  }

  /**
   * Legt fest, ob der Fortschritt neben dem Balken auch nochmal als
   * Prozentwert angezeigt werden soll.
   * @param show true, wenn er angezeigt werden sollen (Default).
   */
  public void showPercent(boolean show)
  {
    this.showPercent = show;
  }

}


/**********************************************************************
 * $Log: ProgressBar.java,v $
 * Revision 1.22  2011/07/22 09:11:13  willuhn
 * @N Dual-Layout. Wenn keine Logs angezeigt werden, wir der Statustext jetzt ueber dem Progressbar und nicht mehr rechts daneben angezeigt - sieht schoener aus
 *
 * Revision 1.21  2011-07-22 07:49:52  willuhn
 * @C Prozent-Anzeige per Default nicht mehr anzeigen - braucht eigentlich kein Schwein - der Balken ist ja aussagekraeftig genug
 *
 * Revision 1.20  2011-05-03 10:13:10  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.19  2010-10-21 10:48:45  willuhn
 * @N BackgroundTaskDialog
 *
 * Revision 1.18  2008/04/23 11:43:40  willuhn
 * @B 432+179 Snapin nach 30 Sekunden automatisch ausblenden (jetzt einfacher via DelayedListener geloest)
 *
 * Revision 1.17  2007/05/14 11:18:09  willuhn
 * @N Hoehe der Statusleiste abhaengig von DPI-Zahl und Schriftgroesse
 * @N Default-Schrift konfigurierbar und Beruecksichtigung dieser an mehr Stellen
 *
 * Revision 1.16  2007/03/21 13:48:52  willuhn
 * @N new abstract "WaitDialog"
 * @N force redraw in backgroundtask monitor/statusbar
 *
 * Revision 1.15  2006/06/19 22:23:33  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2006/06/19 11:50:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2006/06/19 10:54:24  willuhn
 * @N neue Methode setEnabled(boolean) in Input
 * @N neue de_willuhn_util lib
 *
 * Revision 1.12  2005/08/08 17:07:38  web0
 * *** empty log message ***
 *
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