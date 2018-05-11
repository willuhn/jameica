/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.io.File;

/**
 * Wird gesendet, wenn sich im System eine Programm-Datei geaendert hat.
 */
public class FileChangedMessage implements Message
{
  private File file = null;
  
  /**
   * ct.
   * @param file die geaenderte Datei.
   */
  protected FileChangedMessage(File file)
  {
    this.file = file;
  }
  
  /**
   * Liefert die geaenderte Datei.
   * @return die geaenderte Datei.
   */
  public File getFile()
  {
    return this.file;
  }
}


/*********************************************************************
 * $Log: FileChangedMessage.java,v $
 * Revision 1.1  2007/03/09 18:03:08  willuhn
 * @N classloader updates
 * @N FileWatch in de.willuhn.util
 * @N Ueberwachung der Programm-Dateien auf Aenderungen
 *
 **********************************************************************/