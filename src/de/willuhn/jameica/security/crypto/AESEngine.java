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
