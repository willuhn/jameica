/**
 * 
 */
package de.willuhn.jameica.system;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.util.MultipleClassLoader;

/**
 * @author willuhn
 * Initialisiert den Jameica-Classloader.
 */
public class JameicaClassLoader implements Bootable
{

  private MultipleClassLoader loader = null;
  
  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return null;
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader arg0, Bootable arg1) throws SkipServiceException
  {
    this.loader = new MultipleClassLoader();
    this.loader.addClassloader(this.getClass().getClassLoader());

    // Wir machen unseren Classloader zum Context-Classloader fuer diesen Thread
    Thread.currentThread().setContextClassLoader(this.loader);
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }
  
  /**
   * Liefert den Classloader.
   * @return der Classloader.
   */
  public MultipleClassLoader getClassLoader()
  {
    return this.loader;
  }

}
