/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/View.java,v $
 * $Revision: 1.52 $
 * $Date: 2011/08/18 16:03:38 $
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
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.internal.parts.PanelButtonBack;
import de.willuhn.jameica.gui.parts.NotificationPanel;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.gui.parts.TitlePart;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Customizing;
import de.willuhn.logging.Logger;

/**
 * Bildet das Content-Frame ab.
 * @author willuhn
 */
public class View implements Part
{
	private SashForm sash;

	private Composite view;
	private ScrolledComposite scroll;
	private Composite content;
	private Composite snapin;
	private boolean snappedIn    = false;


	private Composite parent;
	private NotificationPanel notifications;
  
  private Canvas logoBg;

  private String title;
  private String logotext;
  
  private TitlePart titlePart;

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
    view.setLayoutData(new GridData(GridData.FILL_BOTH));
		view.setLayout(SWTUtil.createGrid(1,true));


		snapin = new Composite(sash, SWT.BORDER);
		snapin.setLayoutData(new GridData(GridData.FILL_BOTH));
		snapin.setLayout(SWTUtil.createGrid(1,true));
		sash.setMaximizedControl(view);

		if (!Customizing.SETTINGS.getBoolean("application.view.hidelogo",false))
		{
	    ////////////////////////////////////////////////////////////////////////////
	    //
	    final Image logo = SWTUtil.getImage(Customizing.SETTINGS.getString("application.view.logo","panel.png"));
	    final Rectangle imageSize = logo.getBounds();
	    logoBg = SWTUtil.getCanvas(view,logo, SWT.TOP | SWT.RIGHT);
	    RGB bg = Customizing.SETTINGS.getRGB("application.view.background",GUI.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB());
	    logoBg.setBackground(new org.eclipse.swt.graphics.Color(GUI.getDisplay(),bg));
	    logoBg.setLayout(SWTUtil.createGrid(1,false));

	    logoBg.addListener(SWT.Paint, new Listener()
	    {
	      public void handleEvent(Event event)
	      {
	        GC gc = event.gc;
	        Rectangle size = logoBg.getBounds();
	        gc.fillRectangle(size);
	        gc.drawImage(logo,size.width - imageSize.width,0);
	        gc.setFont(Font.SMALL.getSWTFont());
	        
          // kein Hintergrund hinter dem Text malen
	        // Ist zumindest unter Linux nicht noetig. Windows und OSX muesste man mal noch testen
	        gc.setBackground(GUI.getDisplay().getSystemColor(SWT.TRANSPARENT));
	        gc.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
	        gc.setAlpha(150);
	        gc.drawText(logotext == null ? "" : logotext,8,10,true);
	      }
	    });

      Label sep = new Label(view,SWT.SEPARATOR | SWT.HORIZONTAL);
      sep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      ////////////////////////////////////////////////////////////////////////////
		}

    if (!Customizing.SETTINGS.getBoolean("application.view.hidepanel",false))
    {
      ////////////////////////////////////////////////////////////////////////////
      //
      Composite comp = new Composite(view,SWT.NONE);
      comp.setLayout(SWTUtil.createGrid(1,false));
      comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      this.titlePart = new TitlePart(title,false);
      this.titlePart.addButton(new PanelButtonBack()); // Zurueckbutton ist immer dabei
      this.titlePart.paint(comp);
    }

    if (!Customizing.SETTINGS.getBoolean("application.view.hidemessages",false))
    {
      ////////////////////////////////////////////////////////////////////////////
      //
      try
      {
        notifications = new NotificationPanel();
        notifications.paint(view);
      }
      catch (Exception e)
      {
        Logger.error("unable to paint notification panel",e);
      }
      ////////////////////////////////////////////////////////////////////////////
    }
	}
	
  /**
   * Leert die Anzeige.
   * Wird beim Wechsel von einem Dialog auf den naechsten aufgerufen.
   */
  protected void cleanContent()
	{
		if (content != null && !content.isDisposed())
			content.dispose();
		
		if (scroll != null && !scroll.isDisposed())
		  scroll.dispose();
		

		if (Customizing.SETTINGS.getBoolean("application.scrollview",false))
		{
      scroll = new ScrolledComposite(view,SWT.V_SCROLL | SWT.H_SCROLL);
      scroll.setLayoutData(new GridData(GridData.FILL_BOTH));
      scroll.setLayout(SWTUtil.createGrid(1,true));
      scroll.setExpandHorizontal(true);
      scroll.setExpandVertical(true);
      scroll.setMinHeight(Customizing.SETTINGS.getInt("application.scrollview.minheight",580));

      content = new Composite(scroll, SWT.NONE);
      scroll.setContent(content);
		}
		else
		{
	    content = new Composite(view, SWT.NONE);
		}

		content.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout l = new GridLayout();
		l.marginHeight = 6;
		l.marginWidth = 6;
		content.setLayout(l);

		if (this.titlePart != null)
		{
		  this.titlePart.clearButtons();
		  this.titlePart.addButton(new PanelButtonBack()); // Zurueckbutton ist immer dabei
		}
		
    notifications.reset();
    setTitle(null);
	}
  
  /**
   * Fuegt einen Panel-Button hinzu.
   * @param b der Panel-Button.
   */
  public void addPanelButton(PanelButton b)
  {
    if (this.titlePart != null)
      this.titlePart.addButton(b);
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
    this.title = text;
    if (this.titlePart != null)
      this.titlePart.setTitle(this.title);
	}
  
  /**
   * Liefert den aktuellen Titel der View.
   * @return der aktuelle Titel der View.
   */
  public String getTitle()
  {
    return this.title;
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
    Application.getMessagingFactory().sendMessage(new StatusBarMessage(text,StatusBarMessage.TYPE_ERROR));
	}

	/**
	 * Schreibt einen Erfolgstext oben in die View.
	 * @param text anzuzeigender Text. 
	 */
	public void setSuccessText(final String text)
	{
    Application.getMessagingFactory().sendMessage(new StatusBarMessage(text,StatusBarMessage.TYPE_SUCCESS));
	}
	
	/**
	 * Liefert das Notification-Panel.
	 * @return das Notification-Panel.
	 */
	public NotificationPanel getNotificationPanel()
	{
	  return this.notifications;
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
 * Revision 1.52  2011/08/18 16:03:38  willuhn
 * @N BUGZILLA 286 - Panel-Code komplett refactored und in eine gemeinsame neue Klasse "TitlePart" verschoben. Damit muss der Code (incl. Skalieren der Panel) nur noch an einer Stelle gewartet werden. Und wir haben automatisch Panelbutton-Support an allen Stellen - nicht nur in der View, sondern jetzt auch im Snapin, in der Navi und sogar in Dialogen ;)
 *
 * Revision 1.51  2011-05-03 10:13:11  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.50  2011-04-06 16:13:16  willuhn
 * @N BUGZILLA 631
 *
 * Revision 1.49  2010-09-28 23:42:52  willuhn
 * @N Panel-Grafik customizable
 *
 * Revision 1.48  2009/12/16 00:11:59  willuhn
 * @N Scroll-Support fuer Views - nochmal ueberarbeitet und jetzt via Customizing konfigurierbar
 *
 * Revision 1.47  2009/10/12 08:55:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.46  2009/06/04 10:36:01  willuhn
 * @N Customizing-Parameter zum Ausblenden von Logo-Bar, Message-Bar und Title-Panel
 *
 * Revision 1.45  2009/03/20 16:38:09  willuhn
 * @N BUGZILLA 576
 ***************************************************************************/