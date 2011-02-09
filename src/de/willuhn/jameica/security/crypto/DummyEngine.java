/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/crypto/DummyEngine.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/02/09 16:20:10 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
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
}



/**********************************************************************
 * $Log: DummyEngine.java,v $
 * Revision 1.1  2011/02/09 16:20:10  willuhn
 * @N Dummy-Engine
 *
 **********************************************************************/