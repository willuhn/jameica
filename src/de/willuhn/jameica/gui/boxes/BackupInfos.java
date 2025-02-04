/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.boxes;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.services.BackupService;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Eine Box, die darauf hinweist, wenn das Backup beim letzten Shutdown abgebrochen wurde.
 */
public class BackupInfos extends AbstractBox
{
  private final static I18N i18n = Application.getI18n();
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Jameica: " + Application.getI18n().tr("Backup-Informationen");
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#getHeight()
   */
  @Override
  public int getHeight()
  {
    return 300;
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isActive()
   */
  @Override
  public boolean isActive()
  {
    return !this.getErrors().isEmpty();
  }
  
  /**
   * Liefert eine Liste der Fehler, die während der letzten Sitzung auftraten.
   * @return die Liste der Fehler. Nie NULL sondern höchstens eine leere Liste.
   */
  private List<String> getErrors()
  {
    final BackupService service = Application.getBootLoader().getBootable(BackupService.class);
    return service.getLastErrors();
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return !this.getErrors().isEmpty();
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 0;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    final InfoPanel panel = new InfoPanel() {
      /**
       * @see de.willuhn.jameica.gui.parts.InfoPanel#extend(de.willuhn.jameica.gui.parts.InfoPanel.DrawState, org.eclipse.swt.widgets.Composite, java.lang.Object)
       */
      @Override
      public Composite extend(DrawState state, Composite comp, Object context)
      {
        if (state != DrawState.TEXT_AFTER)
          return comp;
        
        final TablePart table = new TablePart(getErrors(),null);
        table.setMulti(false);
        table.setRememberColWidths(true);
        table.setRememberOrder(true);
        table.setRememberState(true);
        table.removeFeature(FeatureSummary.class);
        table.addColumn(i18n.tr("Fehlermeldung"),null);
        
        final Composite myComp = new Composite(comp,SWT.NONE);
        myComp.setLayout(SWTUtil.createGrid(1,false));

        final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 150;
        myComp.setLayoutData(gd);

        final Container c = new SimpleContainer(myComp,true);
        c.addPart(table);
        
        return comp;
      }
    };
    panel.setTitle(i18n.tr(i18n.tr("Hinweis zum letzten Backup")));
    panel.setIcon("dialog-information-large.png");

    panel.setText(i18n.tr("Während der letzten Programmsitzung traten die folgenden Fehler auf, welche die automatische Erstellung des Backups beim Programmende verhinderten."));
    panel.setComment(i18n.tr("Prüfen Sie ggf. die Logdatei auf vorhandene Fehlermeldungen."));
    panel.setUrl("https://www.willuhn.de/wiki/doku.php?id=support:fehlermelden");
    panel.paint(parent);
  }
}
