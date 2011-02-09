/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/crypto/AbstractPasswordBasedEngine.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/02/09 11:25:08 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.security.crypto;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import org.apache.commons.lang.RandomStringUtils;

import de.willuhn.io.IOUtil;
import de.willuhn.jameica.security.Wallet;
import de.willuhn.logging.Logger;

/**
 * Abstrakte Basis-Implementierung einer Passwort-basierten Crypto-Engine.
 */
public abstract class AbstractPasswordBasedEngine implements Engine
{
  private Wallet wallet = null;

  /**
   * @see de.willuhn.jameica.security.crypto.Engine#encrypt(java.io.InputStream, java.io.OutputStream)
   */
  public void encrypt(InputStream is, OutputStream os) throws Exception
  {
    Logger.info("encrypting data using " + getAlgorithm());
    Cipher cipher = createCipher(Cipher.ENCRYPT_MODE);
    CipherOutputStream cos = new CipherOutputStream(os,cipher);
    IOUtil.copy(is,cos);
    
    // Wir muessen den CipherOutputStream leider selbst schliessen, damit das doFinal intern gemacht wird
    // Das schliesst zwar auch bereits "os" - aber das laesst sich nicht vermeiden.
    cos.close();
  }

  /**
   * @see de.willuhn.jameica.security.crypto.Engine#decrypt(java.io.InputStream, java.io.OutputStream)
   */
  public void decrypt(InputStream is, OutputStream os) throws Exception
  {
    Logger.info("decrypting data using " + getAlgorithm());
    Cipher cipher = createCipher(Cipher.DECRYPT_MODE);
    CipherInputStream cis = new CipherInputStream(is,cipher);
    IOUtil.copy(cis, os);
  }

  /**
   * Liefert den vorkonfigurierten Cipher.
   * @param mode Legt den Modus (ENCRYPT/DECRYPT) fest.
   * @return der Cipher.
   * @throws Exception
   */
  private Cipher createCipher(int mode) throws Exception
  {
    Logger.debug("creating cipher");
    Cipher cipher = Cipher.getInstance(getAlgorithm());
    cipher.init(mode,getKey(),getParameterSpec());
    return cipher;
  }

  /**
   * Liefert das Wallet, in dem wir Salt und Passwort speichern.
   * @return das Wallet.
   * @throws Exception
   */
  private synchronized Wallet getWallet() throws Exception
  {
    if (this.wallet == null)
      this.wallet = new Wallet(AbstractPasswordBasedEngine.class);
    return this.wallet;
  }
  
  /**
   * Liefert das Salt fuer die Verschluesselung.
   * @param len Laenge in Bytes.
   * @return das Salt.
   * @throws Exception
   */
  byte[] getSalt(int len) throws Exception
  {
    if (len < 1)
      throw new Exception("invalid salt length: " + len);
    
    // Checken, ob wir schon eins haben.
    Wallet wallet = this.getWallet();
    byte[] salt = (byte[]) wallet.get(getAlgorithm() + ".salt");
    if (salt == null)
    {
      // Neu erstellen
      Logger.debug("creating new salt");
      salt = new byte[len];
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
      random.setSeed(System.nanoTime());
      random.nextBytes(salt);
      wallet.set(getAlgorithm() + ".salt",salt);
    }
    return salt;
  }
  
  /**
   * Liefert das Passwort fuer die Verschluesselung.
   * @param len Laenge in Zeichen.
   * @return das Passwort.
   * @throws Exception
   */
  char[] getPassword(int len) throws Exception
  {
    if (len < 1)
      throw new Exception("invalid password length: " + len);

    // Checken, ob wir schon eins haben.
    Wallet wallet = this.getWallet();
    char[] password = (char[]) wallet.get(getAlgorithm() + ".password");
    if (password == null)
    {
      // Neu erstellen
      Logger.debug("creating random password");
      password = RandomStringUtils.randomAscii(len).toCharArray();
      System.out.println(password);
      wallet.set(getAlgorithm() + ".password",password);
    }
    return password;
  }
  
  /**
   * Liefert die Parameter des Algorithmus.
   * @return die Parameter des Algorithmus.
   * @throws Exception
   */
  abstract AlgorithmParameterSpec getParameterSpec() throws Exception;
  
  /**
   * Liefert den Namen des Algorithmus.
   * @return der Namen des Algorithmus.
   */
  abstract String getAlgorithm();
  
  /**
   * Liefert den Schluessel.
   * @return der Schluessel.
   * @throws Exception
   */
  abstract Key getKey() throws Exception;
}



/**********************************************************************
 * $Log: AbstractPasswordBasedEngine.java,v $
 * Revision 1.1  2011/02/09 11:25:08  willuhn
 * @C Code-Cleanup
 *
 **********************************************************************/