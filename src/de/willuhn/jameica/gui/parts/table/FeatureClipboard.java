/**********************************************************************
 *
 * Copyright (c) 2016 Olaf Willuhn
 * GNU GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts.table;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

/**
 * Feature fuer die Zwischenablage-Funktion.
 */
public class FeatureClipboard implements Feature
{
  private final static Settings settings = new Settings(FeatureClipboard.class);
  private Listener listener = null;
  
  /**
   * @see de.willuhn.jameica.gui.parts.table.Feature#onEvent(de.willuhn.jameica.gui.parts.table.Feature.Event)
   */
  public boolean onEvent(Event e)
  {
    return e == Event.PAINT;
  }

  /**
   * @see de.willuhn.jameica.gui.parts.table.Feature#handleEvent(de.willuhn.jameica.gui.parts.table.Feature.Event, de.willuhn.jameica.gui.parts.table.Feature.Context)
   */
  public void handleEvent(Event e, final Context ctx)
  {
    // Wir wurden bereits registriert
    if (this.listener != null)
      return;
    
    // Die benoetigten Controls sind noch nicht da
    if (ctx.control == null || ctx.menu == null)
      return;
    
    this.applyShortcut(ctx);
    
    // Listener, um die Display-Filter beim Disposen wieder zu entfernen
    ctx.control.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        GUI.getDisplay().removeFilter(SWT.KeyUp,listener);
        Logger.debug("unbound " + listener);
      }
    });
  }
  
  /**
   * Wendet den Shortcut an.
   * @param ctx der Context.
   */
  protected void applyShortcut(final Context ctx)
  {
    final String nl    = settings.getString("line.separator",System.getProperty("line.separator","\n"));
    final String sep   = settings.getString("col.separator",";");
    final String quote = settings.getString("quote.char","\"");

    final KeyStroke shortcut = SWTUtil.getKeyStroke(settings.getString("shortcut","CTRL+C"));
    if (shortcut == null)
      return;

    this.listener = new Listener()
    {
      public void handleEvent(org.eclipse.swt.widgets.Event event)
      {
        char c = (char) shortcut.getNaturalKey();
        if (Character.isLetter(c))
          c = Character.toLowerCase(c);

        if ((event.stateMask == shortcut.getModifierKeys()) && (event.keyCode == c))
        {
          Object control = ctx.control;
          if (!(control instanceof Table) && !(control instanceof Tree))
            return;
          
          // BUGZILLA 1817 - wenn das Widget nicht Table/Tree ist (sondern Text)
          // dann bearbeitet der User gerade einen Text. In dem Fall duerfen wir
          // auch nicht reagieren, da der User den Text aus dem Edit-Feld kopieren will
          Object widget = event.widget;
          if (!(widget instanceof Table) && !(widget instanceof Tree))
            return;
          
          StringBuilder sb = new StringBuilder();
          
          int colCount = 0;
          int rowCount = 0;
          
          if (control instanceof Table)
          {
            Table t = (Table) control;
            colCount = t.getColumnCount();
            TableItem[] rows = t.getSelection();
            if (rows == null || rows.length == 0)
              return;
            
            rowCount = rows.length;
            
            for (TableItem row:rows)
            {
              for (int i=0;i<colCount;++i)
              {
                String s = row.getText(i);
                sb.append(quote);
                sb.append(s != null ? s : "");
                sb.append(quote);
                if (i+1 < colCount)
                  sb.append(sep);
              }
              sb.append(nl);
            }
          }
          else
          {
            Tree t = (Tree) control;
            colCount = t.getColumnCount();
            TreeItem[] rows = t.getSelection();
            if (rows == null || rows.length == 0)
              return;
            
            rowCount = rows.length;
            
            for (TreeItem row:rows)
            {
              for (int i=0;i<colCount;++i)
              {
                String s = row.getText(i);
                sb.append(quote);
                sb.append(s != null ? s : "");
                sb.append(quote);
                if (i+1 < colCount)
                  sb.append(sep);
              }
              sb.append(nl);
            }
          }
          
          Clipboard clipboard = new Clipboard(GUI.getDisplay());
          TextTransfer transfer = TextTransfer.getInstance();
          clipboard.setContents(new Object[]{sb.toString()},new Transfer[]{transfer});
          clipboard.dispose();
          Logger.info("copied text to clipboard. cols: " + colCount + ", rows: " + rowCount);
          if (rowCount > 1)
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("{0} Zeilen in Zwischenablage kopiert",Integer.toString(rowCount)),StatusBarMessage.TYPE_INFO));
          else
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Eine Zeile in Zwischenablage kopiert"),StatusBarMessage.TYPE_INFO));
        }
      }
    };
    
    GUI.getDisplay().addFilter(SWT.KeyUp,listener);
    Logger.debug("bound " + listener);
  }
}
