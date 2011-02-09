/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/crypto/PBEWithMD5AndDESEngine.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/02/09 11:25:08 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.security.crypto;

import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * Implementierung einer Engine mit PBEWithMD5AndDES.
 */
public class PBEWithMD5AndDESEngine extends AbstractPasswordBasedEngine
{
  /**
   * @see de.willuhn.jameica.security.crypto.AbstractPasswordBasedEngine#getAlgorithm()
   */
  String getAlgorithm()
  {
    return "PBEWithMD5AndDES";
  }

  /**
   * @see de.willuhn.jameica.security.crypto.AbstractPasswordBasedEngine#getParameterSpec()
   */
  AlgorithmParameterSpec getParameterSpec() throws Exception
  {
    return new PBEParameterSpec(getSalt(8),987);
  }

  /**
   * @see de.willuhn.jameica.security.crypto.AbstractPasswordBasedEngine#getKey()
   */
  Key getKey() throws Exception
  {
    SecretKeyFactory f = SecretKeyFactory.getInstance(getAlgorithm());
    PBEKeySpec spec = new PBEKeySpec(getPassword(16));
    return f.generateSecret(spec);
  }
}



/**********************************************************************
 * $Log: PBEWithMD5AndDESEngine.java,v $
 * Revision 1.2  2011/02/09 11:25:08  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.1  2011-02-08 18:27:53  willuhn
 * @N Code zum Ver- und Entschluesseln in neue Crypto-Engines ausgelagert und neben der bisherigen RSAEngine eine AES- und eine PBEWithMD5AndDES-Engine implementiert
 *
 **********************************************************************/