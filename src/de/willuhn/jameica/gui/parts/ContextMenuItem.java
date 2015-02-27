/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/ContextMenuItem.java,v $
 * $Revision: 1.4 $
 * $Date: 2008/12/19 01:12:06 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;

import org.eclipse.swt.graphics.Image;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Bildet ein Element eines Kontext-Menus ab.
 */
public class ContextMenuItem
{
	private boolean separator = false;
	private String text       = null;
	private Action action     = null;
	private Image image       = null;
	private String shortcut   = null;

	/**
	 * Menu-Item, welches als Separator verwendet werden kann.
	 */
	public final static ContextMenuItem SEPARATOR = new ContextMenuItem();
	
	static
	{
		SEPARATOR.setSeparator(true);
	}

	/**
   * ct.
   */
  public ContextMenuItem()
	{
    this(null,null);
	}

	/**
	 * ct.
   * @param text Anzuzeigender Text.
   * @param a Action, die beim Klick ausgefuehrt werden soll.
   */
  public ContextMenuItem(String text, Action a)
	{
    this(text,a,null);
	}

  /**
   * ct.
   * @param text Anzuzeigender Text.
   * @param a Action, die beim Klick ausgefuehrt werden soll.
   * @param icon optionale Angabe eines Icons.
   */
  public ContextMenuItem(String text, Action a, String icon)
  {
    this.text = text;
    this.action = a;
    if (icon != null)
    {
      try
      {
        this.setImage(SWTUtil.getImage(icon));
      }
      catch (Exception e)
      {
        Logger.warn("icon " + icon + " not found");
      }
    }
  }

  /**
	 * Legt fest, ob das Menu ein Separator sein soll.
   * @param separator true, wenn es ein Separator sein soll.
   */
  public void setSeparator(boolean separator)
	{
		this.separator = separator;
	}

	/**
	 * Prueft, ob das Element ein Separator ist.
   * @return true, wenn es ein Separator ist.
   */
  public boolean isSeparator()
	{
		return separator;
	}
	

  /**
   * Ein optionales Image.
   * @return Image.
   */
  public Image getImage()
  {
    return image;
  }

	/**
	 * Speichert das anzuzeigende Image.
	 * @param image Image.
	 */
	public void setImage(Image image)
	{
		this.image = image;
	}

  /**
   * Liefert die auszufuehrende Aktion.
   * @return auszufuehrende Aktion.
   */
  public Action getAction()
  {
    return action;
  }

  /**
   * Liefert den anzuzeigenden Text.
   * @return anzuzeigender Text.
   */
  public String getText()
  {
    return text;
  }

	/**
	 * Speichert den anzuzeigenden Text.
	 * @param string
	 */
	public void setText(String string)
	{
		text = string;
	}

  /**
   * Speichert die auszufuehrende Action. 
   * @param a Action.
   */
  public void setAction(Action a)
  {
    this.action = a;
  }
  
  /**
   * Liefert eine optionale Tastenkombi fuer den Short-Cut.
   * @return Tastenkombi.
   */
  public String getShortcut()
  {
    return this.shortcut;
  }
  
  /**
   * Speichert eine optionale Tastenkombi fuer den Short-Cut.
   * @param shortcut
   */
  public void setShortcut(String shortcut)
  {
    this.shortcut = Application.getPlatform().mapSWTShortcut(shortcut);
  }



	/**
	 * Diese Funktion wird fuer jedes MenuItem aufgerufen, wenn sich das Kontext-Menu oeffnet.
	 * Folglich kann hier jedes MenuItem selbst bestimmen, ob es fuer das aktuelle
	 * Objekt verfuegbar sein soll oder nicht.
	 * Diese Default-Implementierung liefert immer <code>true</code>.
	 * Soll das Item also bei Bedarf deaktiviert werden, muss von dieser
	 * Klasse abgeleitet und diese Methode ueberschrieben werden.
	 * Alternativ kann auch bereits die abgeleitete Klasse CheckedContextMenuItem
	 * verwendet werden, die nur dann <code>true</code> liefert, wenn das Objekt nicht
	 * <code>null</code> ist. Sprich: Wenn nicht in leeren Raum geklickt wurde sondern
	 * auf ein Objekt.
   * @param o das zu testende Objekt.
   * @return true, wenn das Item angezeigt werden soll. Andernfalls <code>false</code>.
   */
  public boolean isEnabledFor(Object o)
	{
		return true;
	}
}
