/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/

package de.willuhn.jameica.gui.boxes;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.internal.action.Program;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.plugin.Version;
import de.willuhn.jameica.services.UpdateService;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Box, die anzeigt, wenn eine neuere Jameica-Version zum Download verfügbar ist.
 * Jameica kann sich im laufenden Betrieb nicht selbst aktualisieren - die Box
 * dient daher lediglich als Hinweis, der den User zum manuellen Download der
 * neuen Version sowie zur Update-Anleitung führt.
 */
public class JameicaUpdate extends AbstractBox
{
  private final static I18N i18n = Application.getI18n();

  /**
   * URL der Download-Seite, auf der die aktuelle Jameica-Version bezogen werden kann.
   */
  private final static String URL_DOWNLOAD = "https://www.willuhn.de/products/jameica/download.php";

  /**
   * URL der Anleitung zur Aktualisierung von Jameica.
   */
  private final static String URL_HOWTO = "https://www.willuhn.de/wiki/doku.php?id=support:update";

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Jameica: " + i18n.tr("Update verfügbar");
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isActive()
   */
  public boolean isActive()
  {
    return isEnabled();
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isEnabled()
   */
  public boolean isEnabled()
  {
    // Nur anzeigen, wenn eine neuere Jameica-Version bekannt ist.
    UpdateService service = Application.getBootLoader().getBootable(UpdateService.class);
    return service.getJameicaUpdate() != null;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    // Das darf der User nicht.
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return true;
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
    UpdateService service = Application.getBootLoader().getBootable(UpdateService.class);
    Version available = service.getJameicaUpdate();
    Version current   = Application.getManifest().getVersion();

    InfoPanel panel = new InfoPanel();
    panel.setIcon("dialog-information-large.png");
    panel.setTitle(i18n.tr("Neue Jameica-Version verfügbar"));
    panel.setText(i18n.tr("Es steht eine neue Version {0} von Jameica zum Download bereit. Sie verwenden derzeit die Version {1}.\n" +
                          "Da Jameica sich im laufenden Betrieb nicht selbst aktualisieren kann, muss die neue Version manuell installiert werden.",
                          new String[]{available != null ? available.toString() : "",current != null ? current.toString() : ""}));
    panel.setComment(i18n.tr("Eine Anleitung zur Aktualisierung von Jameica finden Sie in Wiki."));

    {
      Button button = new Button(i18n.tr("Neue Version herunterladen"),new Program(),URL_DOWNLOAD,false,"document-save.png");
      panel.addButton(button);
    }

    {
      Button button = new Button(i18n.tr("Update-Anleitung"),new Program(),URL_HOWTO,false,"gtk-info.png");
      panel.addButton(button);
    }

    panel.paint(parent);
  }

}
