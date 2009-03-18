/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/View.java,v $
 * $Revision: 1.44 $
 * $Date: 2009/03/18 22:40:27 $
 * $Author: willuhn $
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
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
	private CLabel messages;
  
  private Canvas logoBg;
  private Canvas panelBg;
	

  private String title;
  private String logotext;

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
		sash.setLayout(SWTUtil.createGrid(1,true));
		
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
		snapin.setLayout(SWTUtil.createGrid(1,true));
		sash.setMaximizedControl(view);

    ////////////////////////////////////////////////////////////////////////////
    //
    final Image logo = SWTUtil.getImage("panel.bmp");
    final Rectangle imageSize = logo.getBounds();
		logoBg = SWTUtil.getCanvas(view,logo, SWT.TOP | SWT.RIGHT);
    logoBg.setBackground(new org.eclipse.swt.graphics.Color(GUI.getDisplay(),255,255,255));
    GridLayout layout1 = new GridLayout();
    layout1.marginHeight = 0;
    layout1.marginWidth = 0;
    layout1.horizontalSpacing = 0;
    layout1.verticalSpacing = 0;
    logoBg.setLayout(layout1);

    logoBg.addListener(SWT.Paint, new Listener()
    {
      public void handleEvent(Event event)
      {
        GC gc = event.gc;
        Rectangle size = logoBg.getBounds();
        gc.setBackground(new org.eclipse.swt.graphics.Color(GUI.getDisplay(),255,255,255));
        gc.fillRectangle(size);
        gc.drawImage(logo,size.width - imageSize.width,0);
        gc.setFont(Font.SMALL.getSWTFont());
        gc.setForeground(Color.COMMENT.getSWTColor());
        gc.drawText(logotext == null ? "" : logotext,8,14,true);
      }
    });
    ////////////////////////////////////////////////////////////////////////////


    Label sep = new Label(view,SWT.SEPARATOR | SWT.HORIZONTAL);
    sep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    ////////////////////////////////////////////////////////////////////////////
    //
    panelBg = SWTUtil.getCanvas(view,SWTUtil.getImage("panel-reverse.gif"), SWT.TOP | SWT.RIGHT);
    GridLayout layout2 = new GridLayout();
    layout2.marginHeight = 0;
    layout2.marginWidth = 0;
    layout2.horizontalSpacing = 0;
    layout2.verticalSpacing = 0;
    panelBg.setLayout(layout2);
    panelBg.setBackground(Color.BACKGROUND.getSWTColor());

    panelBg.addListener(SWT.Paint,new Listener()
    {
      public void handleEvent(Event event)
      {
        GC gc = event.gc;
        gc.setFont(Font.H2.getSWTFont());
        gc.drawText(title == null ? "" : title,8,1,true);
      }
    });
    ////////////////////////////////////////////////////////////////////////////

		Label sep2 = new Label(view,SWT.SEPARATOR | SWT.HORIZONTAL);
		sep2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		messages = new CLabel(view,SWT.NONE);
		messages.setFont(Font.H2.getSWTFont());
    messages.setBackground(new org.eclipse.swt.graphics.Color(GUI.getDisplay(),255,255,255));
		messages.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Label sep3 = new Label(view,SWT.SEPARATOR | SWT.HORIZONTAL);
    sep3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	}
	
  /**
   * Leert die Anzeige.
   * Wird beim Wechsel von einem Dialog auf den naechsten aufgerufen.
   */
  protected void cleanContent()
	{
		if (content != null && !content.isDisposed())
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
        int[] weights = new int[] {GUI.SETTINGS.getInt("snapin.height.0",3),GUI.SETTINGS.getInt("snapin.height.1",1)};
        if (weights[0] <= 0) weights[0] = 3;
        if (weights[1] <= 0) weights[1] = 1;
        sash.setWeights(weights);
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
        try
        {
          // BUGZILLA 432 - Speichern der letzten Hoehe des Snapins
          int[] weights = sash.getWeights();
          GUI.SETTINGS.setAttribute("snapin.height.0",weights[0]);
          GUI.SETTINGS.setAttribute("snapin.height.1",weights[1]);
          SWTUtil.disposeChildren(snapin);
          sash.setMaximizedControl(view);
        }
        finally
        {
          snappedIn = false;
        }
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
	 * Hinweis: Das Composite enthaelt ein GridLayout.
   * @return Snapin-Composite.
   */
  public Composite getSnapin()
	{
		return snapin;
	}

	/**
	 * Aktualisiert den Titel der View.
   * @param text anzuzeigender Titel.
   */
  public void setTitle(String text)
	{
    this.title = text == null ? "" : text;
    if (this.panelBg != null && !this.panelBg.isDisposed())
    {
      GUI.getDisplay().syncExec(new Runnable() {
        public void run()
        {
          panelBg.redraw();
        }
      });
    }
	}

  /**
   * Aktualisiert den Text neben dem Logo.
   * Normalerweise steht da nichts. Man kann
   * aber was hinschreiben.
   * @param text der text links neben dem Logo.
   */
  public void setLogoText(String text)
  {
    this.logotext = text == null ? "" : text;
    if (this.logoBg != null && !this.logoBg.isDisposed())
    {
      GUI.getDisplay().asyncExec(new Runnable() {
        public void run()
        {
          logoBg.redraw();
        }
      });
    }
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
        messages.setText(text == null ? "" : " " + text);
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
 * Revision 1.44  2009/03/18 22:40:27  willuhn
 * *** empty log message ***
 *
 * Revision 1.43  2009/02/27 14:05:34  willuhn
 * @B BUGZILLA 432 - das Speichern der Verhaeltnisse geht ja viel einfacher ;) Ich muss doch gar nicht selbst ausrechnen, wie die prozentuale Verteilung ist sondern kann einfach die SWT-Komponente fragen. Dann muss ich gar nichts rechnen
 *
 * Revision 1.42  2008/04/23 11:10:24  willuhn
 * @N Bug 432 Snapin merkt sich jetzt seine letzte Hoehe und stellt diese wieder her
 *
 * Revision 1.41  2007/12/18 23:05:42  willuhn
 * @C Farben wieder explizit vorgegeben. Unter Windows XP sieht es so oder so (ob Expand-Bar mit oder ohne XP-Lookp) haesslich aus
 *
 * Revision 1.40  2007/12/18 17:50:12  willuhn
 * @R Background-Color nicht mehr aenderbar
 * @C Layout der Startseite
 *
 * Revision 1.39  2007/04/26 17:33:25  willuhn
 * @C Logo-Text mit asyncExec setzen
 *
 * Revision 1.38  2007/01/25 12:07:58  willuhn
 * @R removed debug output
 * @C dispose snapin content on snapout()
 *
 * Revision 1.37  2006/12/28 15:35:52  willuhn
 * @N Farbige Pflichtfelder
 *
 * Revision 1.36  2006/06/20 23:26:51  willuhn
 * @N View#setLogoText
 *
 * Revision 1.35  2006/04/20 08:44:03  web0
 * @C s/Childs/Children/
 *
 * Revision 1.34  2005/08/15 13:15:32  web0
 * @C fillLayout removed
 *
 * Revision 1.33  2005/08/12 16:24:19  web0
 * @B paint bug when using gtk-qt-engine. Untested!
 *
 * Revision 1.32  2005/06/13 23:18:18  web0
 * *** empty log message ***
 *
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