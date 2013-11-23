/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.dialogs;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Dialog zum Erstellen eines Bookmarks.
 */
public class BookmarkAddDialog extends AbstractDialog
{
  private final static int WINDOW_WIDTH = 450;
  private String comment = null;
  
  /**
   * ct
   */
  public BookmarkAddDialog()
  {
    super(BookmarkAddDialog.POSITION_CENTER);
    this.setSize(WINDOW_WIDTH,200);
    this.setTitle(Application.getI18n().tr("Lesezeichen erstellen"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return this.comment;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    SimpleContainer container = new SimpleContainer(parent,true);
    container.addText(Application.getI18n().tr("Notiz zu diesem Lesezeichen"),true);
    final TextAreaInput text = new TextAreaInput(null,500);
    container.addPart(text);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(Application.getI18n().tr("Übernehmen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        comment = (String) text.getValue();
        close();
      }
    },null,true,"ok.png");
    buttons.addButton(new Cancel());
    
    container.addButtonArea(buttons);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,200));

  }
}
