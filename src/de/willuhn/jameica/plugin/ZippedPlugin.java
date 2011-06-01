/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/ZippedPlugin.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/06/01 13:18:45 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.plugin;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementiert die Checks auf ein gezipptes Plugin.
 */
public class ZippedPlugin
{
  private String name       = null;
  private Manifest manifest = null;
  
  /**
   * ct.
   * @param zip die ZIP-Datei mit dem Plugin.
   * @throws ApplicationException wenn es kein gueltiges Plugin ist oder die Abhaengigkeiten nicht erfuellt sind.
   */
  public ZippedPlugin(File zip) throws ApplicationException
  {
    I18N i18n = Application.getI18n();

    if (zip == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie das zu installierende Plugin"));

    if (!zip.getName().endsWith(".zip"))
      throw new ApplicationException(i18n.tr("Keine gültige ZIP-Datei"));

    ApplicationException ae = new ApplicationException(i18n.tr("Kein gültiges Jameica-Plugin"));
    ZipFile zipFile         = null;
    
    try
    {
      zipFile = new ZipFile(zip,ZipFile.OPEN_READ);
      
      ////////////////////////////////////////////////////////////////////////////
      // 1. Aufbau der ZIP-Datei pruefen
      
      ZipEntry dir = null;
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements())
      {
        ZipEntry entry = entries.nextElement();
        String name = entry.getName();
        
        // Per Konvention darf die ZIP-Datei nur genau einen Ordner enthalten,
        // in dem sich das Plugin befindet. Demzufolge muss jeder Name einen
        // Slash enthalten, da alles in diesem Ordner sein muss. Hat er den
        // Slash nicht, ist es eine Datei direkt auf oberster Ebene. Das ist
        // nicht zulaessig.
        if (!name.contains("/"))
        {
          Logger.error(zip + " contains invalid file " + name);
          throw ae;
        }

        // Das ist ein Unter-Ordner oder eine Datei in einem Unter-Ordner. Zulaessig
        if (name.matches(".{1,}?/.{1,}"))
          continue;

        // Das darf jetzt nur noch der Haupt-Ordner sein
        if (!entry.isDirectory())
        {
          Logger.error("plugin zip-file must contain only one folder");
          throw ae;
        }

        // Wir haben schon den Haupt-Ordner. Hier kommt offensichtlich noch
        // eins. Unzulaessig
        if (dir != null)
        {
          Logger.error("plugin zip-file must contain only one folder");
          throw ae;
        }

        dir = entry;
      }
      
      // Das passiert nur, wenn die ZIP-Datei komplett leer ist. Gibts sowas ueberhaupt?
      if (dir == null)
      {
        Logger.error("plugin zip-file empty");
        throw ae;
      }
      
      String s = dir.getName();
      this.name = s.substring(0,s.length()-1).trim(); // Slash am Ende noch abschneiden
      
      // Nur nochmal zur Sicherheit
      // 1. Name muss vorhanden sein
      // 2. keine Slashes mehr
      // 3. kein Directory-Traversal
      if (this.name.length() == 0 || this.name.contains("/") || this.name.contains(".."))
      {
        Logger.error("plugin folder " + this.name + " invalid");
        throw ae;
      }
      //
      ////////////////////////////////////////////////////////////////////////////

      ////////////////////////////////////////////////////////////////////////////
      // 2. Manifest laden und checken
      
      // Im Hauptordner muss sich die plugin.xml befinden. Und sie darf natuerlich kein Dir sein
      ZipEntry entry = zipFile.getEntry(dir.getName() + "plugin.xml");
      if (entry == null || entry.isDirectory())
      {
        Logger.error("plugin zip-file contains no plugin.xml");
        throw ae;
      }

      // Den muessen wir nicht schliessen. Das geschieht automatisch beim Schliessen der ZIP-Datei
      InputStream is = zipFile.getInputStream(entry);
      if (is == null)
      {
        Logger.error("plugin zip-file contains no plugin.xml");
        throw ae;
      }

      this.manifest = new Manifest(is);
      this.manifest.checkDependencies();
      //
      ////////////////////////////////////////////////////////////////////////////

      // Toll. Sieht alles OK aus ;)
    }
    catch (ApplicationException ae2)
    {
      throw ae2;
    }
    catch (Exception e)
    {
      Logger.error("unable to check zip file " + zip,e);
      throw ae;
    }
    finally
    {
      if (zipFile != null)
      {
        try
        {
          zipFile.close();
        }
        catch (Exception e) { /* useless */}
      }
    }
  }
  
  /**
   * Liefert den Ordner-Namen des Plugins.
   * @return der Ordner-Name des Plugins.
   */
  public String getName()
  {
    return this.name;
  }
  
  /**
   * Liefert das Manifest des Plugins.
   * @return das Manifest des Plugins.
   */
  public Manifest getManifest()
  {
    return this.manifest;
  }
}



/**********************************************************************
 * $Log: ZippedPlugin.java,v $
 * Revision 1.2  2011/06/01 13:18:45  willuhn
 * @C Deploy- und Dependency-Checks in Manifest verschoben
 *
 * Revision 1.1  2011-06-01 11:03:40  willuhn
 * @N ueberarbeiteter Install-Check - das Plugin muss jetzt nicht mehr temporaer entpackt werden - die Pruefung geschieht on-the-fly auf der ZIP-Datei
 *
 **********************************************************************/