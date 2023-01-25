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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.internal.parts.LogoPart;
import de.willuhn.jameica.gui.internal.parts.PanelButtonBack;
import de.willuhn.jameica.gui.internal.parts.SearchPart;
import de.willuhn.jameica.gui.parts.NotificationPanel;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.gui.parts.TitlePart;
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
	private SearchPart searchPart;
	private NotificationPanel notifications;
  
  private String title;
  
  private LogoPart logoPart;
  private TitlePart titlePart;

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    this.parent = parent;
    init();
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

    if (!Customizing.SETTINGS.getBoolean("application.view.hidelogo",true))
    {
      Composite comp = new Composite(view,SWT.NONE);
      comp.setLayout(SWTUtil.createGrid(1,false));
      comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      this.logoPart = new LogoPart();
      this.logoPart.paint(comp);
    }

    if (!Customizing.SETTINGS.getBoolean("application.view.hidepanel",false))
    {
      Composite comp = new Composite(view,SWT.NONE);
      comp.setLayout(SWTUtil.createGrid(1,false));
      comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      this.titlePart = new TitlePart(title,false);
      this.titlePart.paint(comp);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
	  Composite comp = new Composite(view,SWT.NONE);
	  comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final GridLayout l = new GridLayout(2,false);
    l.marginLeft = 3;
    l.marginRight = 3;
	  comp.setLayout(l);

    if (!Customizing.SETTINGS.getBoolean("application.view.hidemessages",false))
    {
      try
      {
        notifications = new NotificationPanel();
        notifications.setBackground(true);
        notifications.setBorder(1);
        notifications.setReceiveMessages(true);
        notifications.paint(comp);
      }
      catch (Exception e)
      {
        Logger.error("unable to paint notification panel",e);
      }
    }

    if (!Customizing.SETTINGS.getBoolean("application.view.hidesearch",false))
    {
      this.searchPart = new SearchPart();
      try
      {
        Composite comp2 = new Composite(comp,SWT.NONE);
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gd.widthHint = 300;
        comp2.setLayoutData(gd);
        comp2.setLayout(SWTUtil.createGrid(1,false));
        this.searchPart.paint(comp2);
      }
      catch (Exception e)
      {
        Logger.error("unable to draw search part");
      }
    }

    ////////////////////////////////////////////////////////////////////////////
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
		
		if (notifications != null)
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
   * @deprecated Ersatzlos gestrichen. Verwende eine StatusBarMessage mit dem Typ "TYPE_INFO".
   */
  public void setLogoText(final String text)
  {
    Application.getMessagingFactory().sendMessage(new StatusBarMessage(text,StatusBarMessage.TYPE_INFO));
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
	 * Liefert die Suchleiste.
	 * @return die Suchleiste.
	 */
	public SearchPart getSearchPart()
	{
	  return this.searchPart;
	}
	
	/**
	 * Liefert den Logo-Part.
	 * @return der Logo-Part.
	 */
	public LogoPart getLogoPart()
	{
	  return this.logoPart;
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
