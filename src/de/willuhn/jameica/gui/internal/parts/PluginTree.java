/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.internal.action.PluginDownload;
import de.willuhn.jameica.gui.internal.menus.PluginListMenu;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.PluginData;
import de.willuhn.jameica.update.PluginGroup;
import de.willuhn.jameica.update.Repository;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung der Liste mit den Gruppen und Plugins eines Repositories.
 */
public class PluginTree extends TreePart
{
  private static I18N i18n = Application.getI18n();
  private DecimalFormat formatter = (DecimalFormat) NumberFormat.getNumberInstance(Locale.getDefault());
  
  private List<SizeFetcher> fetchers = null;
  
  /**
   * ct.
   * @param repository das Repository.
   * @throws ApplicationException
   */
  public PluginTree(Repository repository) throws ApplicationException
  {
    super(repository.getPluginGroups(),new PluginDownload());
    init();
  }

  /**
   * ct.
   * Wird fuer die Liste von Updates verwendet.
   * @param plugins Liste der anzuzeigenden Plugins.
   * @throws ApplicationException
   */
  public PluginTree(List<PluginData> plugins) throws ApplicationException
  {
    super(plugins,new PluginDownload());
    init();
  }

  /**
   * Initialisiert den Tree.
   * @throws ApplicationException
   */
  private void init() throws ApplicationException
  {
    this.fetchers = new LinkedList<SizeFetcher>();

    this.formatter.applyPattern("##0.00");

    addColumn(i18n.tr("Plugin"),"name");
    addColumn(i18n.tr("Beschreibung"),"description");
    addColumn(i18n.tr("Verfügbare Version"),"availableVersion");
    addColumn(i18n.tr("Installierte Version"),"installedVersion");
    addColumn(i18n.tr("Größe"),"dummy",null,false,Column.ALIGN_RIGHT);
    
    setContextMenu(new PluginListMenu());
    setMulti(false);
    setRememberColWidths(true);
    setRememberOrder(true);
    
    setFormatter(new TreeFormatter() {
      /**
       * @see de.willuhn.jameica.gui.formatter.TreeFormatter#format(org.eclipse.swt.widgets.TreeItem)
       */
      public void format(TreeItem item)
      {
        // Wir markieren nicht installierbare Plugins grau
        try
        {
          Object data = item.getData();
          if (data == null || !(data instanceof PluginData))
            return;
          
          PluginData pd = (PluginData) data;
          
          if (pd.isInstalledVersion())
            item.setForeground(Color.SUCCESS.getSWTColor());
          else if (pd.isInstallable())
          {
            item.setForeground(Color.FOREGROUND.getSWTColor());
          }
          else
            item.setForeground(Color.COMMENT.getSWTColor());

          fetchers.add(new SizeFetcher(pd,item));
        }
        catch (Exception e)
        {
          Logger.error("unable to check if plugin is installable",e);
        }
      }
    });
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.TreePart#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
    
    // Jetzt laden wir noch die Download-Groessen im Hintergrund
    try
    {
      for (SizeFetcher sf:this.fetchers)
      {
        sf.start();
      }
    }
    finally
    {
      this.fetchers.clear();
    }
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.TreePart#getChildren(java.lang.Object)
   */
  @Override
  protected List getChildren(Object o)
  {
    if (o instanceof PluginGroup)
      return ((PluginGroup)o).getPlugins();
    
    return null;
  }

  /**
   * Mit dem aktualisieren wir die Download-Groessen im Hintergrund, damit
   * die GUI schneller aufgebaut wird.
   */
  private class SizeFetcher extends Thread
  {
    private PluginData plugin = null;
    private TreeItem item     = null;

    /**
     * ct.
     * @param plugin das Plugin.
     * @param item das TreeItem.
     */
    public SizeFetcher(PluginData plugin, TreeItem item)
    {
      super("size-fetcher");
      this.plugin = plugin;
      this.item   = item;
    }

    /**
     * @see java.lang.Thread#run()
     */
    public void run()
    {
      if (this.item == null || this.item.isDisposed())
        return;

      try
      {
        final long size = this.plugin.getSize();

        GUI.getDisplay().syncExec(new Runnable() {
          public void run()
          {
            try
            {
              if (item.isDisposed()) // nochmal zur Sicherheit, falls das inzwischen passiert ist
                return;
              
              if (size == -1) // Server hat keine Groesse geliefert 
              {
                item.setText(4,i18n.tr("n/a"));
                return;
              }
              double d = size / 1024d / 1024d;
              item.setText(4,i18n.tr("{0} MB",formatter.format(d)));
            }
            catch (Exception e)
            {
              Logger.error("unable to determine download size",e);
            }
          }
        });
      }
      catch (Exception e)
      {
        Logger.error("unable to determine download size",e);
      }
    }
  }
}


/**********************************************************************
 * $
 **********************************************************************/
