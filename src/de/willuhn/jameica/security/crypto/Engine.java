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

/**
 * Interface fuer eine Engine zum Ver- und Entschluessel von Daten.
 */
public interface Engine
{
  /**
   * Verschluesselt die Daten.
   * Die Streams muessen vom Aufrufer geschlossen werden.
   * @param is Inputstream mit den unverschluesselten Daten.
   * @param os Outputstream mit den verschluesselten Daten.
   * @throws Exception
   */
  public void encrypt(InputStream is, OutputStream os) throws Exception;

  /**
   * Entschluesselt die Daten.
   * Die Streams muessen vom Aufrufer geschlossen werden.
   * @param is Inputstream mit den verschluesselten Daten.
   * @param os Outputstream mit den entschluesselten Daten.
   * @throws Exception
   */
  public void decrypt(InputStream is, OutputStream os) throws Exception;
  
  /**
   * Verschluesselt die Daten.
   * Die Streams muessen vom Aufrufer geschlossen werden.
   * @param os OutputStream mit den unverschluesselten Daten.
   * @return Outputstream mit den verschluesselten Daten.
   * @throws Exception
   */
  public OutputStream encrypt(OutputStream os) throws Exception;

  /**
   * Entschluesselt die Daten.
   * Die Streams muessen vom Aufrufer geschlossen werden.
   * @param is Inputstream mit den verschluesselten Daten.
   * @return InputStream mit den entschluesselten Daten.
   * @throws Exception
   */
  public InputStream decrypt(InputStream is) throws Exception;
}



/**********************************************************************
 * $Log: Engine.java,v $
 * Revision 1.1  2011/02/08 18:27:53  willuhn
 * @N Code zum Ver- und Entschluesseln in neue Crypto-Engines ausgelagert und neben der bisherigen RSAEngine eine AES- und eine PBEWithMD5AndDES-Engine implementiert
 *
 **********************************************************************/