/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/FileChangedMessage.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/03/09 18:03:08 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
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