/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
import java.util.regex.Pattern;

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
      mycl.setParent(Application.getClassLoader());
      mycl.setName("loader." + manifest.getName());
      Logger.info("  adding system classloader");
      
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
        List<Manifest> plugins = Application.getPluginLoader().getManifests();
        for (int i=0;i<plugins.size();++i)
        {
          Manifest mf = plugins.get(i);
          if (mf == null || manifest.getName().equals(mf.getName())) // sind wir selbst
            continue;
          
          // Plugin ist shared und damit im oben hinzugefügten Jameica-Classloader bereits enthalten
          // Wir wuerden hier sonst unnoetig eine weitere Instanz dieses Classloaders hinzufuegen
          if (mf.isShared())
            continue;
          
          for (int k=0;k<deps.length;++k)
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
    
    // Kompilat-Ordner aus den Quellcode-Projekten, um die Plugins auch in der IDE im Debugger laufen lassen zu koennen
    for (File bin:Arrays.asList(new File(dir,"bin"),new File(dir,"target/classes")))
    {
      if (bin.exists())
      {
        Logger.info(mycl.getName() + ": added dir " + bin);
        mycl.add(bin); // Fuer den Start in Eclipse bzw. entpackte Classen
      }
      Application.getCallback().getStartupMonitor().addPercentComplete(2);
    }

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

    
    final Pattern binDir1 = Pattern.compile(path + "/bin/.*\\.class");
    final Pattern binDir2 = Pattern.compile(path + "/target/classes.*\\.class");
    
    // Wir iterieren ueber alle Dateien in dem Verzeichnis.
    for (int i=0;i<child.length;++i)
    {
      if (++count % 75 == 0)
        Application.getCallback().getStartupMonitor().addPercentComplete(1);

      String name = child[i].getCanonicalPath();
      name = name.replaceAll("\\\\","/"); // Windows-Backslash gegen Linux-Slash ersetzen
      
      // Class-Files nur, wenn sie sich im bin-Verzeichnis befinden
      boolean binDir = binDir1.matcher(name).matches();
      if (binDir || binDir2.matcher(name).matches())
      {
        // Jetzt muessen wir vorn noch den Verzeichnisnamen abschneiden
        name = name.substring(path.length() + (binDir ? 5 : 16)); // fuehrenden Pfad abschneiden ("/bin" bzw. "/target/classes" beachten)
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
        try
        {
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
        finally
        {
          if (jar != null)
          {
            try
            {
              jar.close();
            }
            catch (Exception e)
            {
              Logger.error("unable to close jar file",e);
            }
          }
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
      Logger.error("error while loading class " + classname + " via " + cl.getName(),t);
    }
  }

}
