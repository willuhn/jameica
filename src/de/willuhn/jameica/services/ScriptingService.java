/**********************************************************************
 * 
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.io.FileWatch;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.gui.internal.ext.ScriptingSettingsView;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.messaging.BootMessage;
import de.willuhn.jameica.messaging.BootMessageConsumer;
import de.willuhn.jameica.messaging.InvokeScriptMessageConsumer;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.SystemMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Platform;
import de.willuhn.jameica.system.Settings;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Interface fuer den Scripting-Service.
 */
public class ScriptingService implements Bootable
{
  /**
   * Die Queue, die nach dem Hinzufuegen eines Scripts benachrichtigt wird.
   */
  public final static String QUEUE_ADDED   = "jameica.scripting.added";
  
  /**
   * Die Queue, die nach dem Entfernen eines Scripts benachrichtigt wird.
   */
  public final static String QUEUE_REMOVED = "jameica.scripting.removed";
  
  private Settings settings = new Settings(ScriptingService.class);
  
  private ScriptEngine engine      = null;
  private MessageConsumer mcInvoke = null;
  private List<File> files         = null;
  private Extension settingsExt    = null;
  private Events events            = new Events();
  
  // Wir merken uns hier unsere Boot-Messages, damit wir die nicht bei jedem Service-Reload
  // (was z.Bsp. beim Klick auf "Speichern" in den Settings passiert) immer wieder neu
  // hinzufuegen.
  private List<BootMessage> messages = new ArrayList<BootMessage>();

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class<Bootable>[] depends()
  {
    return new Class[]{BeanService.class,MessagingService.class};
  }
  
  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    Application.getMessagingFactory().registerMessageConsumer(new InitMessageConsumer());
  }
  
  /**
   * Initialisiert den Scripting-Service.
   */
  private void init()
  {
    this.migrate();
    this.events.clear();

    // 1. Script-Engine laden
    if (this.engine == null)
    {
      ScriptEngineManager factory = new ScriptEngineManager();
      this.engine = factory.getEngineByName("JavaScript");
    }
    if (this.engine == null)
    {
      Logger.warn("java does not support scripting (RhinoScript)");
      return;
    }
    this.engine.put("events",this.events);

    // 2. Settings-Extension registrieren
    if (this.settingsExt == null)
    {
      BeanService beanService = Application.getBootLoader().getBootable(BeanService.class);
      this.settingsExt = beanService.get(ScriptingSettingsView.class);
      ExtensionRegistry.register(this.settingsExt,de.willuhn.jameica.gui.internal.views.Settings.class.getName());
    }

    // 3. Ggf. gespeicherte Fehlermeldungen wieder loeschen
    if (this.messages.size() > 0)
    {
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      BootMessageConsumer consumer = service.get(BootMessageConsumer.class);
      for (BootMessage msg:this.messages)
      {
        consumer.getMessages().remove(msg);
      }
      this.messages.clear();
    }
    
    // 4. Vom Benutzer registrierte Scripts ausfuehren.
    
    this.files = this.getScripts();
    for (final File f:this.files)
    {
      if (!f.isFile() || !f.canRead())
      {
        I18N i18n = Application.getI18n();
        BootMessage msg = new BootMessage(i18n.tr("Script {0} nicht lesbar",f.getAbsolutePath()));
        msg.setTitle(i18n.tr("Script nicht lesbar"));
        msg.setIcon("dialog-warning-large.png");
        msg.addButton(new Button(i18n.tr("Script konfigurieren..."),new de.willuhn.jameica.gui.internal.action.Settings(),Application.getI18n().tr("Scripting")));
        Application.getMessagingFactory().getMessagingQueue("jameica.boot").queueMessage(msg); // muessen wir queuen, weil der Consumer noch nicht da ist
        
        this.messages.add(msg); // merken wir uns
        
        continue;
      }
      eval(f);
      FileWatch.addFile(f,new Observer() {
        public void update(Observable o, Object arg)
        {
          Logger.info("auto-reloading script " + f);
          eval(f);
        }
      });
    }
    
    // 3. Message-Consumer fuer Invoke-Aufrufe
    this.mcInvoke = new InvokeScriptMessageConsumer();
    Application.getMessagingFactory().getMessagingQueue("jameica.scripting").registerMessageConsumer(this.mcInvoke);
  }
  
  /**
   * Hilfsklasse zum Migrieren der Settings vom Plugin zu Jameica-intern.
   */
  private void migrate()
  {
    if (settings.getString("migrated",null) != null)
      return;
    
    File file = new File(Application.getConfig().getConfigDir(),"de.willuhn.jameica.scripting.Plugin.properties");
    if (file.exists() && file.canRead())
    {
      Logger.info("migrating scripting-settings from " + file);
      de.willuhn.util.Settings source = new de.willuhn.util.Settings(null,file);
      String[] keys = source.getAttributes();
      for (String s:keys)
      {
        String value = source.getString(s,null);
        Logger.info("  " + s + "=" + value);
        settings.setAttribute(s,value);
      }
      settings.setAttribute("migrated",DateUtil.DEFAULT_FORMAT.format(new Date()));
      Logger.info("deleting " + file);
      if (!file.delete())
      {
        Logger.warn("could not delete " + file);
        file.deleteOnExit();
      }
    }
  }
  
  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    try
    {
      if (this.files != null)
      {
        for (File f:this.files)
        {
          FileWatch.removeFile(f);
        }
      }
    }
    finally
    {
      this.engine = null;
      this.files = null;
      Application.getMessagingFactory().getMessagingQueue("jameica.scripting").unRegisterMessageConsumer(this.mcInvoke);
    }
  }
  
  /**
   * Startet den Scripting-Service neu, damit die Scripts neu geladen werden.
   */
  public void reload()
  {
    try
    {
      Logger.info("restarting scripting service");
      this.shutdown();
      this.init();
    }
    catch (Exception e)
    {
      Logger.error("unable to restart scripting service",e);
    }
  }
  

  
  /**
   * Fuehrt die Script-Datei aus.
   * @param f die auszufuehrende Script-Datei.
   */
  private void eval(File f)
  {
    Logger.info("executing script " + f);
    if (!f.exists() || !f.canRead() || !f.isFile())
    {
      Logger.warn("  not found or not readable, skipping");
      return;
    }
    Reader r = null;
    try
    {
      String encoding = settings.getString("script.encoding",null);
      if (encoding != null)
        r = new BufferedReader(new InputStreamReader(new FileInputStream(f),encoding));
      else
        r = new BufferedReader(new InputStreamReader(new FileInputStream(f))); // Der Reader wirft leider eine NPE, wenn man ihm NULL als charset gibt
      this.getEngine().eval(r);
    }
    catch (Exception e)
    {
      Logger.error("error while loading script",e);
    }
    finally
    {
      IOUtil.close(r);
    }
  }

  /**
   * Liefert die Script-Engine.
   * @return die Script-Engine.
   */
  public ScriptEngine getEngine()
  {
    return this.engine;
  }

  /**
   * Liefert die Namen der auszufuehrenden Javascript-Funktionen fuer das Event.
   * @param event das Event.
   * @return die auszufuehrenden JS-Funktion oder NULL, wenn keine definiert sind.
   */
  public List<String> getFunction(String event)
  {
    return this.events.get(event);
  }
  
  /**
   * Liefert die Liste der vom User registrierten Scripts.
   * @return Liste der vom User registrierten Scripts.
   * Niemals NULL sondern hoechstens eine leere Liste.
   */
  public List<File> getScripts()
  {
    Platform platform = Application.getPlatform();
    String[] list = settings.getList("scripts",new String[0]);
    List<File> files = new ArrayList<File>();
    for (String s:list)
    {
      if (s == null || s.length() == 0)
        continue;
      try
      {
        // Wir zeigen die Scripts mit absoluten Pfaden an - auch wenn sie nur relativ gespeichert sind
        files.add(new File(platform.toAbsolute(s)));
      }
      catch (Exception e)
      {
        Logger.error("unable to determine canonical path of script " + s,e);
      }
    }
    
    Collections.sort(files); // Sortiert sieht schoener aus
    
    return files;
  }
  
  /**
   * Prueft, ob das angegebene Script bereits hinzugefuegt wurde.
   * @param file das zu pruefende Script.
   * @return true, wenn es bereits vorhanden ist.
   * @throws IOException
   */
  public boolean contains(File file) throws IOException
  {
    Platform platform = Application.getPlatform();
    File test = new File(platform.toAbsolute(file.getAbsolutePath()));
    
    List<File> existing = this.getScripts();
    for (File f:existing)
    {
      if (f.equals(test))
        return true;
    }
    
    return false;
  }
  
  /**
   * Fuegt ein neues Script hinzu.
   * @param file das hinzuzufuegende Script.
   * @throws ApplicationException wenn die Script-Datei bereits vorhanden ist.
   */
  public void addScript(File file) throws ApplicationException
  {
    if (file == null)
      return;
    
    // Checken, ob wir das Script schon haben
    try
    {
      if (contains(file))
        throw new ApplicationException(Application.getI18n().tr("Script-Datei {0} ist bereits registriert",file.getAbsolutePath()));
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to convert path " + file + " to canonical form",e);
      throw new ApplicationException(Application.getI18n().tr("Fehler beim Import der Script-Datei {0}: {1}",new String[]{file.getAbsolutePath(),e.getMessage()}));
    }
    
    List<File> existing = getScripts();
    Logger.info("adding script " + file);
    existing.add(file);
    
    Platform platform = Application.getPlatform();
    List<String> list = new ArrayList<String>();
    for (File f:existing)
    {
      list.add(platform.toRelative(f.getAbsolutePath()));
    }
    settings.setAttribute("scripts",list.toArray(new String[list.size()]));
    
    reload();

    // Via Messaging Bescheid geben, dass wir das Script hinzugefuegt haben.
    Application.getMessagingFactory().getMessagingQueue(QUEUE_ADDED).sendMessage(new QueryMessage(file));
  }
  
  /**
   * Entfernt das Script.
   * @param file das zu entfernende Script.
   */
  public void removeScript(File file)
  {
    if (file == null)
      return;
    
    List<File> existing = this.getScripts();
    List<String> newList  = new ArrayList<String>();

    for (File f:existing)
    {
      try
      {
        if (f.getCanonicalPath().equals(file.getCanonicalPath()))
          continue; // Das ist das zu entfernende
        newList.add(f.getAbsolutePath());
      }
      catch (Exception e)
      {
        Logger.error("unable to convert path " + file + " to canonical form",e);
      }
    }

    if (existing.size() == newList.size()) // keine Aenderungen
      return;
    
    Logger.info("removing script " + file);
    settings.setAttribute("scripts",newList.toArray(new String[newList.size()]));

    reload();

    // Via Messaging Bescheid geben, dass wir das Script entfernt haben.
    Application.getMessagingFactory().getMessagingQueue(QUEUE_REMOVED).sendMessage(new QueryMessage(file));
  }
  
  /**
   * Hilfsklasse zum Mappen der Events auf die JS-Funktionen.
   */
  public class Events
  {
    private Map<String,List<String>> mapping = new HashMap<String,List<String>>();
    
    /**
     * Liefert eine Liste der JS-Funktionen fuer das Event.
     * @param event das Event.
     * @return Liste der Funktionen oder NULL, wenn keine definiert sind.
     */
    public List<String> get(String event)
    {
      return this.mapping.get(event);
    }
    
    /**
     * Fuegt die JS-Funktion dem Event hinzu.
     * @param event das Event.
     * @param function die Funktion.
     */
    public void add(String event, String function)
    {
      List<String> functions = get(event);
      if (functions == null)
      {
        functions = new ArrayList<String>();
        this.mapping.put(event,functions);
      }
      
      // Nur, wenn wir sie noch nicht haben. Andernfalls wuerden
      // wir die Funktion immer wieder erneut registrieren
      if (!functions.contains(function))
      {
        Logger.info("registering script function \"" + function + "\" for event \"" + event + "\"");
        functions.add(function);
      }
    }
    
    /**
     * Leert alle Mappings.
     */
    public void clear()
    {
      this.mapping.clear();
    }
  }
  
  /**
   * Fuehrt die Initialisierung des Scripting-Service aus, wenn das System gebootet wurde.
   */
  private class InitMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }
    
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{SystemMessage.class};
    }
    
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      SystemMessage msg = (SystemMessage) message;
      
      if (msg.getStatusCode() == SystemMessage.SYSTEM_STARTED)
        init();
    }
  }

}
