/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
	public static final ContextMenuItem SEPARATOR = new ContextMenuItem();
	
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
   * @param separator {@code true}, wenn es ein Separator sein soll.
   */
  public void setSeparator(boolean separator)
	{
		this.separator = separator;
	}

	/**
	 * Prueft, ob das Element ein Separator ist.
   * @return {@code true}, wenn es ein Separator ist.
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
   * Liefert eine optionale Tastenkombination fuer den Short-Cut.
   * @return Tastenkombination.
   */
  public String getShortcut()
  {
    return this.shortcut;
  }
  
  /**
   * Speichert eine optionale Tastenkombination fuer den Short-Cut.
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
	 *
	 * <p>Diese Default-Implementierung liefert immer {@code true}.
	 * Soll das Item also bei Bedarf deaktiviert werden, muss von dieser
	 * Klasse abgeleitet und diese Methode ueberschrieben werden.
	 *
	 * <p>Alternativ kann auch bereits die abgeleitete Klasse {@link CheckedContextMenuItem}
	 * verwendet werden, die nur dann {@code true} liefert, wenn das Objekt nicht
	 * {@code null} ist. Sprich: Wenn nicht in leeren Raum geklickt wurde sondern
	 * auf ein Objekt.
	 *
   * @param o das zu testende Objekt.
   * @return {@code true}, wenn das Item angezeigt werden soll. Andernfalls {@code false}.
   * @see CheckedContextMenuItem#isEnabledFor(Object)
   */
  public boolean isEnabledFor(Object o)
	{
		return true;
	}
}
