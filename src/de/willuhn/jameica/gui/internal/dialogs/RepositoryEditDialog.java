/**********************************************************************
 *
 * Copyright (c) 2018 Olaf Willuhn
 * GNU GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.dialogs;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.internal.action.RepositoryAdd;
import de.willuhn.jameica.gui.internal.parts.RepositoryList;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

/**
 * Dialog zum Konfiurieren der Repositories.
 */
public class RepositoryEditDialog extends AbstractDialog<Void>
{
  /**
   * ct.
   * @param position
   */
  public RepositoryEditDialog(int position)
  {
    super(position);
    this.setTitle(i18n.tr("Plugin-Repositories"));
    this.setSize(460,300);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
  {
    SimpleContainer container = new SimpleContainer(parent,true);
    container.addText(i18n.tr("Sie können ein Repository löschen, indem Sie mit der rechten Maustaste auf das betreffende Repository klicken und \"Löschen...\" " +
                              "bzw. \"Deaktivieren...\" wählen."),true);
    
    final RepositoryList list = new RepositoryList();
    container.addPart(list);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Neues Repository hinzufügen..."),new RepositoryAdd(),null,false,"document-new.png");
    buttons.addButton(i18n.tr("Schließen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,false,"window-close.png");
    container.addButtonArea(buttons);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  protected Void getData() throws Exception
  {
    return null;
  }

}
