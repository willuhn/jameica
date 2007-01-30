/**
 * 
 */
package de.willuhn.jameica.system;

import java.io.File;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.plugin.Manifest;

/**
 * 
 */
public class ManifestService implements Bootable
{
  private Manifest manifest = null;

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{JameicaClassLoader.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader arg0, Bootable arg1) throws SkipServiceException
  {
    try
    {
      this.manifest = Application.prepareClasses(new File("."));
    }
    catch (Exception e)
    {
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
   * @return
   */
  public Manifest getManifest()
  {
    return this.manifest;
  }

}
