/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.store;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.security.crypto.AESEngine;
import de.willuhn.jameica.security.crypto.Engine;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ein Store zum bequemen Lesen und Speichern von JAXB-tauglichen Beans.
 */
@Lifecycle(Type.REQUEST)
public class BeanStore
{
  private Engine engine = new AESEngine();
  
  /**
   * Erzeugt einen neuen leeren Bean-Container.
   * @param type der konkrete Typ der Beans.
   * @param encrypted true, wenn der Beanstore verschluesselt speichern soll.
   * @return der erzeugte Bean-Container.
   * @throws ApplicationException
   */
  public <T> BeanContainer<T> load(Class<T> type, boolean encrypted) throws ApplicationException
  {
    // Mal schauen, ob die Datei existiert. Wenn nicht, erstellen wir einen neuen.
    File file = this.getFile(type,encrypted);
    
    // Ne, dann neu erstellen
    if (!file.exists())
    {
      BeanContainer<T> container = new BeanContainer<>(type,encrypted);
      Logger.info("created new " + container);
      return container;
    }

    
    try(
        InputStream is2 = this.engine.decrypt(new BufferedInputStream(new FileInputStream(file)));
    ) {
      // Ja, dann laden
      JAXBContext ctx = JAXBContext.newInstance(BeanContainer.class,type);
      Unmarshaller u = ctx.createUnmarshaller();

      ////////////////
      // Fuer den Fall, dass die Beans Ressourcen enthalten, die injeziert werden koennen,
      // durchlaufen sie den Injector.
      final BeanService beanService = Application.getBootLoader().getBootable(BeanService.class);
      u.setListener(new Unmarshaller.Listener()
      {
        /**
         * @see javax.xml.bind.Unmarshaller.Listener#afterUnmarshal(java.lang.Object, java.lang.Object)
         */
        @Override
        public void afterUnmarshal(Object target, Object parent)
        {
          beanService.inject(target);
        }
      });
      //
      ////////////////
      
      BeanContainer<T> container = (BeanContainer<T>) u.unmarshal(is2);
      Logger.info("loading bean container from " + file);
      if (!encrypted)
      {
        Logger.info("bean container encryption is disabled");
        container = (BeanContainer<T>) u.unmarshal((InputStream)null);
      }
      
      Logger.info("loaded " + container);
      return container;
    }
    catch (Exception e)
    {
      Logger.error("unable to load bean-container",e);
      throw new ApplicationException(Application.getI18n().tr("Laden des Bean-Container fehlgeschlagen: {0}",e.getMessage()));
    }
  }
  
  /**
   * Speichert einen Bean-Container.
   * @param container der zu speichernde Container.
   * @throws ApplicationException
   */
  public void store(BeanContainer<?> container) throws ApplicationException
  {
    File tmp = null;
    try
    {
      File file = this.getFile(container.type,container.encrypted);
      tmp  = File.createTempFile(file.getName() + "_","",file.getAbsoluteFile().getParentFile());

      // Ja, dann laden
      Logger.info("storing " + container + " to " + file);
      JAXBContext ctx = JAXBContext.newInstance(BeanContainer.class,container.type);
      Marshaller m = ctx.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);

      try (OutputStream os = new BufferedOutputStream(new FileOutputStream(tmp))) {
        if (container.encrypted) {
          m.marshal(container,this.engine.encrypt(os));
        } else {
          m.marshal(container,os);
        }

        // Wir koennen das Flushen und Schliessen nicht im finally() machen,
        // weil wir _nach_ dem Schliessen noch die Datei umbenennen wollen.
        // Das Umbenennen wuerde sonst _vorher_ passieren.
        os.flush();
      }
      
      if (!tmp.exists())
        throw new IOException("stored file does not exist");
      
      if (file.exists() && !file.delete())
        throw new IOException("deleting of previous file failed");
      
      // Schreiben war erfolgreich. Jetzt kopieren wir die Temp-Datei rueber.
      tmp.renameTo(file);
      tmp.delete();
      Logger.info("stored " + container);
    }
    catch (Exception e)
    {
      Logger.error("unable to load bean-container",e);
      throw new ApplicationException(Application.getI18n().tr("Speichern des Bean-Container fehlgeschlagen: {0}",e.getMessage()));
    }
    finally
    {
      // Wenn das Speichern fehlschlug, dann loeschen wir die halbe Datei
      if (tmp != null && tmp.exists())
        tmp.delete();
    }
  }
  
  /**
   * Liefert den Ablage-Ort des Containers.
   * @param type der konkrete Typ der Beans.
   * @param encrypted true, wenn der Beanstore verschluesselt speichern soll.
   * @return der Ablage-Ort.
   */
  private File getFile(Class<?> type, boolean encrypted)
  {
    String dir  = Application.getConfig().getConfigDir();
    String name = type.getName();
    
    return new File(dir,name + "." + (encrypted ? "e" : "") + "beans");
  }

}


