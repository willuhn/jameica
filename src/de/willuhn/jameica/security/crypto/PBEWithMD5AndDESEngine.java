/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/crypto/PBEWithMD5AndDESEngine.java,v $
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
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import de.willuhn.io.IOUtil;
import de.willuhn.jameica.security.Wallet;
import de.willuhn.logging.Logger;
import de.willuhn.util.Base64;

/**
 * Implementierung einer Engine mit PBEWithMD5AndDES.
 */
public class PBEWithMD5AndDESEngine implements Engine
{
  private final static String ALG = "PBEWithMD5AndDES";
  private final static int ITER   = 987;

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
    PBEParameterSpec paramspec = new PBEParameterSpec(getSalt(),ITER);
    Cipher cipher = Cipher.getInstance(ALG);
    cipher.init(mode,getKey(),paramspec);
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
      this.wallet = new Wallet(PBEWithMD5AndDESEngine.class);
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
      salt = new byte[8];
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
  private SecretKey getKey() throws Exception
  {
    // Checken, ob wir schon eins haben.
    Wallet wallet = this.getWallet();
    char[] password = (char[]) wallet.get("password");
    if (password == null)
    {
      // Neu erstellen
      Logger.debug("creating random password");
      byte[] buf = new byte[16];
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
      random.setSeed(System.nanoTime());
      random.nextBytes(buf);
      password = Base64.encode(buf).toCharArray();
      wallet.set("password",password);
    }

    Logger.debug("creating secret key");
    SecretKeyFactory f = SecretKeyFactory.getInstance(ALG);
    PBEKeySpec spec = new PBEKeySpec(password);
    return f.generateSecret(spec);
  }
}



/**********************************************************************
 * $Log: PBEWithMD5AndDESEngine.java,v $
 * Revision 1.1  2011/02/08 18:27:53  willuhn
 * @N Code zum Ver- und Entschluesseln in neue Crypto-Engines ausgelagert und neben der bisherigen RSAEngine eine AES- und eine PBEWithMD5AndDES-Engine implementiert
 *
 **********************************************************************/