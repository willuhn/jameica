/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.dialogs;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.boxes.Box;
import de.willuhn.jameica.gui.boxes.BoxRegistry;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.internal.action.Start;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TableChangeListener;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
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
    List<Box> boxes = BoxRegistry.getBoxes();
    
    Container c = new SimpleContainer(parent,true);
    c.addText(i18n.tr("W�hlen Sie die anzuzeigenden Elemente aus."),true);

    table = new TablePart(boxes,null)
    {
      /**
       * @see de.willuhn.jameica.gui.parts.TablePart#getEditorControl(int, org.eclipse.swt.widgets.TableItem, java.lang.String)
       * Ueberschrieben, um nur Ziffern bei der Hoehe zuzulassen.
       */
      protected Control getEditorControl(int row, TableItem item, String oldValue)
      {
        final String validChars = "0123456789";
        Control c = super.getEditorControl(row, item, oldValue);
        c.addListener(SWT.Verify, new Listener()
        {
          public void handleEvent(Event e)
          {
            char[] chars = e.text.toCharArray();
            for (int i=0; i<chars.length; i++) {
              if (validChars.indexOf(chars[i]) == -1)
              {
                e.doit = false;
                return;
              }
            }
          }
        });
        return c;
      }
    };
    table.addColumn(i18n.tr("Bezeichnung"),"name");
    table.addColumn(i18n.tr("H�he des Elements"),"height",null,true,Column.ALIGN_RIGHT);
    table.setCheckable(true);
    table.setMulti(false);
    table.removeFeature(FeatureSummary.class);
    table.setRememberOrder(false); // Die Reihenfolge wird ja durch die Indizes bestimmt
    
    table.setFormatter(new TableFormatter() {
      public void format(TableItem item)
      {
        if (item == null || item.getData() == null)
          return;
        Box box = (Box) item.getData();
        item.setChecked(box.isEnabled());
        
        int height = BoxRegistry.getHeight(box);
        item.setText(1,height > 0 ? Integer.toString(height) : "");
        
        if (!box.isActive())
        {
          item.setForeground(Color.COMMENT.getSWTColor());
          item.setGrayed(true);
        }
        else
        {
          item.setForeground(Color.FOREGROUND.getSWTColor());
        }
      }
    });
    
    table.addSelectionListener(new Listener() {
      public void handleEvent(Event event)
      {
        Box b = (Box) event.data;
        if (b == null)
          return;
        
        boolean active = b.isActive();
        up.setEnabled(active);
        down.setEnabled(active);
      }
    });
    
    table.addChangeListener(new TableChangeListener()
    {
      public void itemChanged(Object object, String attribute, String newValue) throws ApplicationException
      {
        if (object == null)
          return;
        
        int i = Box.HEIGHT_DEFAULT;
        try
        {
          if (StringUtils.isNotEmpty(newValue))
            i = Integer.parseInt(newValue);
        }
        catch (Exception e)
        {
          Logger.warn("invalid height given: " + newValue);
          return;
        }
        BoxRegistry.setHeight((Box)object,i);
      }
    });

    c.addPart(table);

    ButtonArea buttons = new ButtonArea();
    
    up = new Button(i18n.tr("Nach oben"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        Box box = (Box) table.getSelection();
        if (box == null) // Keine Box markiert
          return;

        try
        {
          List checked = table.getItems(true);
          
          // Entfernen und eins weiter oben wieder einfuegen
          int index = table.removeItem(box);
          if (index == -1)
            return; // gabs gar nicht
          
          table.addItem(box,index == 0 ? 0 : index-1); // wenn wir schon ganz oben waren, bleiben wir dort
          table.select(box);
          table.setChecked(box,checked.contains(box)); // Checked-State wiederherstellen
        }
        catch (Exception e)
        {
          Logger.error("Fehler beim Verschieben des Elementes",e);
        }
      }
    },null,false,"maximize.png");
    down = new Button(i18n.tr("Nach unten"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        Box box = (Box) table.getSelection();
        if (box == null) // Keine Box markiert
          return;
        
        try
        {
          List checked = table.getItems(true);

          int index = table.removeItem(box);
          if (index == -1)
            return; // gabs gar nicht
          
          int size = table.size();
          
          if (index < size) // Index nur erhoehen, solange wir nich schon die letzten sind
            index++;
          

          table.addItem(box,index);
          table.select(box);
          table.setChecked(box,checked.contains(box)); // Checked-State wiederherstellen
        }
        catch (Exception e)
        {
          Logger.error("Fehler beim Verschieben des Elementes",e);
        }
      }
    },null,false,"minimize.png");
    
    buttons.addButton(up);
    buttons.addButton(down);
    buttons.addButton(i18n.tr("�bernehmen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          // Indizes und Status speichern
          List<Box> boxes   = table.getItems(false);
          List<Box> checked = table.getItems(true);
          for (int i=0;i<boxes.size();++i)
          {
            Box box = boxes.get(i);
            box.setIndex(i);
            box.setEnabled(checked.contains(box));
          }
        }
        catch (RemoteException re)
        {
          Logger.error("unable to apply box states",re);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Speichern: {0}",re.getMessage()),StatusBarMessage.TYPE_ERROR));
        }
        
        close();
        new Start().handleAction(context); // Startseite neu laden
      }
    },null,true,"ok.png");
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");
    c.addButtonArea(buttons);
    
    getShell().setMinimumSize(550,400);
    getShell().setSize(getShell().computeSize(550,SWT.DEFAULT));

  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }
}
