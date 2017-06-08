/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 * 
 * GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.dialogs;

import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.plugin.Dependency;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog, der fehlende Plugin-Abhaengigkeiten anzeigt.
 */
public class DependencyMissingDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getI18n();
  private List<Dependency> missing = null;

  /**
   * ct.
   * @param position Dialog-Position.
   * @param missing die Liste der fehlenden Abhaengigkeiten.
   */
  public DependencyMissingDialog(int position, List<Dependency> missing)
  {
    super(position);
    this.missing = missing;
    this.setTitle(i18n.tr("Fehlende Abhängigkeiten"));
    this.setSize(360,300);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    SimpleContainer container = new SimpleContainer(parent,true);
    container.addText(i18n.tr("Das Plugin kann nicht installiert werden, da noch weitere Plugins benötigt werden, " +
    		                      "die nicht (oder nicht in der passenden Version) installiert sind und auch in " +
    		                      "keinem der Plugin-Repositories verfügbar sind."),true);
    
    TablePart deps = new TablePart(this.missing,null);
    deps.addColumn(i18n.tr("Name des Plugins"), "name");
    deps.addColumn(i18n.tr("Benötigte Version"),"version");
    deps.setMulti(false);
    deps.setRememberColWidths(false);
    deps.setRememberOrder(true);
    deps.removeFeature(FeatureSummary.class);
    
    container.addPart(deps);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Schließen"),new Action() {
    
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true,"process-stop.png");

    container.addButtonArea(buttons);
  }

}
