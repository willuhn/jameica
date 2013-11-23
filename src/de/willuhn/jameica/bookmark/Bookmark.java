/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.bookmark;

import java.io.Serializable;
import java.util.Date;

/**
 * Serialisierbare Bean, die ein Bookmark haelt.
 */
public class Bookmark implements Serializable
{
  // Niemals aendern, damit das auch serialisierbar bleibt, wenn Properties geaendert werden.
  static final long serialVersionUID = -112736636515580220L;
  
  private String view     = null;
  private Context context = null;
  private Date created    = null;
  private String title    = null;
  private String comment  = null;
  
  /**
   * Liefert den Namen der Java-Klasse der gebookmarkten View.
   * @return view Name der Java-Klasse der View.
   */
  public String getView()
  {
    return view;
  }
  
  /**
   * Speichert den Namen der Java-Klasse der gebookmarkten View.
   * @param view view Name der Java-Klasse der View.
   */
  public void setView(String view)
  {
    this.view = view;
  }
  
  /**
   * Liefert die serialisierte Form der Context-Information, die der
   * View beim Start uebergeben werden soll.
   * @return die Context-Information in serialisierter Form.
   */
  public Context getContext()
  {
    return this.context;
  }
  
  /**
   * Speichert die serialisierte Form der Context-Information, die
   * der View beim Start uebergeben werden soll.
   * @param context die Context-Information in serialisierter Form.
   */
  public void setContext(Context context)
  {
    this.context = context;
  }

  /**
   * Liefert das Erstellungsdatum des Bookmarks.
   * @return das Erstellungsdatum.
   */
  public Date getCreated()
  {
    return created;
  }
  
  /**
   * Speichert das Erstellungsdatum des Bookmarks. 
   * @param created das Erstellungsdatum.
   */
  public void setCreated(Date created)
  {
    this.created = created;
  }
  
  /**
   * Liefert den Titel des Bookmark.
   * @return der Titel des Bookmark.
   */
  public String getTitle()
  {
    return title;
  }
  
  /**
   * Speichert den Titel des Bookmark.
   * @param title der Titel des Bookmark.
   */
  public void setTitle(String title)
  {
    this.title = title;
  }
  
  /**
   * Liefert einen Kommentar.
   * @return Kommentar.
   */
  public String getComment()
  {
    return comment;
  }
  
  /**
   * Speichert einen Kommentar.
   * @param comment Kommentar.
   */
  public void setComment(String comment)
  {
    this.comment = comment;
  }
}


