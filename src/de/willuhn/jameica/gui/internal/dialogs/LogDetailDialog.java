/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/dialogs/LogDetailDialog.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/05/18 15:18:43 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.dialogs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TextPart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Level;
import de.willuhn.logging.Message;
import de.willuhn.util.ApplicationException;

/**
 * Ein Dialog, der die Details einer Log-Nachricht anzeigen kann.
 */
public class LogDetailDialog extends AbstractDialog
{
  private Message message = null;

  /**
   * ct.
   * @param message die anzuzeigende Log-Nachricht.
   * @param position
   */
  public LogDetailDialog(Message message, int position)
  {
    super(position);
    this.message = message;
    this.setTitle(Application.getI18n().tr("Details der System-Nachricht"));
    this.setSize(450,300);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container container = new SimpleContainer(parent,true);
    
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    container.addLabelPair(Application.getI18n().tr("Datum"), new LabelInput(df.format(message.getDate())));
    
    Level level = message.getLevel();
    LabelInput l = new LabelInput(level.getName());
    if (level.getValue() <= Level.DEBUG.getValue()) l.setColor(Color.COMMENT);
    else if (level.getValue() == Level.WARN.getValue()) l.setColor(Color.LINK_ACTIVE);
    else if (level.getValue() >= Level.ERROR.getValue()) l.setColor(Color.ERROR);

    container.addLabelPair(Application.getI18n().tr("Priorit‰t"), l);
    container.addLabelPair(Application.getI18n().tr("Quelle"), new LabelInput(message.getLoggingClass() + "." + message.getLoggingMethod()));
    
    container.addHeadline(Application.getI18n().tr("Nachricht"));
    
    TextPart tp = new TextPart();
    tp.setAutoscroll(false);
    tp.setWordWrap(true);
    tp.appendText(message.getText());
    
    container.addPart(tp);

    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(Application.getI18n().tr("Schlieﬂen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true,"process-stop.png");
    container.addButtonArea(buttons);
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
 * $Log: LogDetailDialog.java,v $
 * Revision 1.5  2011/05/18 15:18:43  willuhn
 * @B BUGZILLA 1049
 *
 * Revision 1.4  2011-05-03 10:13:11  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.3  2008/04/15 16:16:36  willuhn
 * @B BUGZILLA 584
 *
 * Revision 1.2  2006/05/11 20:40:06  web0
 * @N schoenere Formatierung des LogDetail-Dialogs
 *
 * Revision 1.1  2006/03/07 22:38:02  web0
 * @N LogDetailView
 *
 **********************************************************************/