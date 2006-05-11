/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/dialogs/LogDetailDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/05/11 20:40:06 $
 * $Author: web0 $
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
import de.willuhn.jameica.gui.parts.TextPart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.LabelGroup;
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
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent, Application.getI18n().tr("Eigenschaften"));
    
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    group.addLabelPair(Application.getI18n().tr("Datum"), new LabelInput(df.format(message.getDate())));
    
    Level level = message.getLevel();
    LabelInput l = new LabelInput(level.getName());
    if (level.getValue() == Level.DEBUG.getValue()) l.setColor(Color.COMMENT);
    else if (level.getValue() == Level.WARN.getValue()) l.setColor(Color.LINK_ACTIVE);
    else if (level.getValue() == Level.ERROR.getValue()) l.setColor(Color.ERROR);

    group.addLabelPair(Application.getI18n().tr("Priorität"), l);
    group.addLabelPair(Application.getI18n().tr("Quelle"), new LabelInput(message.getLoggingClass() + "." + message.getLoggingMethod()));
    
    new Headline(parent,Application.getI18n().tr("Nachricht"));
    final String text = message.getText();
    TextPart tp = new TextPart();
    tp.setAutoscroll(false);
    tp.setWordWrap(false);
    tp.appendText(text);
    tp.paint(parent);

    
    ButtonArea buttons = new ButtonArea(parent,1);
    buttons.addButton(Application.getI18n().tr("Schliessen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true);
    
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
 * Revision 1.2  2006/05/11 20:40:06  web0
 * @N schoenere Formatierung des LogDetail-Dialogs
 *
 * Revision 1.1  2006/03/07 22:38:02  web0
 * @N LogDetailView
 *
 **********************************************************************/