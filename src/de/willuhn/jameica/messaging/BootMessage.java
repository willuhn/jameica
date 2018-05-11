/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.willuhn.jameica.gui.parts.Button;

/**
 * Eine Boot-Message, die zum Beispiel versendet wird, wenn es beim Boot-Vorgang
 * zu einem Fehler kam, der dem User angezeigt werden soll.
 */
public class BootMessage implements Message
{
  private String title   = null;
  private String text    = null;
  private String comment = null;
  private String url     = null;
  private String icon    = "dialog-information-large.png";
  private List<Button> buttons = new LinkedList<Button>();
  
  /**
   * ct.
   * @param text der Text.
   */
  public BootMessage(String text)
  {
    this.text  = text;
  }

  /**
   * Liefert den Titel.
   * @return title der Titel.
   */
  public String getTitle()
  {
    return title;
  }

  /**
   * Speichert den Titel.
   * @param title der Titel.
   */
  public void setTitle(String title)
  {
    this.title = title;
  }

  /**
   * Liefert den Text.
   * @return text der Text.
   */
  public String getText()
  {
    return text;
  }

  /**
   * Speichert den Text.
   * @param text der Text.
   */
  public void setText(String text)
  {
    this.text = text;
  }

  /**
   * Liefert einen Kommentar.
   * @return comment Kommentar.
   */
  public String getComment()
  {
    return comment;
  }

  /**
   * Speichert einen Kommentar.
   * @param comment der Kommentar.
   */
  public void setComment(String comment)
  {
    this.comment = comment;
  }

  /**
   * Liefert eine URL.
   * @return url die URL.
   */
  public String getUrl()
  {
    return url;
  }

  /**
   * Speichert eine URL.
   * @param url die URL.
   */
  public void setUrl(String url)
  {
    this.url = url;
  }

  /**
   * Liefert ein optionales Icon.
   * @return icon
   */
  public String getIcon()
  {
    return icon;
  }

  /**
   * Speichert ein optionales Icon.
   * @param icon icon
   */
  public void setIcon(String icon)
  {
    this.icon = icon;
  }

  /**
   * Fuegt einen Button hinzu, der angezeigt werden soll.
   * @param button optionaler Button.
   */
  public void addButton(Button button)
  {
    this.buttons.add(button);
  }
  
  /**
   * Liefert ein optionale Liste von Buttons.
   * @return optionale Liste von Buttons.
   */
  public List<Button> getButtons()
  {
    return Collections.unmodifiableList(this.buttons);
  }
  

}


