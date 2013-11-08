/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/ClassService.java,v $
 * $Revision: 1.9 $
 * $Date: 2012/03/28 22:28:07 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.io.FileFinder;
import de.willuhn.jameica.plugin.Dependency;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.MultipleClassLoader;


/**
 * Kuemmert sich um das Laden der Klassen und Registrieren im Classfinder.
 */
public class ClassService implements Bootable
{

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{LogService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    try {
      // wir laden erstmal uns selbst
      prepareClasses(Application.getManifest(),Application.getClassLoader());
      File[] jars = Application.getClassLoader().addJars(new File("lib"));
      if (jars != null)
      {
        Arrays.sort(jars); // Das machen wir nur der Optik wegen. Dann kann man das im Log besser lesen
        for (int i=0;i<jars.length;++i)
        {
          Logger.info("loaded system jar " + jars[i].getAbsolutePath());
        }
        
      }
    }
    catch (RuntimeException re)
    {
      throw re;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }

  /**
   * Durchsucht das Verzeichnis, in dem sich das Manifest befindet nach Klassen und Jars,
   * laedt diese in den Classpath und registriert die alle Klassen im Classfinder,
   * deren Name zu den Suchfiltern in der Sektion &lt;classfinder&gt; passen. 
   * @param manifest das Manifest.
   * @return ein Classloader, der genau dieses Plugin enthaelt.
   * @throws Exception
   */
  public synchronized MultipleClassLoader prepareClasses(Manifest manifest) throws Exception
  {
    if (manifest == null)
      throw new Exception("no manifest given");

    File dir = new File(manifest.getPluginDir());
    Logger.info("checking directory " + dir.getAbsolutePath());
    Application.getCallback().getStartupMonitor().setStatusText("checking directory " + dir.getAbsolutePath());

    ////////////////////////////////////////////////////////////////////////////
    // Classpath befuellen

    Application.getCallback().getStartupMonitor().addPercentComplete(2);

    MultipleClassLoader mycl = null;
    
    if (manifest.isShared())
    {
      Logger.info("using global classloader for plugin " + manifest.getName());
      mycl = Application.getClassLoader();
    }
    else
    {
      Logger.info("using private classloader for plugin " + manifest.getName());
      // Eigenen Classloader fuer das Plugin erstellen
      mycl = new MultipleClassLoader();
      mycl.setName("loader." + manifest.getName());
      Logger.info("  adding system classloader");
      mycl.addClassloader(Application.getClassLoader());
      
      // Jetzt muessen wir uns noch die Classloader der Dependency-Plugins holen
      // und dem aktuellen Plugin hinzufuegen. Damit kennt das Plugin dann:
      //   - sich selbst
      //   - die Plugins, von denen es abhaengig ist
      //   - Jameica
      // Hier reichen die direkten Abhaengkeiten. Weil deren Classloader kennen
      // ja wiederrum ihre Abhaengigkeiten
      Dependency[] deps = manifest.getDirectDependencies();
      if (deps != null && deps.length > 0)
      {
        Logger.info("  adding depending classloaders");
        List<Manifest> plugins = Application.getPluginLoader().getInstalledManifests();
        for (int i=0;i<plugins.size();++i)
        {
          Manifest mf = plugins.get(i);
          if (mf == null || manifest.getName().equals(mf.getName())) // sind wir selbst
            continue;
          for (int k=0;k<deps.length;++i)
          {
            if (deps[k].getName().equals(mf.getName()) && mf.isLoaded())
            {
              Logger.info("    " + mf.getName());
              mycl.addClassloader(mf.getClassLoader());
              break;
            }
          }
        }
      }
      
    }

    // Wir fuegen das Verzeichnis zum ClassLoader hinzu. (auch fuer die Ressourcen)
    mycl.add(dir);
    
    File bin = new File(dir,"bin");
    if (bin.exists())
    {
      Logger.info(mycl.getName() + ": added dir " + bin);
      mycl.add(bin); // Fuer den Start in Eclipse bzw. entpackte Classen
    }
    Application.getCallback().getStartupMonitor().addPercentComplete(2);

    // Und jetzt noch alle darin befindlichen Jars
    File[] jars = mycl.addJars(dir);
    if (jars != null)
    {
      Arrays.sort(jars); // Das machen wir nur der Optik wegen. Dann kann man das im Log besser lesen
      for (int i=0;i<jars.length;++i)
      {
        Logger.info(mycl.getName() + ": loaded jar " + jars[i].getAbsolutePath());
      }
      
    }

    Application.getCallback().getStartupMonitor().addPercentComplete(1);
    ////////////////////////////////////////////////////////////////////////////
    prepareClasses(manifest, mycl);
    return mycl;
  }

  /**
   * @param manifest
   * @param mycl
   * @throws IOException
   */
  private void prepareClasses(Manifest manifest, MultipleClassLoader mycl) throws IOException
  {
    ////////////////////////////////////////////////////////////////////////////
    // Classfinder befuellen
    
    long count = 0;

    File dir = new File(manifest.getPluginDir());
    FileFinder ff = new FileFinder(dir);
    
    // Include-Verzeichnisse aus Manifest uebernehmen
    String[] cfIncludes = manifest.getClassFinderIncludes();
    if (cfIncludes.length == 0)
    {
      Logger.info("no classfinder includes for this plugin");
      return;
    }
    for (int i=0;i<cfIncludes.length;++i)
    {
      Logger.debug("classfinder include: " + cfIncludes[i]);
      ff.matches(cfIncludes[i]);
    }

    File[] child = ff.findRecursive();
    
    String path = dir.getCanonicalPath();
    path = path.replaceAll("\\\\","/"); // Windows-Backslash gegen Linux-Slash ersetzen
    
    // Wir iterieren ueber alle Dateien in dem Verzeichnis.
    for (int i=0;i<child.length;++i)
    {
      if (++count % 75 == 0)
        Application.getCallback().getStartupMonitor().addPercentComplete(1);

      String name = child[i].getCanonicalPath();
      name = name.replaceAll("\\\\","/"); // Windows-Backslash gegen Linux-Slash ersetzen
      
      // Class-Files nur, wenn sie sich im bin-Verzeichnis befinden
      if (name.matches(path + "/bin/.*\\.class"))
      {
        // Jetzt muessen wir vorn noch den Verzeichnisnamen abschneiden
        name = name.substring(path.length() + 5); // fuehrenden Pfad abschneiden ("/bin" beachten)
        name = name.substring(0, name.indexOf(".class")).replace('/', '.'); // .class weg Trenner ersetzen
        if (name.startsWith("."))
          name = name.substring(1); // ggf. fuehrenden Punkt abschneiden
    
        // In ClassFinder uebernehmen
        load(mycl,name);
      }
        
      if (name.endsWith(".jar") || name.endsWith(".zip"))
      {
        Logger.info("inspecting " + name);

        JarFile jar = null;
        try {
          jar = new JarFile(child[i]);
        }
        catch (IOException ioe) {
          Logger.error("unable to load " + name + ", skipping",ioe);
          continue; // skip
        }

        // So, jetzt iterieren wir ueber alle Files in dem Jar
        Enumeration<JarEntry> jarEntries = jar.entries();
        JarEntry entry = null;
  
        while (jarEntries.hasMoreElements())
        {
          if (++count % 75 == 0)
            Application.getCallback().getStartupMonitor().addPercentComplete(1);
          
          entry = jarEntries.nextElement();
          String entryName = entry.getName();

          int idxClass = entryName.indexOf(".class");

          // alles, was nicht mit ".class" aufhoert, koennen wir jetzt ignorieren
          if (idxClass == -1)
            continue;
  
          // wir machen einen Klassen-Namen draus
          entryName = entryName.substring(0, idxClass).replace('/', '.').replace('\\', '.');

          // In ClassFinder uebernehmen
          load(mycl,entryName);
        }
      }
    }
  }
  
  /**
   * Laedt die Klasse und fuegt sie in den Classfinder.
   * @param classname zu ladende Klasse.
   */
  private static void load(MultipleClassLoader cl, String classname)
  {
    try {
      cl.load(classname);
    }
    catch (Throwable t)
    {
      Logger.error("error while loading class " + classname,t);
    }
  }

}


/**********************************************************************
 * $Log: ClassService.java,v $
 * Revision 1.9  2012/03/28 22:28:07  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.8  2011-07-18 16:31:00  willuhn
 * @N Name fuer den Classloader vergebbar
 *
 * Revision 1.7  2011-05-31 16:39:04  willuhn
 * @N Funktionen zum Installieren/Deinstallieren von Plugins direkt in der GUI unter Datei->Einstellungen->Plugins
 *
 * Revision 1.6  2010-09-10 11:34:52  willuhn
 * @C Klassen im bin-Verzeichnis nur noch finden, wenn das "bin" der direkte Unterordner ist. Fuehrte sonst zu unnoetigen Fehlermeldungen, wenn Jameica in einem Pfad installiert ist, der "/bin/" enthaelt
 *
 * Revision 1.5  2010/06/01 21:35:21  willuhn
 * @N Geladene Jars im Log alphabetisch ausgeben - das ist besser lesbar
 *
 * Revision 1.4  2008/12/17 01:05:41  willuhn
 * @N Deployment von heruntergeladenen in "DeployService" verschoben. Dann geschieht das Entpacken erst beim naechsten Start. Da zu dem Zeitpunkt der Classloader die Dateien noch nicht geladen hat, kann eine ggf. vorhandene vorherige Installation geloescht werden
 * @C FileUtil.deleteRecursive
 *
 * Revision 1.3  2008/08/31 23:07:10  willuhn
 * @N Erster GUI-Code fuer die Suche
 *
 * Revision 1.2  2008/08/27 14:41:17  willuhn
 * @N Angabe der Versionsnummer von abhaengigen Plugins oder der Jameica RT
 *
 * Revision 1.1  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 **********************************************************************/
