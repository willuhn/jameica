/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.dialogs;

import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.bookmark.Bookmark;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.internal.action.BookmarkDelete;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.internal.parts.BookmarkTreePart;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Dialog zum Suchen von Bookmarks.
 */
public class BookmarkSearchDialog extends AbstractDialog
{
  private final static int WINDOW_WIDTH = 550;
  private Bookmark chosen = null;
  private BookmarkTreePart tree = null;
  private TextInput search = null;
  
  /**
   * ct
   */
  public BookmarkSearchDialog()
  {
    super(BookmarkSearchDialog.POSITION_CENTER);
    this.setSize(WINDOW_WIDTH,400);
    this.setTitle(Application.getI18n().tr("Lesezeichen"));
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
    
    this.search = new TextInput(null,100);
    this.search.setName(Application.getI18n().tr("Suchbegriff"));
    
    this.tree = new BookmarkTreePart(new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (!(context instanceof Bookmark))
          return;
        
        chosen = (Bookmark) context;
        close();
      }
    });
    
    container.addInput(this.search);
    container.addPart(this.tree);
    
    this.search.getControl().addKeyListener(new DelayedAdapter());
    this.search.focus();

    final Button apply = new Button(Application.getI18n().tr("Öffnen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        Object sel = tree.getSelection();
        if (!(sel instanceof Bookmark))
          return;
        
        chosen = (Bookmark) sel;
        close();
      }
    },null,true,"ok.png");
    apply.setEnabled(false);
    
    final Button delete = new Button(Application.getI18n().tr("Löschen..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        Object sel = tree.getSelection();
        if (!(sel instanceof Bookmark))
          return;

        new BookmarkDelete().handleAction(sel);
        tree.update((String)search.getValue());
      }
    },null,true,"user-trash-full.png");
    delete.setEnabled(false);

    this.tree.addSelectionListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        boolean b = (event.data instanceof Bookmark);
        apply.setEnabled(b);
        delete.setEnabled(b);
      }
    });
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(apply);
    buttons.addButton(delete);
    buttons.addButton(new Cancel());
    container.addButtonArea(buttons);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,400));
  }
  
  /**
   * Verzoegert das Ausloesen des Events etwas, damit man in Ruhe tippen kann.
   */
  private class DelayedAdapter extends KeyAdapter
  {
    private Listener forward = new DelayedListener(300,new Listener()
    {
      /**
       * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
       */
      public void handleEvent(Event event)
      {
        try
        {
          tree.update((String) search.getValue());
        }
        catch (ApplicationException ae)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
        }
      }
    
    });

    /**
     * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
     */
    public void keyReleased(KeyEvent e)
    {
      forward.handleEvent(null); // Das Event-Objekt interessiert uns eh nicht
    }
  }
  
}
