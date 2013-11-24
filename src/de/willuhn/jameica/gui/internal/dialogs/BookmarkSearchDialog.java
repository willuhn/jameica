/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.dialogs;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.bookmark.Bookmark;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.internal.parts.BookmarkTreePart;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Dialog zum Suchen von Bookmarks.
 */
public class BookmarkSearchDialog extends AbstractDialog
{
  private final static int WINDOW_WIDTH = 450;
  private Bookmark chosen = null;
  private BookmarkTreePart tree = null;
  
  /**
   * ct
   */
  public BookmarkSearchDialog()
  {
    super(BookmarkSearchDialog.POSITION_CENTER);
    this.setSize(WINDOW_WIDTH,400);
    this.setTitle(Application.getI18n().tr("Lesezeichen suchen"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return this.chosen;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    SimpleContainer container = new SimpleContainer(parent,true);
    
    tree = new BookmarkTreePart();
    container.addPart(tree);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(Application.getI18n().tr("Übernehmen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true,"ok.png");
    buttons.addButton(new Cancel());
    
    container.addButtonArea(buttons);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,400));

  }
}
