/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/PluginDetailPart.java,v $
 * $Revision: 1.3 $
 * $Date: 2012/03/28 22:28:07 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.internal.action.PluginDetails;
import de.willuhn.jameica.gui.internal.action.PluginUnInstall;
import de.willuhn.jameica.gui.internal.action.PluginUpdate;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt die Kurz-Infos eines Plugins inclusive Buttons fuer die Verwaltung
 * des Plugins an.
 */
public class PluginDetailPart extends InfoPanel
{
  private Manifest manifest = null;
  private Composite comp = null;
  
  /**
   * ct.
   * @param mf das Manifest des Plugins.
   */
  public PluginDetailPart(Manifest mf)
  {
    this.manifest = mf;
    this.setUrl(this.manifest.getHomepage());
    this.setIcon(this.manifest.getIcon());
    this.setText(this.manifest.getDescription());
    
    if (!this.manifest.isInstalled())
      this.setForeground(Color.COMMENT);
    
    // Haben wir einen Init-Error bei dem Plugin?
    Throwable error = Application.getPluginLoader().getInitErrors().get(manifest);

    I18N i18n = Application.getI18n();

    String title = this.manifest.getName();
    if (!manifest.isInstalled())
    {
      // Checken, ob wir eine Fehlermeldung haben
      title = i18n.tr("{0} ({1})",title, (error != null ? error.getMessage() : i18n.tr("Neustart erforderlich")));
    }
    this.setTitle(title);
    
    this.setComment(i18n.tr("Version {0}",this.manifest.getVersion().toString()));
    
    String builddate   = this.manifest.getBuildDate();
    String buildnumber = this.manifest.getBuildnumber();
    if (builddate != null && builddate.length() > 0 && buildnumber != null && buildnumber.length() > 0)
      this.setTooltip(i18n.tr("Build-Datum {0}\nBuildnummer {1}",builddate,buildnumber));
    
    // Buttons zum Oeffnen, Deinstallieren, Aktualisieren
    Button open   = new Button(i18n.tr("Öffnen..."),new PluginDetails(),this.manifest,false,"document-open.png");
    Button update = new Button(i18n.tr("Plugin aktualisieren..."),new PluginUpdate(),this.manifest,false,"emblem-package.png");
    Button delete = new Button(i18n.tr("Plugin löschen..."),new PluginUnInstall(),this.manifest,false,"user-trash-full.png");

    // Update und oeffnen gibt es nicht bei neuen Installationen
    open.setEnabled(manifest.isInstalled());
    update.setEnabled(manifest.isInstalled() || error != null); // Update auch bei Fehler erlauben

    // Checken, ob es installiert/deinstalliert werden kann
    try
    {
      Application.getPluginLoader().canUnInstall(this.manifest);
    }
    catch (ApplicationException ae) {
      delete.setEnabled(false);
    }

    this.addButton(open);
    this.addButton(update);
    this.addButton(delete);
  }

  /**
   * Disposed das Part.
   */
  public void dispose()
  {
    try
    {
      if (this.comp == null || this.comp.isDisposed())
        return;
      SWTUtil.disposeChildren(this.comp);
      this.comp.dispose();
    }
    finally
    {
      this.comp = null;
    }
  }
}



/**********************************************************************
 * $Log: PluginDetailPart.java,v $
 * Revision 1.3  2012/03/28 22:28:07  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.2  2011-08-03 11:58:06  willuhn
 * @N PluginLoader#getInitError
 *
 * Revision 1.1  2011-06-02 12:15:16  willuhn
 * @B Das Handling beim Update war noch nicht sauber
 *
 * Revision 1.2  2011-06-01 21:20:02  willuhn
 * @N Beim Deinstallieren die Navi und Menupunkte des Plugins deaktivieren
 * @N Frisch installierte aber noch nicht aktive Plugins auch dann anzeigen, wenn die View verlassen wird
 *
 * Revision 1.1  2011-06-01 17:35:58  willuhn
 * @N Ergonomischere Verwaltung der Plugins
 *
 **********************************************************************/