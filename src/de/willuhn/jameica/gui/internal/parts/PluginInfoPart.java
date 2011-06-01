/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/Attic/PluginInfoPart.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/06/01 21:20:02 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.internal.action.PluginDetails;
import de.willuhn.jameica.gui.internal.action.PluginInstall;
import de.willuhn.jameica.gui.internal.action.PluginUnInstall;
import de.willuhn.jameica.gui.internal.action.Program;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt die Kurz-Infos eines Plugins inclusive Buttons fuer die Verwaltung
 * des Plugins an.
 */
public class PluginInfoPart implements Part
{
  private Manifest manifest = null;
  private Composite comp = null;
  
  /**
   * ct.
   * @param mf das Manifest des Plugins.
   */
  public PluginInfoPart(Manifest mf)
  {
    this.manifest = mf;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    // Wir wurden schon gezeichnet.
    if (this.comp != null)
      return;
    
    AbstractPlugin plugin = Application.getPluginLoader().getPlugin(this.manifest.getPluginClass());
    boolean freshInstall = plugin == null;

    I18N i18n = Application.getI18n();

    org.eclipse.swt.graphics.Color white = GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE);
    org.eclipse.swt.graphics.Color comment = Color.COMMENT.getSWTColor();
    
    // 2-spaltige Anzeige. Links das Icon, rechts Eigenschaften und Buttons
    this.comp = new Composite(parent,SWT.BORDER);
    this.comp.setBackground(white);
    this.comp.setBackgroundMode(SWT.INHERIT_FORCE);
    this.comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    this.comp.setLayout(new GridLayout(2,false));
    
    final String link = this.manifest.getHomepage();
    
    // Linke Spalte mit dem Icon
    {
      int rows = 4;
      if (link != null && link.length() > 0) rows++;
      GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
      gd.verticalSpan = rows;
      Label icon = new Label(this.comp,SWT.NONE);
      icon.setBackground(white);
      icon.setLayoutData(gd);
      
      String name = manifest.getIcon();
      if (name != null && name.length() > 0 && plugin != null)
        icon.setImage(SWTUtil.getImage(name,plugin.getResources().getClassLoader()));
      else
        icon.setImage(SWTUtil.getImage("package-x-generic-medium.png"));
    }
    
    // Rechte Spalte mit den Eigenschaften
    {
      // Name
      {
        Label title = new Label(this.comp,SWT.NONE);
        title.setBackground(white);
        title.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        title.setFont(Font.H2.getSWTFont());
        String text = this.manifest.getName();
        if (freshInstall) 
        {
          title.setForeground(comment);
          text = i18n.tr("{0} (Neustart erforderlich)",text);
        }
        title.setText(text);
      }
      
      // Beschreibung
      {
        Label desc = new Label(this.comp,SWT.NONE);
        desc.setBackground(white);
        if (freshInstall) desc.setForeground(comment);
        desc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        desc.setText(this.manifest.getDescription());
      }
      
      // Link
      if (link != null && link.length() > 0)
      {
        Link l = new Link(this.comp,SWT.NONE);
        l.setBackground(white);
        l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (freshInstall) l.setForeground(comment);
        l.setText("<A>" + link + "</A>");
        l.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e)
          {
            try
            {
              new Program().handleAction(link);
            }
            catch (ApplicationException ae)
            {
              Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
            }
          }
        });
      }

      // Versionsnummer
      {
        Label version = new Label(this.comp,SWT.NONE);
        version.setBackground(white);
        version.setFont(Font.SMALL.getSWTFont());
        version.setForeground(Color.COMMENT.getSWTColor());
        version.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        version.setText(i18n.tr("Version {0}",this.manifest.getVersion().toString()));
        String builddate   = this.manifest.getBuildDate();
        String buildnumber = this.manifest.getBuildnumber();
        if (builddate != null && builddate.length() > 0 && buildnumber != null && buildnumber.length() > 0)
          version.setToolTipText(i18n.tr("Build-Datum {0}\nBuildnummer {1}",builddate,buildnumber));
      }
      
      // Buttons zum Oeffnen, Deinstallieren, Aktualisieren
      Button open   = new Button(i18n.tr("Öffnen..."),new PluginDetails(),this.manifest,false,"document-open.png");
      Button update = new Button(i18n.tr("Plugin aktualisieren..."),new PluginInstall(),null,false,"emblem-package.png");
      Button delete = new Button(i18n.tr("Plugin löschen..."),new PluginUnInstall(),this.manifest,false,"user-trash-full.png");

      // Update und oeffnen gibt es nicht bei neuen Installationen
      open.setEnabled(!freshInstall);
      update.setEnabled(!freshInstall);

      // Checken, ob es installiert/deinstalliert werden kann
      try
      {
        Application.getPluginLoader().canUnInstall(this.manifest);
      }
      catch (ApplicationException ae) {
        update.setEnabled(false);
        delete.setEnabled(false);
      }
      
      ButtonArea buttons = new ButtonArea();
      buttons.addButton(open);
      buttons.addButton(update);
      buttons.addButton(delete);
      
      buttons.paint(this.comp);

    }
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
 * $Log: PluginInfoPart.java,v $
 * Revision 1.2  2011/06/01 21:20:02  willuhn
 * @N Beim Deinstallieren die Navi und Menupunkte des Plugins deaktivieren
 * @N Frisch installierte aber noch nicht aktive Plugins auch dann anzeigen, wenn die View verlassen wird
 *
 * Revision 1.1  2011-06-01 17:35:58  willuhn
 * @N Ergonomischere Verwaltung der Plugins
 *
 **********************************************************************/