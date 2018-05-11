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
