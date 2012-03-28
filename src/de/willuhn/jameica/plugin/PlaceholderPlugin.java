/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/PlaceholderPlugin.java,v $
 * $Revision: 1.1 $
 * $Date: 2012/03/28 22:28:07 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.plugin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Seit Version 2.3 (2012-03-21) unterstuetzt Jameica auch Plugins, die
 * keinen eigenen Java-Code mitbringen sondern z.Bsp. nur Jars oder
 * Javascripts. Im Manifest dieser Plugins kann das Attribut "class"
 * des XML-Elements "plugin" dann einfach weggelassen werden.
 * Da diese Plugins dennoch eine Klasse bereitstellen muessen, die
 * von <code>AbstractPlugin</code> abgeleitet ist, wird dieses PlaceholderPlugin
 * verwendet. Es erzeugt via Reflection-Proxy dynamisch generierte
 * Klassen, die von AbstractPlugin ableiten.
 */
public class PlaceholderPlugin implements Plugin
{
  private PluginResources res = new PluginResources(this);
  private Manifest manifest = null;
  
  /**
   * ct.
   * @param manifest
   */
  private PlaceholderPlugin(Manifest manifest)
  {
    this.manifest = manifest;
  }
  
  /**
   * Erzeugt eine neue generische Platzhalter-Instanz, die von AbstractPlugin abgeleitet ist.
   * @param manifest das Manifest des Platzhalter-Plugins.
   * @return die neue Plugin-Instanz.
   */
  public static Plugin createInstance(Manifest manifest)
  {
    InvocationHandler handler = new PlaceholderInvocationHandler(new PlaceholderPlugin(manifest));
    return (Plugin) Proxy.newProxyInstance(manifest.getClassLoader(),new Class[]{Plugin.class},handler);
  }
  
  /**
   * @see de.willuhn.jameica.plugin.Plugin#getResources()
   */
  public PluginResources getResources()
  {
    return this.res;
  }

  /**
   * @see de.willuhn.jameica.plugin.Plugin#getManifest()
   */
  public Manifest getManifest()
  {
    return this.manifest;
  }

  /**
   * @see de.willuhn.jameica.plugin.Plugin#init()
   */
  public void init() throws ApplicationException
  {
    Application.getMessagingFactory().getMessagingQueue("plugin." + this.manifest.getName() + ".init").sendSyncMessage(new QueryMessage(this));
  }

  /**
   * @see de.willuhn.jameica.plugin.Plugin#install()
   */
  public void install() throws ApplicationException
  {
    Application.getMessagingFactory().getMessagingQueue("plugin." + this.manifest.getName() + ".install").sendSyncMessage(new QueryMessage(this));
  }

  /**
   * @see de.willuhn.jameica.plugin.Plugin#update(de.willuhn.jameica.plugin.Version)
   */
  public void update(Version oldVersion) throws ApplicationException
  {
    Application.getMessagingFactory().getMessagingQueue("plugin." + this.manifest.getName() + ".update").sendSyncMessage(new QueryMessage(new Object[]{this,oldVersion}));
  }

  /**
   * @see de.willuhn.jameica.plugin.Plugin#shutDown()
   */
  public void shutDown()
  {
    Application.getMessagingFactory().getMessagingQueue("plugin." + this.manifest.getName() + ".shutdown").sendSyncMessage(new QueryMessage(this));
  }

  /**
   * @see de.willuhn.jameica.plugin.Plugin#uninstall(boolean)
   */
  public void uninstall(boolean deleteUserData) throws ApplicationException
  {
    Application.getMessagingFactory().getMessagingQueue("plugin." + this.manifest.getName() + ".uninstall").sendSyncMessage(new QueryMessage(new Object[]{this,deleteUserData}));
  }


  /**
   * Implementiert die Funktionen aus AbstractPlugin.
   */
  private static class PlaceholderInvocationHandler implements InvocationHandler 
  {
    private Plugin redirect = null;
    
    /**
     * ct.
     * @param redirect
     */
    private PlaceholderInvocationHandler(Plugin redirect)
    {
      this.redirect = redirect;
    }
    
    /**
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
      return BeanUtil.invoke(redirect,method.getName(),args);
    }
  }
}



/**********************************************************************
 * $Log: PlaceholderPlugin.java,v $
 * Revision 1.1  2012/03/28 22:28:07  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 **********************************************************************/