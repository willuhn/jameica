/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/controller/Attic/SettingsControl.java,v $
 * $Revision: 1.19 $
 * $Date: 2004/05/27 23:38:25 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.controller;

import java.util.Hashtable;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.ColorInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.StyleFactory;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.gui.views.Settings;
import de.willuhn.jameica.gui.views.Start;
import de.willuhn.util.I18N;

/**
 * 
 */
public class SettingsControl extends AbstractControl
{

	private I18N i18n;
	private AbstractInput colorWhite;
	private AbstractInput colorComment;
	private AbstractInput colorBackground;
	private AbstractInput colorForeground;
	private AbstractInput colorBorder;
	private AbstractInput colorError;
	private AbstractInput colorSuccess;
	private AbstractInput colorLink;
	private AbstractInput colorLinkActive;
	
	private AbstractInput styleFactory;
	
  /**
   * ct.
   * @param view
   */
  public SettingsControl(AbstractView view)
  {
    super(view);
    i18n = Application.getI18n();
  }

	/**
	 * Liefert ein Auswahl-Feld fuer die Style-Factory.
   * @return
   */
  public AbstractInput getStyleFactory()
	{
		if (styleFactory != null)
			return styleFactory;
		try {
			Class[] styles = Application.getClassLoader().getClassFinder().findImplementors(StyleFactory.class);
			Hashtable ht = new Hashtable();
			for (int i=0;i<styles.length;++i)
			{
				StyleFactory f = (StyleFactory) styles[i].newInstance();
				ht.put(f.getName(),f);
			}
			styleFactory = new SelectInput(ht,GUI.getStyleFactory().getName());
		}
		catch (Exception e)
		{
			Application.getLog().error("unable to load available stylefactories",e);
			styleFactory = new LabelInput(i18n.tr("Fehler beim Laden der Styles"));
		}
		return styleFactory;
	}

	/**
	 * Auswahlfeld.
   * @return
   */
  public AbstractInput getColorWhite()
	{
		if (colorWhite != null)
			return colorWhite;
		colorWhite = new ColorInput(Color.WIDGET_BG.getSWTColor());
		return colorWhite;
	}

	/**
	 * Auswahlfeld.
	 * @return
	 */
	public AbstractInput getColorComment()
	{
		if (colorComment != null)
			return colorComment;
		colorComment = new ColorInput(Color.COMMENT.getSWTColor());
		return colorComment;
	}

	/**
	 * Auswahlfeld.
	 * @return
	 */
	public AbstractInput getColorBackground()
	{
		if (colorBackground != null)
			return colorBackground;
		colorBackground = new ColorInput(Color.BACKGROUND.getSWTColor());
		return colorBackground;
	}

	/**
	 * Auswahlfeld.
	 * @return
	 */
	public AbstractInput getColorForeground()
	{
		if (colorForeground != null)
			return colorForeground;
		colorForeground = new ColorInput(Color.FOREGROUND.getSWTColor());
		return colorForeground;
	}

	/**
	 * Auswahlfeld.
	 * @return
	 */
	public AbstractInput getColorBorder()
	{
		if (colorBorder != null)
			return colorBorder;
		colorBorder = new ColorInput(Color.BORDER.getSWTColor());
		return colorBorder;
	}

	/**
	 * Auswahlfeld.
	 * @return
	 */
	public AbstractInput getColorError()
	{
		if (colorError != null)
			return colorError;
		colorError = new ColorInput(Color.ERROR.getSWTColor());
		return colorError;
	}

	/**
	 * Auswahlfeld.
	 * @return
	 */
	public AbstractInput getColorSuccess()
	{
		if (colorSuccess != null)
			return colorSuccess;
		colorSuccess = new ColorInput(Color.SUCCESS.getSWTColor());
		return colorSuccess;
	}

	/**
	 * Auswahlfeld.
	 * @return
	 */
	public AbstractInput getColorLink()
	{
		if (colorLink != null)
			return colorLink;
		colorLink = new ColorInput(Color.LINK.getSWTColor());
		return colorLink;
	}

	/**
	 * Auswahlfeld.
	 * @return
	 */
	public AbstractInput getColorLinkActive()
	{
		if (colorLinkActive != null)
			return colorLinkActive;
		colorLinkActive = new ColorInput(Color.LINK_ACTIVE.getSWTColor());
		return colorLinkActive;
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleDelete()
   */
  public void handleDelete()
  {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
   */
  public void handleCancel()
  {
  	GUI.startView(Start.class.getName(),null);
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public void handleStore()
  {

  	try
    {
    	Color.WIDGET_BG.setSWTColor((org.eclipse.swt.graphics.Color)getColorWhite().getValue());
			Color.COMMENT.setSWTColor((org.eclipse.swt.graphics.Color)getColorComment().getValue());
			Color.BACKGROUND.setSWTColor((org.eclipse.swt.graphics.Color)getColorBackground().getValue());

			Color.FOREGROUND.setSWTColor((org.eclipse.swt.graphics.Color)getColorForeground().getValue());
			Color.BORDER.setSWTColor((org.eclipse.swt.graphics.Color)getColorBorder().getValue());

			Color.ERROR.setSWTColor((org.eclipse.swt.graphics.Color)getColorError().getValue());
			Color.SUCCESS.setSWTColor((org.eclipse.swt.graphics.Color)getColorSuccess().getValue());

			Color.LINK.setSWTColor((org.eclipse.swt.graphics.Color)getColorLink().getValue());
			Color.LINK_ACTIVE.setSWTColor((org.eclipse.swt.graphics.Color)getColorLinkActive().getValue());

			GUI.getStatusBar().setSuccessText(i18n.tr("Einstellungen gespeichert."));
			
			SimpleDialog d = new SimpleDialog(SimpleDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Neustart erforderlich"));
			d.setText(i18n.tr("Bitte starten Sie Jameica neu, damit Ihre Änderungen wirksam werden."));
			d.open();
    }
    catch (Exception e)
    {
    	Application.getLog().error("error while writing config",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Einstellungen."));
    }
  	
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate()
  {
  }
  
  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
   */
  public void handleOpen(Object o)
  {
  }
  
  /**
   * Setzt die Einstellungen zurueck.
   */
  public void handleRestore()
  {
  	try {
  		YesNoDialog prompt = new YesNoDialog(YesNoDialog.POSITION_CENTER);
  		prompt.setTitle(i18n.tr("Sicher?"));
  		prompt.setText(i18n.tr("Alle Einstellungen werden auf die Standard-Werte zurückgesetzt"));
  		if (!((Boolean) prompt.open()).booleanValue())
  			return;

			Color.WIDGET_BG.reset();
			Color.COMMENT.reset();
			Color.BACKGROUND.reset();
			Color.FOREGROUND.reset();
			Color.BORDER.reset();
			Color.ERROR.reset();
			Color.SUCCESS.reset();
			Color.LINK.reset();
			Color.LINK_ACTIVE.reset();

			GUI.startView(Settings.class.getName(),null);
			GUI.getStatusBar().setSuccessText(i18n.tr("Einstellungen zurückgesetzt."));
  	}
  	catch (Exception e)
  	{
  		Application.getLog().error("error while restoring settings",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Zurücksetzen"));
  	}
  	
  }

}


/**********************************************************************
 * $Log: SettingsControl.java,v $
 * Revision 1.19  2004/05/27 23:38:25  willuhn
 * @B deadlock in swt event queue while startGUITimeout
 *
 * Revision 1.18  2004/05/23 18:15:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/05/23 16:34:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/04/19 22:05:27  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/04/12 19:16:00  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.14  2004/04/01 00:23:24  willuhn
 * @N FontInput
 * @N ColorInput
 * @C improved ClassLoader
 * @N Tabs in Settings
 *
 * Revision 1.13  2004/03/30 22:08:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/03/24 00:46:03  willuhn
 * @C refactoring
 *
 * Revision 1.11  2004/03/11 08:56:56  willuhn
 * @C some refactoring
 *
 * Revision 1.10  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.9  2004/03/03 22:27:11  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.8  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.7  2004/02/11 00:10:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/01/28 20:51:25  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.5  2004/01/23 00:29:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/01/08 20:50:33  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.3  2004/01/06 20:11:22  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/01/06 01:27:30  willuhn
 * @N table order
 *
 * Revision 1.1  2004/01/04 19:51:01  willuhn
 * *** empty log message ***
 *
 **********************************************************************/