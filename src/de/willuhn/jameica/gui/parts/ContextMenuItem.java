/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/ContextMenuItem.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/07/20 21:47:44 $
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
import org.eclipse.swt.widgets.Listener;

/**
 * Bildet ein Element eines Kontext-Menus ab.
 */
public class ContextMenuItem
{
	private boolean separator = false;
	private String text       = null;
	private Listener listener = null;
	private Image image       = null;

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
	}

	/**
	 * ct.
   * @param text Anzuzeigender Text.
   * @param l Listener, der beim Klick ausgeloest werden soll.
   */
  public ContextMenuItem(String text, Listener l)
	{
		this.text = text;
		this.listener = l;
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
   * @return
   */
  public Image getImage()
  {
    return image;
  }

  /**
   * @return
   */
  public Listener getListener()
  {
    return listener;
  }

  /**
   * @return
   */
  public String getText()
  {
    return text;
  }

  /**
   * @param image
   */
  public void setImage(Image image)
  {
    this.image = image;
  }

  /**
   * @param listener
   */
  public void setListener(Listener listener)
  {
    this.listener = listener;
  }

  /**
   * @param string
   */
  public void setText(String string)
  {
    text = string;
  }

	/**
	 * Diese Funktion wird fuer jedes MenuItem aufgerufen, wenn sich das Kontext-Menu oeffnet.
	 * Folglich kann hier jedes MenuItem selbst bestimmen, ob es fuer das aktuelle
	 * Objekt verfuegbar sein soll oder nicht.
	 * Diese Default-Implementierung liefert immer <code>true</code>.
	 * Soll das Item also bei Bedarf deaktiviert werden, muss von dieser
	 * Klasse abgeleitet und diese Methode ueberschrieben werden.
   * @param o das zu testende Objekt.
   * @return true, wenn das Item angezeigt werden soll. Andernfalls <code>false</code>.
   */
  public boolean isEnabledFor(Object o)
	{
		return true;
	}
}


/**********************************************************************
 * $Log: ContextMenuItem.java,v $
 * Revision 1.1  2004/07/20 21:47:44  willuhn
 * @N ContextMenu
 *
 **********************************************************************/