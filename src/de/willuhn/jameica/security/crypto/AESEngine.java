/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/crypto/AESEngine.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/05/10 18:00:17 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.security.crypto;

import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Implementierung einer Engine mit AES.
 */
public class AESEngine extends AbstractPasswordBasedEngine
{
  /**
   * @see de.willuhn.jameica.security.crypto.AbstractPasswordBasedEngine#getAlgorithm()
   */
  String getAlgorithm()
  {
    return "AES/CBC/PKCS5Padding";
  }

  /**
   * @see de.willuhn.jameica.security.crypto.AbstractPasswordBasedEngine#getParameterSpec()
   */
  AlgorithmParameterSpec getParameterSpec() throws Exception
  {
    return new IvParameterSpec(getSalt(16));
  }

  /**
   * @see de.willuhn.jameica.security.crypto.AbstractPasswordBasedEngine#getKey()
   */
  Key getKey() throws Exception
  {
    byte[] password = new String(getPassword(16)).getBytes("ISO-8859-1");
    return new SecretKeySpec(password,"AES");
  }

}



/**********************************************************************
 * $Log: AESEngine.java,v $
 * Revision 1.3  2011/05/10 18:00:17  willuhn
 * @C AES-Keysize gekuerzt - verursachte auf manchen Systemen eine "java.security.InvalidKeyException: Illegal key size". Die bereits existierenden Keys bleiben aber erhalten
 *
 * Revision 1.2  2011-02-09 11:25:08  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.1  2011-02-08 18:27:53  willuhn
 * @N Code zum Ver- und Entschluesseln in neue Crypto-Engines ausgelagert und neben der bisherigen RSAEngine eine AES- und eine PBEWithMD5AndDES-Engine implementiert
 *
 **********************************************************************/