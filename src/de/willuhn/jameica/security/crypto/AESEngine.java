/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/crypto/AESEngine.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/02/08 18:27:53 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.security.crypto;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import de.willuhn.io.IOUtil;
import de.willuhn.jameica.security.Wallet;
import de.willuhn.logging.Logger;

/**
 * Implementierung einer Engine mit AES.
 */
public class AESEngine implements Engine
{
  private final static String ALG = "AES/CBC/PKCS5Padding";

  private Wallet wallet = null;
  
  /**
   * @see de.willuhn.jameica.security.crypto.Engine#encrypt(java.io.InputStream, java.io.OutputStream)
   */
  public void encrypt(InputStream is, OutputStream os) throws Exception
  {
    Logger.debug("encrypting data");
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
    Logger.debug("decrypting data");
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
    Cipher cipher = Cipher.getInstance(ALG);
    cipher.init(mode,getKeySpec(),new IvParameterSpec(getSalt()));
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
      this.wallet = new Wallet(AESEngine.class);
    return this.wallet;
  }
  
  /**
   * Liefert das Salt fuer die Verschluesselung.
   * @return das Salt.
   * @throws Exception
   */
  private byte[] getSalt() throws Exception
  {
    // Checken, ob wir schon eins haben.
    Wallet wallet = this.getWallet();
    byte[] salt = (byte[]) wallet.get("salt");
    if (salt == null)
    {
      // Neu erstellen
      Logger.debug("creating new salt");
      salt = new byte[16];
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
      random.setSeed(System.nanoTime());
      random.nextBytes(salt);
      wallet.set("salt",salt);
    }
    
    return salt;
  }
  
  /**
   * Liefert den Key fuer die Verschluesselung.
   * @return der Key fuer die Verschluesselung.
   * @throws Exception
   */
  private SecretKeySpec getKeySpec() throws Exception
  {
    // Checken, ob wir schon eins haben.
    Wallet wallet = this.getWallet();
    byte[] password = (byte[]) wallet.get("password");
    if (password == null)
    {
      // Neu erstellen
      Logger.debug("creating random password");
      password = new byte[24];
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
      random.setSeed(System.nanoTime());
      random.nextBytes(password);
      wallet.set("password",password);
    }
    
    return new SecretKeySpec(password, "AES");
  }

}



/**********************************************************************
 * $Log: AESEngine.java,v $
 * Revision 1.1  2011/02/08 18:27:53  willuhn
 * @N Code zum Ver- und Entschluesseln in neue Crypto-Engines ausgelagert und neben der bisherigen RSAEngine eine AES- und eine PBEWithMD5AndDES-Engine implementiert
 *
 **********************************************************************/