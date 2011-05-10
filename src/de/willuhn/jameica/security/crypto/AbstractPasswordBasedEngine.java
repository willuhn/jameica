/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/crypto/AbstractPasswordBasedEngine.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/05/10 18:00:17 $
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
import de.willuhn.util.Base64;

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
    AlgorithmParameterSpec spec = getParameterSpec();
    if (spec != null)
      cipher.init(mode,getKey(),spec);
    else
      cipher.init(mode,getKey());
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
      this.wallet = new Wallet(this.getClass(), new RSAEngine());
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
    
    // Migration. Wir hatten das anfangs falsch ohne Laenge gespeichert
    String s = (String) wallet.get("salt"); // wir speichern nicht direkt das Array, weil das das Wallet aufblaest
    if (s == null)
      s = (String) wallet.get("salt." + len); // Das ist jetzt der neue Platz
    if (s != null)
      return Base64.decode(s);
    
    
      
    // Neu erstellen
    Logger.debug("creating new salt");
    byte[] salt = new byte[len];
    SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
    random.setSeed(System.nanoTime());
    random.nextBytes(salt);
    wallet.set("salt",Base64.encode(salt));
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
    
    // Migration. Wir hatten das anfangs falsch ohne Laenge gespeichert
    String s = (String) wallet.get("password"); // wir speichern nicht direkt das Array, weil das das Wallet aufblaest
    if (s == null)
      s = (String) wallet.get("password." + len); // Das ist jetzt der neue Platz
    if (s != null)
      return s.toCharArray();
    
    // Neu erstellen
    s = RandomStringUtils.randomAscii(len);
    Logger.info("created random password, length: " + s.length());
    wallet.set("password." + len,s);
    return s.toCharArray();
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
 * Revision 1.5  2011/05/10 18:00:17  willuhn
 * @C AES-Keysize gekuerzt - verursachte auf manchen Systemen eine "java.security.InvalidKeyException: Illegal key size". Die bereits existierenden Keys bleiben aber erhalten
 *
 * Revision 1.4  2011-04-06 08:49:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2011-02-09 12:26:51  willuhn
 * @C Salt und Passwort NICHT als char[]/byte[] im RSA-Wallet speichern. Der XML-Encoder verwendet fuer jedes char/byte ein XML-Element, was dazu fuehrt, dass das Wallet auf eine kritische Groesse aufgeblasen wird
 *
 * Revision 1.2  2011-02-09 11:25:23  willuhn
 * @R Debug-Ausgabe entfernt
 *
 * Revision 1.1  2011-02-09 11:25:08  willuhn
 * @C Code-Cleanup
 *
 **********************************************************************/