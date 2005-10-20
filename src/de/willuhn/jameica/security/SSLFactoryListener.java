/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/Attic/SSLFactoryListener.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/10/20 23:21:24 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.security;

/**
 * Ein Listener, der an der SSLFactory registriert werden kann um
 * informiert zu werden, wenn sich die Liste der Stammzertifikate
 * geaendert hat. Sprich, wenn ein Zertifikat hinzugefuegt oder
 * geloescht wurde.
 */
public interface SSLFactoryListener
{
  /**
   * Wird aufgerufen, wenn sich der SSLContext geaendert hat.
   */
  public void sslContextChanged();
}


/*********************************************************************
 * $Log: SSLFactoryListener.java,v $
 * Revision 1.1  2005/10/20 23:21:24  web0
 * @C Network support
 *
 **********************************************************************/