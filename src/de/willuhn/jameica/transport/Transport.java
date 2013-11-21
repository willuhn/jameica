/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.transport;

import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import de.willuhn.util.ProgressMonitor;

/**
 * Generische Kapselung zum Download von Daten von anderen Systemen - typischerweise via HTTP.
 * Initial wird das lediglich vom Update-Service zum Download von Plugins verwendet.
 * Es ist aber nicht darauf beschraenkt.
 */
public interface Transport
{
  /**
   * Initialisiert den Transport mit der angegebenen URL.
   * @param url die URL.
   */
  public void init(URL url);
  
  /**
   * Ruft die Daten von der angegebenen URL herunter und schreibt sie in den Stream.
   * @param os OutputStream, in den die Daten geschrieben werden.
   * Der OutputStream wird vom Transport bereits geschlossen.
   * @param monitor optionaler Progress-Monitor.
   * @throws Exception
   */
  public void get(OutputStream os, ProgressMonitor monitor) throws Exception;
  
  /**
   * Prueft, ob die angegebene URL existiert.
   * @return true, wenn sie existiert, sonst false.
   */
  public boolean exists();
  
  /**
   * Liefert die Dateigroesse der URL in Bytes.
   * @return Dateigroesse der URL in Bytes oder -1, wenn sie nicht ermittelbar ist.
   */
  public long getSize();
  
  /**
   * Liefert eine Liste der vom Transport untertuetzten Protokolle.
   * @return Liste der Protokolle.
   * Z.Bsp. "http".
   */
  public List<String> getProtocols();
}
