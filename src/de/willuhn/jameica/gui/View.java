/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/View.java,v $
 * $Revision: 1.31 $
 * $Date: 2005/06/13 22:05:32 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 ****************************************************************************/
package de.willuhn.jameica.gui;



import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;

/**
 * Bildet das Content-Frame ab.
 * @author willuhn
 */
public class View implements Part
{
	private SashForm sash;

	private Composite view;
	private Composite content;
	private Composite snapin;
	private boolean snappedIn = false;

	private Composite parent;
	private String title;
	private CLabel messages;
	

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    this.parent = parent;
    init();
    cleanContent();
  }

  /**
   * Initialisiert das Layout der View.
   */
  private void init()
	{
		sash = new SashForm(parent,SWT.VERTICAL);
    sash.setLayoutData(new GridData(GridData.FILL_BOTH));
		sash.setLayout(new FillLayout());
		
		view = new Composite(sash, SWT.BORDER);
		view.setBackground(Color.BACKGROUND.getSWTColor());
		view.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		view.setLayout(layout);
	
		snapin = new Composite(sash, SWT.BORDER);
		snapin.setLayoutData(new GridData(GridData.FILL_BOTH));
		snapin.setLayout(new FillLayout());
		sash.setMaximizedControl(view);

		Canvas c = SWTUtil.getCanvas(view,SWTUtil.getImage("panel.bmp"), SWT.TOP | SWT.RIGHT);
		c.setBackground(new org.eclipse.swt.graphics.Color(GUI.getDisplay(),255,255,255));

    Label sep = new Label(view,SWT.SEPARATOR | SWT.HORIZONTAL);
    sep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Canvas panelBg = SWTUtil.getCanvas(view,SWTUtil.getImage("panel-reverse.gif"), SWT.TOP | SWT.RIGHT);
    GridLayout layout2 = new GridLayout();
    layout2.marginHeight = 0;
    layout2.marginWidth = 0;
    layout2.horizontalSpacing = 0;
    layout2.verticalSpacing = 0;
    panelBg.setLayout(layout2);

    panelBg.addListener(SWT.Paint,new Listener()
    {
      public void handleEvent(Event event)
      {
        GC gc = event.gc;
        gc.setFont(Font.H1.getSWTFont());
        gc.drawText(title == null ? "" : title,8,1,true);
      }
    });

		Label sep2 = new Label(view,SWT.SEPARATOR | SWT.HORIZONTAL);
		sep2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		messages = new CLabel(view,SWT.NONE);
		messages.setFont(Font.H2.getSWTFont());
		messages.setBackground(Color.BACKGROUND.getSWTColor());
		messages.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	}
	
  /**
   * Leert die Anzeige.
   * Wird beim Wechsel von einem Dialog auf den naechsten aufgerufen.
   */
  protected void cleanContent()
	{
		if (content != null)
			content.dispose();

		// Wir machen hier deshalb nicht nur ein layout() oder redraw()
		// weil wir wollen, dass die gesamte View disposed und entfernt
		// wird, bevor eine neue drauf kommt.
		content = new Composite(view, SWT.NONE);
		content.setBackground(Color.BACKGROUND.getSWTColor());
		content.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout l = new GridLayout();
		l.marginHeight = 6;
		l.marginWidth = 6;
		content.setLayout(l);
		messages.setText("");
		messages.layout();
	}

	/**
   * Das Snapin-Composite wird angezeigt.
   */
  public void snapIn()
	{
		GUI.startSync(new Runnable()
    {
      public void run()
      {
				sash.setMaximizedControl(null);
				sash.setWeights(new int[] {3,1});
				snappedIn = true;
      }
    });
	}

	/**
   * Das Snapin-Composite wird ausgeblendet.
   */
  public void snapOut()
	{
		GUI.startSync(new Runnable()
    {
      public void run()
      {
				sash.setMaximizedControl(view);
				snappedIn = false;
      }
    });
	}

	/**
	 * Prueft, ob das Snapin gerade angezeigt wird.
   * @return true, wenn es angezeigt wird.
   */
  public boolean snappedIn()
	{
		return snappedIn;
	}

	/**
	 * Liefert das SnapIn-Composite.
	 * Die Funktion liefert immer ein leeres Snapin. Wenn sich also vorher
	 * was drin befunden hat, wird es vorm erneuten Herausgeben geleert.
	 * Hinweis: Das Composite enthaelt ein FillLayout.
   * @return Snapin-Composite.
   */
  public Composite getSnapin()
	{
		SWTUtil.disposeChilds(snapin);
		return snapin;
	}

	/**
	 * Aktualisiert den Titel der View.
   * @param text anzuzeigender Titel.
   */
  public void setTitle(String text)
	{
    this.title = text == null ? "" : text;
	}

	/**
	 * Schreibt einen Fehlertext oben in die View.
   * @param text anzuzeigender Text. 
   */
  public void setErrorText(final String text)
	{
    setStatusText(text,Color.ERROR);
	}

  /**
   * Private Hilfs-Funktion, die den Text anzeigt.
   * @param text anzuzeigender Text.
   * @param color Farbe.
   */
  private void setStatusText(final String text, final Color color)
  {
		final long currentClick = System.currentTimeMillis();

    GUI.getDisplay().asyncExec(new Runnable() {
      public void run() {
        messages.setText(text);
        messages.setForeground(color.getSWTColor());
				lastClick = currentClick;
      }
    });
    GUI.getDisplay().timerExec(10000,new Runnable()
    {
      public void run()
      {
				if (currentClick == lastClick && !messages.isDisposed()) // nur entfernen, wenn wir der letzte Klick waren
					messages.setText("");
      }
    });
  }
	private long lastClick;

	/**
	 * Schreibt einen Erfolgstext oben in die View.
	 * @param text anzuzeigender Text. 
	 */
	public void setSuccessText(final String text)
	{
    setStatusText(text,Color.SUCCESS);
	}

  /**
   * Aktualisiert die Anzeige.
   */
  protected void refreshContent()
	{
		view.layout();
	}

  /**
   * Liefert das Composite, in das die anzuzeigenden Dialoge bitte ihre Controls reinmalen sollen.
   * @return Composite, in das der Dialog seine Elemente reinmalen soll.
   */
  protected Composite getContent()
	{
		return content;
	}
}



/***************************************************************************
 * $Log: View.java,v $
 * Revision 1.31  2005/06/13 22:05:32  web0
 * *** empty log message ***
 *
 * Revision 1.30  2005/06/13 11:48:41  web0
 * *** empty log message ***
 *
 * Revision 1.29  2005/06/13 11:47:25  web0
 * *** empty log message ***
 *
 * Revision 1.28  2005/06/03 17:14:41  web0
 * @N Livelog
 *
 * Revision 1.27  2004/11/17 19:02:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.26  2004/11/10 16:19:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.25  2004/11/10 15:53:23  willuhn
 * @N Panel
 *
 * Revision 1.24  2004/10/24 17:19:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2004/10/08 00:19:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2004/08/27 19:11:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.21  2004/08/27 17:46:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.20  2004/08/26 23:19:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2004/05/26 23:23:23  willuhn
 * @N Timeout fuer Messages in Statusbars
 *
 * Revision 1.18  2004/05/23 16:34:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.16  2004/04/29 23:05:54  willuhn
 * @N new snapin feature
 *
 * Revision 1.15  2004/03/24 00:46:03  willuhn
 * @C refactoring
 *
 * Revision 1.14  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.13  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.12  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 * Revision 1.11  2004/02/18 01:40:30  willuhn
 * @N new white style
 *
 * Revision 1.10  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.9  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.7  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.5  2003/12/05 18:43:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2003/11/20 03:48:41  willuhn
 * @N first dialogues
 *
 * Revision 1.3  2003/11/13 00:37:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 ***************************************************************************/