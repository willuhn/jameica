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
    this.setSize(460,500);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
  {
    SimpleContainer container = new SimpleContainer(parent,true);
    container.addText(i18n.tr("Sie k�nnen ein Repository l�schen, indem Sie mit der rechten Maustaste auf das betreffende Repository klicken und \"L�schen...\" " +
                              "bzw. \"Deaktivieren...\" w�hlen."),true);
    
    final RepositoryList list = new RepositoryList();
    container.addPart(list);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Neues Repository hinzuf�gen..."),new RepositoryAdd(),null,false,"document-new.png");
    buttons.addButton(i18n.tr("Schlie�en"), new Action()
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
