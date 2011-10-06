/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/dialogs/ChooseBoxesDialog.java,v $
 * $Revision: 1.15 $
 * $Date: 2011/10/06 10:49:08 $
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
import java.util.List;

import org.eclipse.swt.widgets.Composite;
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
import de.willuhn.jameica.gui.parts.TablePart;
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
    this.setSize(400,400);
    setTitle(i18n.tr("Auswahl der anzuzeigenden Elemente"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    List<Box> boxes = BoxRegistry.getBoxes();
    
    Container c = new SimpleContainer(parent,true);
    c.addText(i18n.tr("Wählen Sie die anzuzeigenden Elemente aus."),true);

    table = new TablePart(boxes,null);
    table.addColumn(i18n.tr("Bezeichnung"),"name");
    table.setCheckable(true);
    table.setMulti(false);
    table.setSummary(false);
    table.setRememberOrder(false); // Die Reihenfolge wird ja durch die Indizes bestimmt
    
    table.setFormatter(new TableFormatter() {
      public void format(TableItem item)
      {
        if (item == null || item.getData() == null)
          return;
        Box box = (Box) item.getData();
        item.setChecked(box.isEnabled());
        
        if (!box.isActive())
        {
          item.setForeground(Color.COMMENT.getSWTColor());
          item.setGrayed(true);
        }
        else
        {
          item.setForeground(Color.WIDGET_FG.getSWTColor());
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
    buttons.addButton(i18n.tr("Übernehmen"), new Action() {
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
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }
}


/*********************************************************************
 * $Log: ChooseBoxesDialog.java,v $
 * Revision 1.15  2011/10/06 10:49:08  willuhn
 * @N Termin-Provider konfigurierbar
 *
 * Revision 1.14  2011-05-05 09:43:00  willuhn
 * @B Beim Umsortierungen ging der Checked-State verloren, wenn er noch nicht gespeichert war
 *
 * Revision 1.13  2011-05-05 09:36:25  willuhn
 * @C SearchOptionsDialog ueberarbeitet - beim Aendern der Sortierung gingen die Markierungen verloren
 *
 * Revision 1.12  2011-05-05 09:18:41  willuhn
 * @C Contextmenu ersetzt gegen anklickbare Zeilen. Erheblich ergonomischer
 *
 * Revision 1.11  2011-05-03 12:57:00  willuhn
 * @B Das komplette Ausblenden nicht-aktiver Boxen fuehrte zu ziemlichem Durcheinander in dem Dialog
 * @C Aendern der Sortier-Reihenfolge vereinfacht. Sie wird jetzt nicht mehr live sondern erst nach Klick auf "Uebernehmen" gespeichert - was fachlich ja auch richtiger ist
 *
 * Revision 1.10  2011-05-03 11:39:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2011-05-03 11:38:47  willuhn
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
 **********************************************************************/