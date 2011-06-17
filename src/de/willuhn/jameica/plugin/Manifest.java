/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/Manifest.java,v $
 * $Revision: 1.33 $
 * $Date: 2011/06/17 15:55:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.jar.JarFile;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;
import de.willuhn.jameica.gui.MenuItem;
import de.willuhn.jameica.gui.MenuItemXml;
import de.willuhn.jameica.gui.NavigationItem;
import de.willuhn.jameica.gui.NavigationItemXml;
import de.willuhn.jameica.plugin.PluginSource.Type;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Enthaelt die Manifest-Informationen des Plugins aus plugin.xml.
 */
public class Manifest implements Comparable
{
  private File manifest                 = null;

  private IXMLElement root              = null;
  private String[] cfInclude            = null;
  private NavigationItem navi           = null;
  private MenuItem menu                 = null;
  
  private AbstractPlugin pluginInstance = null;
  
  private Type source                   = null;
  
  private boolean isInstalled           = false;
  
  private String buildnumber            = "";
  private String builtdate              = "";

  /**
   * ct.
   * @param manifest die Datei mit dem Manifest.
   * @throws Exception
   */
  public Manifest(File manifest) throws Exception
  {
    if (manifest == null)
    throw new IOException("no manifest (plugin.xml) given");

    if (!manifest.exists() || !manifest.canRead())
      throw new IOException("manifest " + manifest.getAbsolutePath() + " not readable");

    this.manifest = manifest;
    read(new FileInputStream(manifest));

    Logger.info(getName() + " " + this.getVersion());
    Logger.info("  Directory  : " + this.getPluginDir());

    try
    {
      // Das ist nur geraten und eher fuer Debugging-Zwecke ;)
      JarFile jar = new JarFile(getPluginDir() + File.separator + getName() + ".jar");
      java.util.jar.Manifest mf = jar.getManifest();

      this.buildnumber = mf.getMainAttributes().getValue("Implementation-Buildnumber");
      this.builtdate   = mf.getMainAttributes().getValue("Built-Date");
      Logger.info("  Buildnumber: " + buildnumber);
      Logger.info("  Built-Date : " + builtdate);
    }
    catch (Exception e)
    {
      Logger.debug("unable to read jar manifest, running uncompressed within debugger?");
    }
  }

  /**
   * ct.
   * @param manifest InputStream mit dem Manifest.
   * @throws Exception
   */
  public Manifest(InputStream manifest) throws Exception
  {
    if (manifest == null)
      throw new IOException("no manifest (plugin.xml) given");
    read(manifest);
  }
  
  /**
   * Liest das Manifest ein.
   * @param is InputStream mit dem Manifest.
   * @throws Exception
   */
  private void read(InputStream is) throws Exception
  {
    IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
    parser.setReader(new StdXMLReader(is));
    root = (IXMLElement) parser.parse();
  }

  /**
   * Liefert das Verzeichnis, in dem sich das Plugin befindet.
   * @return das Installations-Verzeichnis.
   */
  public String getPluginDir()
  {
    if (this.manifest == null)
      return null;
    
    String s = this.manifest.getParent();
    if (s != null)
      return s;
    
    // Das File-Objekt wurde wahrscheinlich ohne Pfadangabe erzeugt.
    // Also ermitteln wir den Pfad.
    try
    {
      File canonical = this.manifest.getCanonicalFile();
      return canonical.getParent();
    }
    catch (IOException ioe)
    {
      Logger.error("unable to determine parent directory of " + this.manifest.getAbsolutePath() + ", using \".\"",ioe);
      return ".";
    }
  }
  
	/**
   * Liefert die Versionsnummer.
   * @return Versionsnummer.
   */
  public Version getVersion()
	{
    try
    {
      return new Version(root.getAttribute("version","1.0"));
    }
    catch (Exception e)
    {
      return new Version();
    }
	}
  
  /**
   * Liefert die Build-Nummer, insofern sie ermittelbar ist.
   * Da die Nummer nur im Manifest des Jars steht, kann sie nur dann
   * ermittelt werden, wenn die Anwendung in ein solches deployed wurde
   * und der entsprechende Parameter im Manifest des JARs existiert.
   * @return Build-Number.
   */
  public final String getBuildnumber()
  {
    return this.buildnumber;
  }

  /**
   * Liefert das Build-Datum, insofern es ermittelbar ist.
   * Da das Datum nur im Manifest des Jars steht, kann es nur dann
   * ermittelt werden, wenn die Anwendung in ein solches deployed wurde
   * und der entsprechende Parameter im Manifest des JARs existiert.
   * @return Build-Datum.
   */
  public final String getBuildDate()
  {
    return this.builtdate;
  }


	/**
   * Liefert den Namen der Komponente.
   * @return Name.
   */
  public String getName()
	{
    return root.getAttribute("name",null);
	}
	
  /**
   * Liefert den Klassen-Name des Plugins.
   * @return Klassen-Name des Plugins.
   */
  public String getPluginClass()
  {
    return this.root.getAttribute("class",null);
  }
  
  /**
   * Liefert true, wenn das Plugin ueber den globalen Classloader von Jameica geladen werden soll.
   * @return true, wenn es ueber den globalen Classloader geladen werden soll.
   * Andernfalls erhaelt es einen exlusiven Classloader.
   * Default: True
   */
  public boolean isShared()
  {
    return "true".equalsIgnoreCase(this.root.getAttribute("shared","true"));
  }
  
  /**
   * Liefert die Beschreibung der Komponente.
   * @return Beschreibung.
   */
  public String getDescription()
	{
		IXMLElement desc = root.getFirstChildNamed("description");
		return desc == null ? null: desc.getContent();
	}
	
	/**
   * Liefert die Download-URL der Komponente.
   * @return Download-URL der Komponente.
   */
  public String getURL()
	{
		IXMLElement desc = root.getFirstChildNamed("url");
		return desc == null ? null: desc.getContent();
	}
	
  /**
   * Liefert die Homepage-URL der Komponente.
   * @return Homepage-URL der Komponente.
   */
  public String getHomepage()
  {
		IXMLElement desc = root.getFirstChildNamed("homepage");
		return desc == null ? null: desc.getContent();
  }

	/**
   * Liefert die Lizenz der Komponente.
   * @return Lizenz.
   */
  public String getLicense()
	{
		IXMLElement desc = root.getFirstChildNamed("license");
		return desc == null ? null: desc.getContent();
	}

  /**
   * Liefert das Icon des Plugins.
   * Sollte 64x64 Pixel gross sein.
   * @return das Icon oder NULL, wenn keines definiert ist.
   */
  public String getIcon()
  {
    IXMLElement desc = root.getFirstChildNamed("icon");
    return desc == null ? null : desc.getContent();
  }

  /**
   * Liefert das Menu der Komponente.
   * @return Menu.
   */
  public MenuItem getMenu()
  {
    if (this.menu != null)
      return this.menu;
    
    I18N i18n = Application.getI18n();
    
    // Mal schauen, ob das Plugin ein eigenes i18n hat.
    
    if (pluginInstance != null)
    {
      try
      {
        i18n = pluginInstance.getResources().getI18N();
      }
      catch (Exception e)
      {
        // ignore
      }
    }
    IXMLElement e = root.getFirstChildNamed("menu");
    this.menu = e == null ? null : new MenuItemXml(null,e,i18n);
    return this.menu;
  }

  /**
   * Liefert die Navigation der Komponente.
   * @return Menu.
   */
  public NavigationItem getNavigation()
  {
    if (this.navi != null)
      return this.navi;
    
    I18N i18n = Application.getI18n();
    
    // Mal schauen, ob das Plugin ein eigenes i18n hat.
    if (pluginInstance != null)
    {
      try
      {
        i18n = pluginInstance.getResources().getI18N();
      }
      catch (Exception e)
      {
        // ignore
      }
    }
    IXMLElement e = root.getFirstChildNamed("navigation");
    this.navi = e == null ? null : new NavigationItemXml(null,e,i18n);
    return this.navi;
  }

	/**
	 * Liefert eine Liste von Service-Desktriptoren zu diesem Plugin.
   * @return Liste aller Service-Deskriptoren aus der plugin.xml oder
   * <code>null</code> wenn keine definiert sind.
   */
  public ServiceDescriptor[] getServices()
	{
		IXMLElement services = root.getFirstChildNamed("services");
		if (services == null || !services.hasChildren())
			return new ServiceDescriptor[0]; // BUGZILLA 531

		Vector v = services.getChildrenNamed("service");
		ServiceDescriptor[] s = new ServiceDescriptor[v.size()];
		for (int i=0;i<v.size();++i)
		{
			s[i] = new ServiceDescriptorXml((IXMLElement)v.get(i));
		}
		return s;
	}

  /**
   * Liefert eine Liste von Pfaden, die in den Classfinder aufgenommen werden sollen.
   * @return Liste von Pfaden fuer den ClassFinder.
   */
  public String[] getClassFinderIncludes()
  {
    if (this.cfInclude != null)
      return this.cfInclude;

    IXMLElement finder = root.getFirstChildNamed("classfinder");
    if (finder == null || !finder.hasChildren())
      return new String[0];

    Vector v = finder.getChildrenNamed("include");
    this.cfInclude = new String[v.size()];
    for (int i=0;i<v.size();++i)
    {
      this.cfInclude[i] = ((IXMLElement)v.get(i)).getContent();
    }
    return this.cfInclude;
  }

  /**
   * Liefert eine Liste von Extension-Desktriptoren zu diesem Plugin.
   * @return Liste aller Extension-Deskriptoren aus der plugin.xml oder
   * <code>null</code> wenn keine definiert sind.
   */
  public ExtensionDescriptor[] getExtensions()
  {
    IXMLElement extensions = root.getFirstChildNamed("extensions");
    if (extensions == null || !extensions.hasChildren())
      return null;

    Vector v = extensions.getChildrenNamed("extension");
    ExtensionDescriptor[] s = new ExtensionDescriptor[v.size()];
    for (int i=0;i<v.size();++i)
    {
      s[i] = new ExtensionDescriptorXml((IXMLElement)v.get(i));
    }
    return s;
  }
  
  /**
   * Liefert eine Liste von Consumer-Desktriptoren zu diesem Plugin.
   * @return Liste aller Consumer-Deskriptoren aus der plugin.xml oder
   * <code>null</code> wenn keine definiert sind.
   */
  public ConsumerDescriptor[] getMessageConsumers()
  {
    IXMLElement consumers = root.getFirstChildNamed("messaging");
    if (consumers == null || !consumers.hasChildren())
      return null;

    Vector v = consumers.getChildrenNamed("consumer");
    ConsumerDescriptor[] s = new ConsumerDescriptor[v.size()];
    for (int i=0;i<v.size();++i)
    {
      s[i] = new ConsumerDescriptorXml((IXMLElement)v.get(i));
    }
    return s;
  }

  /**
   * Liefert die Versionsabhaengigkeit zu Jameica.
   * @return Die Abhaengigkeit zu einer bestimmten Jameica-Version.
   */
  public Dependency getJameicaDependency()
  {
    IXMLElement req = root.getFirstChildNamed("requires");
    return new Dependency("jameica", req == null ? null : req.getAttribute("jameica",null));
  }

  /**
   * Liste der Plugins, von denen dieses hier direkt abhaengig ist.
   * Es werden also nur die direkt abhaengigen Plugins geliefert, nicht jedoch
   * die indirekten Abhaengigkeiten.
   * Wenn die komplette Liste der Abhaengigkeiten ermittelt werden soll (also incl.
   * indirekter Abhaengigkeiten), dann muss "getDependencies()" verwendet werden.
   * @return  Liste von Abhaengigkeiten fuer dieses Plugin.
   * Die Funktion liefert nie NULL sondern hoechstens ein leeres Array.
   */
  public Dependency[] getDirectDependencies()
  {
    IXMLElement deps = root.getFirstChildNamed("requires");

    if (deps == null || !deps.hasChildren())
      return new Dependency[0];

    List<Dependency> found = new ArrayList<Dependency>();
    
    Vector v = deps.getChildrenNamed("import");
    for (int i=0;i<v.size();++i)
    {
      IXMLElement plugin = (IXMLElement) v.get(i);
      String name = plugin.getAttribute("plugin",null);
      if (name == null || name.length() == 0)
        continue;

      boolean required = Boolean.valueOf(plugin.getAttribute("required","true")).booleanValue();
      Dependency dep = new Dependency(name,plugin.getAttribute("version",null),required);
      found.add(dep);
    }
    return found.toArray(new Dependency[found.size()]);
  }
  
  /**
   * Liste der Plugins, von denen dieses hier direkt oder indirekt abhaengig ist.
   * Es werden also nicht nur die direkt abhaengigen Plugins geliefert sondern auch alle
   * Plugins, die von diesen wiederrum benoetigt werden.
   * Wenn nur die einfache Liste der Abhaengigkeiten ermittelt werden soll (also ohne
   * indirekte Abhaengigkeiten), dann muss "getDirectDependencies()" verwendet werden.
   * @return  Liste von Abhaengigkeiten fuer dieses Plugin.
   * Die Funktion liefert nie NULL sondern hoechstens ein leeres Array.
   */
  public Dependency[] getDependencies()
  {
    Dependency[] direct = this.getDirectDependencies();
    if (direct.length == 0)
      return direct; // Keine direkten Abhaengigkeiten. Also auch keine indirekten
    
    List<Dependency> found = new ArrayList<Dependency>();
    List<String> toCheck = new ArrayList<String>();
    
    for (Dependency d:direct)
    {
      found.add(d);
      toCheck.add(d.getName());
    }
    
    List all = Application.getPluginLoader().getManifests();
    for (int i=0;i<all.size();++i)
    {
      Manifest mf = (Manifest) all.get(i);
      String name = mf.getName();
      
      if (!toCheck.contains(name))
        continue; // interessiert uns nicht
      
      // Jepp, das Plugin ist in unserer Pruef-Liste enthalten.
      // Also brauchen wir auch dessen Abhaengigkeiten
      Dependency[] secondDeps = mf.getDependencies();
      if (secondDeps == null || secondDeps.length == 0)
        continue; // Plugin hat keine Abhaengikeiten

      // Checken, ob wir die Abhaengigkeit schon haben
      // Nur bei Bedarf hinzufuegen
      for (int k=0;k<secondDeps.length;++k)
      {
        if (found.contains(secondDeps[k]))
          continue; // haben wir schon
        found.add(secondDeps[k]);
      }
    }
    return (Dependency[]) found.toArray(new Dependency[found.size()]);
  }
  
  /**
   * Prueft, ob das Plugin installiert werden kann.
   * Konkret wird hier geprueft:
   * 
   *  1. Ist die richtige Jameica-Version installiert
   *  2. Sind die Abhaengigkeiten zu anderen Plugins erfuellt.
   *  3. Ist das Plugin ggf. schon installiert, wenn ja
   *     - kann es ueberschrieben werden?
   *     - ist die installierte Version nicht neuer als die zu installierende
   * @throws ApplicationException wenn das Plugin nicht installiert werden kann.
   */
  public void canDeploy() throws ApplicationException
  {
    // 1. Benoetigte Jameica-Version.
    Dependency jd = this.getJameicaDependency();
    if (!jd.check())
      throw new ApplicationException(Application.getI18n().tr("Plugin benötigt Jameica {0}",jd.getVersion()));

    // 2. Es reichen die direkten Abhaengigkeiten. Die indirekten werden ja
    //    von dem anderen Manifest geprueft
    Dependency[] deps = this.getDirectDependencies();
    for (Dependency d:deps)
    {
      if (!d.check())
        throw new ApplicationException(Application.getI18n().tr("Plugin benötigt {0}, welches aber nicht (oder in der falschen Version) installiert ist",d.getName()));
    }

    Manifest installed = null;

    // Checken, ob schon eine aktuellere Version installiert ist.
    List<Manifest> list = Application.getPluginLoader().getInstalledManifests();
    
    for (Manifest m:list)
    {
      if (m.getName().equals(this.getName()))
      {
        installed = m;
        break;
      }
    }
    
    if (installed == null)
      return;

    // 3a. Checken, ob es ueberschrieben werden kann.
    Type source = installed.getPluginSource();
    if (source == null || source != Type.USER)
      throw new ApplicationException(Application.getI18n().tr("Plugin kann nicht aktualisiert werden, da es sich im Plugin-Ordner des Systems befinden"));

    // 3b. Checken, ob die installierte Version eventuell aktueller ist
    if (installed.getVersion().compareTo(this.getVersion()) > 0)
      throw new ApplicationException(Application.getI18n().tr("Plugin ist bereits in einer aktuelleren Version installiert"));
  }
  
  /**
   * Liefert die Instanz des Plugins.
   * @return die Instanz des Plugins
   */
  AbstractPlugin getPluginInstance()
  {
    return this.pluginInstance;
  }
  
  /**
   * Speichert die Plugin-Instanz.
   * @param plugin die Plugin-Instanz.
   */
  void setPluginInstance(AbstractPlugin plugin)
  {
    this.pluginInstance = plugin;
  }
  
  /**
   * Legt die Art der Plugin-Quelle fest, aus der das Plugin stammt.
   * @param type die Art der Plugin-Quelle.
   */
  public void setPluginSource(Type type)
  {
    this.source = type;
  }
  
  /**
   * Liefert die Art der Plugin-Quelle, aus der das Plugin stammt.
   * @return die Art der Plugin-Quelle, aus der das Plugin stammt.
   */
  public Type getPluginSource()
  {
    return this.source;
  }

  /**
   * Prueft, ob das Plugin initialisiert werden konnte.
   * @return true, wenn es initialisiert werden konnte.
   */
  public boolean isInstalled()
  {
    return this.isInstalled;
  }
  
  /**
   * Legt fest, ob das Plugin als erfolgreich installiert gelten soll.
   * @param b
   */
  public void setInstalled(boolean b)
  {
    this.isInstalled = b;
  }

  /**
   * Wir implementieren die Funktion, damit wir eine Liste von Manifesten nach Abhaengigkeit sortieren koennen.
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    String name = this.getName();

    if (name == null || name.length() == 0)
    {
      Logger.debug("1: " + name + " > <unknown>");
      return -1; // wir haben keine Namen. Dann lassen wir uns sicherheitshalber zuerst laden
    }

    if (o == null || !(o instanceof Manifest))
    {
      Logger.debug("2: " + name + " > <unknown>");
      return -1; // Wir zuerst.
    }
    
    Manifest other   = (Manifest) o;
    String otherName = other.getName();

    if (otherName == null || otherName.length() == 0)
    {
      Logger.debug("3: " + otherName + " > " + name);
      return 1; // Das andere Plugin hat keinen Namen. Dann laden wir das sicherheitshalber zuerst
    }

    /////////////////////////////////////////////////////////////////
    // Schritt 1: Wir schauen, ob wir in der Abhaengigkeitsliste des anderen Plugins stehen
    Dependency[] deps = other.getDependencies();
    if (deps == null || deps.length == 0)
    {
      Logger.debug("4: " + otherName + " > " + name);
      return 1; // Es hat keine Abhaengigkeiten, also koennen wir nach dem anderen Plugin geladen werden
    }
    
    for (int i=0;i<deps.length;++i)
    {
      if (deps[i] == null)
        continue; // ueberspringen
      if (name.equals(deps[i].getName()))
      {
        Logger.debug("5: " + name + " > " + otherName);
        return -1; // Das andere Plugin haengt von uns ab. Also muessen wir zuerst geladen werden
      }
    }
    //
    /////////////////////////////////////////////////////////////////

    
    /////////////////////////////////////////////////////////////////
    // Schritt 2: Wir schauen, ob wir von dem anderen Plugin abhaengig sind.
    deps = this.getDependencies();
    if (deps == null || deps.length == 0)
    {
      Logger.debug("6: " + name + " > " + otherName);
      return -1; // Wir haben keine Abhaengigkeiten, also koennen wir vor dem anderen Plugin geladen werden
    }
    
    for (int i=0;i<deps.length;++i)
    {
      if (deps[i] == null)
        continue; // ueberspringen
      if (otherName.equals(deps[i]))
      {
        Logger.debug("7: " + otherName + " > " + name);
        return 1; // Wir haengen von dem anderen Plugin ab. Dann muss das andere zuerst geladen werden
      }
    }

    
    // Keine Schnittmenge vorhanden. Dann ist die Reihenfolge egal.
    Logger.debug("8: " + name + " <> " + otherName);
    return 0;
  }
}


/**********************************************************************
 * $Log: Manifest.java,v $
 * Revision 1.33  2011/06/17 15:55:18  willuhn
 * @N Registrieren von Message-Consumern im Manifest
 *
 * Revision 1.32  2011-06-08 13:22:22  willuhn
 * @N Neuer First-Start-Assistent, der zum Installieren eines neuen Plugins auffordert
 *
 * Revision 1.31  2011-06-01 17:35:59  willuhn
 * @N Ergonomischere Verwaltung der Plugins
 *
 * Revision 1.30  2011-06-01 16:06:11  willuhn
 * @N Mit Schema-Spezifikation 1.1 der plugin.xml kann jetzt auch der Dateiname eines Icon in der Groesse 64x64 Pixel angegeben werden, welches unter Datei->Einstellungen->Plugins angezeigt wird
 *
 * Revision 1.29  2011-06-01 15:22:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.28  2011-06-01 15:22:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.27  2011-06-01 13:45:43  willuhn
 * @B In ZippedPlugin duerfen die Deps nicht geprueft werden, weil dadurch indirekt (in Dependency.check()) der Plugin-Loader initialisiert werden wuerde
 *
 * Revision 1.26  2011-06-01 13:18:45  willuhn
 * @C Deploy- und Dependency-Checks in Manifest verschoben
 *
 * Revision 1.25  2011-06-01 12:35:58  willuhn
 * @N Die Verzeichnisse, in denen sich Plugins befinden koennen, sind jetzt separate Klassen vom Typ PluginSource. Damit kann das kuenftig um weitere Plugin-Quellen erweitert werden und man muss nicht mehr die Pfade vergleichen, um herauszufinden, in welcher Art von Plugin-Quelle ein Plugin installiert ist
 *
 * Revision 1.24  2011-05-31 16:39:04  willuhn
 * @N Funktionen zum Installieren/Deinstallieren von Plugins direkt in der GUI unter Datei->Einstellungen->Plugins
 *
 * Revision 1.23  2011-05-25 08:00:55  willuhn
 * @N Doppler-Check. Wenn ein gleichnamiges Plugin bereits geladen wurde, wird das zweite jetzt ignoriert. Konnte passieren, wenn ein User ein Plugin sowohl im System- als auch im User-Plugindir installiert hatte
 * @C Lade-Reihenfolge geaendert. Vorher 1. System, 2. User, 3. Config. Jetzt: 1. System, 2. Config, 3. User. Explizit in der Config angegebene Plugindirs haben also Vorrang vor ~/.jameica/plugins. Es bleibt weiterhin dabei, dass die Plugins im System-Dir Vorrang haben. Ist es dort bereits installiert, wird jetzt (dank Doppler-Check) das ggf. im User-Dir vorhandene ignoriert.
 *
 * Revision 1.22  2008/12/30 15:21:42  willuhn
 * @N Umstellung auf neue Versionierung
 *
 * Revision 1.21  2008/12/16 23:57:59  willuhn
 * @N Manifest-Objekt jetzt auch via InputStream erzeugbar
 *
 * Revision 1.20  2008/11/30 22:57:08  willuhn
 * @N Neues optionales Attribute "required", um optionale Abhaengigkeiten abbilden zu koennen
 *
 * Revision 1.19  2008/08/27 14:41:17  willuhn
 * @N Angabe der Versionsnummer von abhaengigen Plugins oder der Jameica RT
 *
 * Revision 1.18  2008/04/09 16:55:18  willuhn
 * @N Manifest#getDependencies() liefert nun auch indirekte Abhaengigkeiten
 * @C Sortierung der Plugins auf Quicksort umgestellt
 *
 * Revision 1.17  2008/01/11 10:22:19  willuhn
 * @N Name des Plugins vor Buildnummer und Builddate ausgeben
 *
 * Revision 1.16  2008/01/06 21:51:58  willuhn
 * @B bug 531
 *
 * Revision 1.15  2007/11/21 11:34:41  willuhn
 * @B "Boolean.parseBoolean" gibt es erst in Java 1.5
 *
 * Revision 1.14  2007/11/19 13:13:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2007/11/19 12:44:17  willuhn
 * @B Bug in Sortierung
 *
 * Revision 1.12  2007/11/13 00:45:18  willuhn
 * @N Classloader (privat/global) vom Plugin beeinflussbar (via "shared=true/false" in plugin.xml)
 *
 * Revision 1.11  2007/04/10 17:40:15  willuhn
 * @B Beruecksichtigung der Plugin-Abhaengigkeiten auch bei der Reihenfolge der zu ladenden Klassen (erzeugt sonst ggf. NoClassDefFoundErrors)
 *
 * Revision 1.10  2007/04/04 22:19:39  willuhn
 * @N Plugin-Dependencies im PluginLoader
 *
 * Revision 1.9  2006/10/07 19:35:11  willuhn
 * @B Zugriff auf buildnumber hatte sich mit neuem Pluginloader geaendert
 *
 * Revision 1.8  2006/06/30 13:51:34  willuhn
 * @N Pluginloader Redesign in HEAD uebernommen
 *
 * Revision 1.6.6.1  2006/06/06 21:27:08  willuhn
 * @N New Pluginloader (in separatem Branch)
 *
 * Revision 1.6  2005/05/27 17:31:46  web0
 * @N extension system
 *
 * Revision 1.5  2004/12/21 01:08:01  willuhn
 * @N new service configuration system in plugin.xml with auostart and dependencies
 *
 * Revision 1.4  2004/12/17 01:10:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/12 23:49:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/08 16:41:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/08 00:19:19  willuhn
 * *** empty log message ***
 *
 **********************************************************************/