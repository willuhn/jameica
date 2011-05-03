/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/dialogs/ChooseBoxesDialog.java,v $
 * $Revision: 1.9 $
 * $Date: 2011/05/03 11:38:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.dialogs;

import java.rmi.RemoteException;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.boxes.Box;
import de.willuhn.jameica.gui.boxes.BoxRegistry;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.internal.action.Start;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Dialog zum Konfigurieren der Boxen.
 */
public class ChooseBoxesDialog extends AbstractDialog
{
  private TablePart table = null;
  private Button down     = null;
  private Button up       = null;

  /**
   * @param position
   */
  public ChooseBoxesDialog(int position)
  {
    super(position);
    setTitle(i18n.tr("Auswahl der anzuzeigenden Elemente"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Box[] list = BoxRegistry.getBoxes();

    // Wir kopieren die Daten in eine Liste von GenericObjects
    Vector v = new Vector();
    for (int i=0;i<list.length;++i)
    {
      Box box = list[i];
      if (!box.isActive())
        continue;
      v.add(new BoxObject(box));
    }
    
    GenericIterator iterator = PseudoIterator.fromArray((BoxObject[]) v.toArray(new BoxObject[v.size()]));
    table = new TablePart(iterator,null);
    table.addColumn(i18n.tr("Bezeichnung"),"name");
    table.addColumn(i18n.tr("Status"),"enabled", new Formatter() {
      public String format(Object o)
      {
        if (o == null || !(o instanceof Boolean))
          return null;
        return ((Boolean) o).booleanValue() ? i18n.tr("Aktiv") : "-"; 
      }
    });
    table.setMulti(false);
    table.setSummary(false);
    table.setFormatter(new TableFormatter() {
      public void format(TableItem item)
      {
        if (item == null || item.getData() == null)
          return;
        BoxObject o = (BoxObject) item.getData();
        if (o.box.isEnabled())
          item.setForeground(Color.SUCCESS.getSWTColor());
        else if (!o.box.isActive())
          item.setForeground(Color.COMMENT.getSWTColor());
        else
          item.setForeground(Color.WIDGET_FG.getSWTColor());
      }
    });
    
    ContextMenu menu = new ContextMenu();
    menu.addItem(new MyMenuItem(true));
    menu.addItem(new MyMenuItem(false));
    table.setContextMenu(menu);

    Container c = new SimpleContainer(parent);
    c.addPart(table);

    ButtonArea buttons = new ButtonArea();
    
    up = new Button(i18n.tr("Nach oben"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        BoxObject o = (BoxObject) table.getSelection();
        if (o == null)
          return;
        
        if (BoxRegistry.up(o.box))
        {
          int index = table.removeItem(o);
          if (index != -1)
          {
            try
            {
              table.addItem(o,index-1);
              table.select(o);
            }
            catch (Exception e)
            {
              Logger.error("Fehler beim Verschieben des Elementes",e);
            }
          }
        }
      }
    },null,false,"maximize.png");
    down = new Button(i18n.tr("Nach unten"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        BoxObject o = (BoxObject) table.getSelection();
        if (o == null)
          return;
        if (BoxRegistry.down(o.box))
        {
          int index = table.removeItem(o);
          if (index != -1)
          {
            try
            {
              table.addItem(o,o.box.getIndex());
              table.select(o);
            }
            catch (Exception e)
            {
              Logger.error("Fehler beim Verschieben des Elementes",e);
            }
          }
        }
      }
    },null,false,"minimize.png");
    
    buttons.addButton(up);
    buttons.addButton(down);
    buttons.addButton(i18n.tr("Übernehmen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
        new Start().handleAction(context);
      }
    },null,true,"ok.png");
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,false,"process-stop.png");
    c.addButtonArea(buttons);
    
    getShell().setMinimumSize(getShell().computeSize(SWT.DEFAULT,SWT.DEFAULT));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

  /**
   * Hilfs-Objekt, um Boxen zu GenericObjects zu machen.
   */
  private class BoxObject implements GenericObject
  {
    private Box box = null;
    
    /**
     * ct.
     * @param box
     */
    private BoxObject(Box box)
    {
      this.box = box;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      if ("enabled".equals(arg0))
        return Boolean.valueOf(box.isEnabled());
      return box.getName();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"name","enabled"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return box.getClass().getName();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject arg0) throws RemoteException
    {
      if (arg0 == null || !(arg0 instanceof BoxObject))
        return false;
      BoxObject other = (BoxObject) arg0;
      return this.getID().equals(other.getID());
    }
    
  }
  
  /**
   * Hilsklasse.
   */
  private class MyMenuItem extends CheckedContextMenuItem
  {
    private boolean state = false;
    
    /**
     * ct.
     * @param state
     */
    private MyMenuItem(boolean state)
    {
      super(state ? i18n.tr("Aktivieren") : i18n.tr("Deaktivieren"), new MyAction(state));
      this.state = state;
    }
    
    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o == null || !(o instanceof BoxObject))
        return false;
      BoxObject bo = (BoxObject) o;
      if (!bo.box.isActive())
        return false;
      return state ^ bo.box.isEnabled();
    }
  }
  
  /**
   * Hilfsklasse.
   */
  private class MyAction implements Action
  {
    private boolean state = false;
    
    /**
     * @param state
     */
    private MyAction(boolean state)
    {
      this.state = state;
    }
    
    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
      if (context == null || !(context instanceof BoxObject))
        return;
      BoxObject o = (BoxObject) context;
      o.box.setEnabled(state);
      
      // Element entfernen und wieder hinzufuegen, damit die Ansicht aktualisiert wird
      int index = table.removeItem(o);
      if (index != -1)
      {
        try
        {
          table.addItem(o,index);
          table.select(o);
        }
        catch (Exception e)
        {
          Logger.error("Fehler beim Aktualisieren des Elementes",e);
        }
      }
    }
    
  }
}


/*********************************************************************
 * $Log: ChooseBoxesDialog.java,v $
 * Revision 1.9  2011/05/03 11:38:47  willuhn
 * @B daemliches OS X
 *
 * Revision 1.8  2011-05-03 11:33:57  willuhn
 * @N Button "Startseite anpassen" als Panel-Button
 * @B das Entfernen und Wiederhinzufuegen von Elementen im ChooseBoxDialog fuehrte unter OS X zu einer ArrayIndexOutOfBoundsException - warum auch immer
 *
 * Revision 1.7  2011-04-29 16:34:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2011-04-29 16:27:02  willuhn
 * @N MAC - Dialog war zu schmal
 *
 * Revision 1.5  2011-04-26 12:20:24  willuhn
 * @B Potentielle Bugs gemaess Code-Checker
 *
 * Revision 1.4  2011-01-14 17:33:39  willuhn
 * @N Erster Code fuer benutzerdefinierte Erinnerungen via Reminder-Framework
 *
 * Revision 1.3  2008-04-23 09:53:02  willuhn
 * @N Abbrechen-Button in Dialog
 *
 * Revision 1.2  2006/08/02 09:12:02  willuhn
 * @B Sortierung der Boxen auf der Startseite
 *
 * Revision 1.1  2006/06/29 23:10:01  willuhn
 * @N Box-System aus Hibiscus in Jameica-Source verschoben
 *
 * Revision 1.2  2005/11/20 23:39:11  willuhn
 * @N box handling
 *
 * Revision 1.1  2005/11/09 01:13:53  willuhn
 * @N chipcard modul fuer AMD64 vergessen
 * @N Startseite jetzt frei konfigurierbar
 *
 **********************************************************************/