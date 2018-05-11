/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.internal.action.PluginDetails;
import de.willuhn.jameica.gui.internal.action.PluginDownload;
import de.willuhn.jameica.gui.internal.action.PluginUnInstall;
import de.willuhn.jameica.gui.internal.action.PluginUpdate;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.messaging.PluginCacheMessageConsumer;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.Version;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.PluginData;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt die Kurz-Infos eines Plugins inclusive Buttons fuer die Verwaltung
 * des Plugins an.
 */
public class PluginDetailPart extends InfoPanel
{
  private final static I18N i18n = Application.getI18n();
  
  /**
   * Legt fest, um welche Art von Plugin-Info es sich handelt.
   */
  public enum Type
  {
    /**
     * Installiertes Plugin.
     */
    INSTALLED,
    
    /**
     * Verfuegbares Plugin.
     */
    AVAILABLE,
    
    /**
     * Verfuegbares Update eines installierten Plugins.
     */
    UPDATE
  }
  
  private SelectInput version = null;
  private List<PluginData> plugins = null;
  private Manifest manifest = null;
  private Type type = null;

  /**
   * ct.
   * @param mf das Manifest des Plugins.
   * @param type der Typ des Plugins.
   */
  public PluginDetailPart(Manifest mf, Type type)
  {
    this(mf,null,type);
  }

  /**
   * ct.
   * @param mf das Manifest des Plugins.
   * @param plugins Liste der gefundenen Plugins.
   * @param type der Typ des Plugins.
   */
  public PluginDetailPart(Manifest mf, List<PluginData> plugins, Type type)
  {
    this.plugins = plugins;
    this.manifest = mf;
    this.type = type;
    this.setUrl(this.manifest.getHomepage());
    this.setBorder(Boolean.TRUE);
    
    String icon = this.manifest.getIcon();
    if (icon == null || (type != null && type == Type.AVAILABLE))
      icon = "package-x-generic-medium.png";
    this.setIcon(icon);
    this.setText(this.manifest.getDescription());
    
    if (this.type == Type.INSTALLED)
    {
      if (!this.manifest.isInstalled())
        this.setForeground(Color.COMMENT);
    }
    
    // Haben wir einen Init-Error bei dem Plugin?
    Throwable error = Application.getPluginLoader().getInitErrors().get(manifest);

    String title = this.manifest.getName();
    if (!manifest.isInstalled() && (this.type == Type.INSTALLED))
    {
      String text = "";
      if (error != null)
        text = error.getMessage(); // Hat einen Initialisierungsfehler
      else if (PluginCacheMessageConsumer.getCache().containsKey(mf.getName()))
        text = i18n.tr("Neustart erforderlich"); // Ist im Cache und hat keinen Fehler. Dann wurde es gerade erst installiert
      else
        text = i18n.tr("Nicht installiert");
      title = i18n.tr("{0} ({1})",title, text);
    }
    this.setTitle(title);
    
    if (this.type != Type.AVAILABLE || this.plugins == null || this.plugins.size() < 2)
      this.setComment(i18n.tr("Version {0}",this.manifest.getVersion().toString()));
    
    String builddate   = this.manifest.getBuildDate();
    String buildnumber = this.manifest.getBuildnumber();
    if (builddate != null && builddate.length() > 0 && buildnumber != null && buildnumber.length() > 0)
      this.setTooltip(i18n.tr("Build-Datum {0}\nBuildnummer {1}",builddate,buildnumber));
    
    // Buttons zum Oeffnen, Deinstallieren, Aktualisieren
    
    // Checken, ob es installiert/deinstalliert werden kann
    if (this.type == Type.INSTALLED)
    {
      Button open   = new Button(i18n.tr("Öffnen..."),new PluginDetails(),this.manifest,false,"document-open.png");
      Button update = new Button(i18n.tr("Plugin aktualisieren..."),new PluginUpdate(),this.manifest,false,"emblem-package.png");

      // Update und oeffnen gibt es nicht bei neuen Installationen
      open.setEnabled(manifest.isInstalled());
      update.setEnabled(manifest.isInstalled() || error != null); // Update auch bei Fehler erlauben
      this.addButton(open);
      this.addButton(update);

      Button delete = new Button(i18n.tr("Plugin löschen..."),new PluginUnInstall(),this.manifest,false,"user-trash-full.png");
      try
      {
        Application.getPluginLoader().canUnInstall(this.manifest);
      }
      catch (ApplicationException ae) {
        delete.setEnabled(false);
      }
      this.addButton(delete);
    }
    else if (this.type == Type.AVAILABLE && this.plugins != null && this.plugins.size() > 0)
    {
      Action download = new PluginDownload(){
        /**
         * @see de.willuhn.jameica.gui.internal.action.PluginDownload#handleAction(java.lang.Object)
         */
        @Override
        public void handleAction(Object context) throws ApplicationException
        {
          super.handleAction(getSelectedVersion());
        }
      };
      Button install = new Button(i18n.tr("Installieren..."),download,null,false,"document-save.png");
      this.addButton(install);
    }
    else if (this.type == Type.UPDATE && this.plugins != null && this.plugins.size() > 0)
    {
      Action download = new PluginDownload(){
        /**
         * @see de.willuhn.jameica.gui.internal.action.PluginDownload#handleAction(java.lang.Object)
         */
        @Override
        public void handleAction(Object context) throws ApplicationException
        {
          super.handleAction(getSelectedVersion());
        }
      };
      Button install = new Button(i18n.tr("Aktualisieren..."),download,null,false,"document-save.png");
      this.addButton(install);
    }
  }
  
  /**
   * Liefert die ausgewaehlte Plugin-Version.
   * @return die ausgewaehlte Plugin-Version.
   */
  private PluginData getSelectedVersion()
  {
    if (this.plugins == null || this.plugins.size() == 0)
      return null;
    
    if (this.plugins.size() == 1)
      return this.plugins.get(0);
    
    Version version = (Version) this.getVersion().getValue();
    for (PluginData p:this.plugins)
    {
      if (p.getAvailableVersion().equals(version))
        return p;
    }
    
    Logger.warn("unable to determine selected version, using first found");
    return this.plugins.get(0);
  }
  
  /**
   * Liefert ein Auswahlfeld fuer die zu installierende Version.
   * @return ein Auswahlfeld fuer die zu installierende Version.
   */
  private SelectInput getVersion()
  {
    if (this.version != null)
      return this.version;
    
    List<Version> versions = new ArrayList<Version>();
    if (this.plugins != null)
    {
      for (PluginData plugin:plugins)
      {
        versions.add(plugin.getAvailableVersion());
      }
    }
    Collections.sort(versions);
    Collections.reverse(versions); // Die neueste Version oben
    this.version = new SelectInput(versions,null);
    this.version.setName(i18n.tr("Zu installierende Version"));
    return this.version;
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.InfoPanel#extend(de.willuhn.jameica.gui.parts.InfoPanel.DrawState, org.eclipse.swt.widgets.Composite, java.lang.Object)
   */
  @Override
  public Composite extend(DrawState state, Composite comp, Object context)
  {
    if (state != null && state == DrawState.BUTTONS_BEFORE && this.plugins != null && this.plugins.size() > 1)
    {
      Composite newComp = new Composite(comp,SWT.NONE);
      newComp.setBackground(comp.getBackground());
      newComp.setBackgroundMode(SWT.INHERIT_FORCE);
      newComp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
      newComp.setLayout(new GridLayout(2,false));
      
      SimpleContainer c = new SimpleContainer(newComp);
      c.addInput(this.getVersion());
      return newComp;
    }
    return super.extend(state, comp, context);
  }
}
