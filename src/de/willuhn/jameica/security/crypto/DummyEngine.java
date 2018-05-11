/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.security.crypto;

import java.io.InputStream;
import java.io.OutputStream;

import de.willuhn.io.IOUtil;

/**
 * Dummy-Engine, die nicht verschluesselt sondern lediglich die Daten kopiert.
 * NUR ZU TESTZWECKEN VERWENDEN!
 */
public class DummyEngine implements Engine
{
  /**
   * @see de.willuhn.jameica.security.crypto.Engine#decrypt(java.io.InputStream, java.io.OutputStream)
   */
  public void decrypt(InputStream is, OutputStream os) throws Exception
  {
    IOUtil.copy(is,os);
  }

  /**
   * @see de.willuhn.jameica.security.crypto.Engine#encrypt(java.io.InputStream, java.io.OutputStream)
   */
  public void encrypt(InputStream is, OutputStream os) throws Exception
  {
    IOUtil.copy(is,os);
  }
  
  /**
   * @see de.willuhn.jameica.security.crypto.Engine#decrypt(java.io.InputStream)
   */
  public InputStream decrypt(InputStream is) throws Exception
  {
    return is;
  }
  
  /**
   * @see de.willuhn.jameica.security.crypto.Engine#encrypt(java.io.OutputStream)
   */
  public OutputStream encrypt(OutputStream os) throws Exception
  {
    return os;
  }
}
