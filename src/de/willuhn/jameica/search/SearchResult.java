/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.search;

import java.rmi.RemoteException;
import java.util.List;

import de.willuhn.util.ApplicationException;


/**
 * Implementierung des Suchergebnisses fuer einen Provider.
 */
public class SearchResult
{
  private SearchProvider provider = null;
  private String text             = null;

  /**
   * ct.
   * @param provider der SearchProvider.
   * @param text der Suchbegriff.
   */
  public SearchResult(SearchProvider provider, String text)
  {
    this.provider = provider;
    this.text     = text;
  }

  /**
   * Liefert das Suchergebnis.
   * @return das Suchergebnis.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public List getResult() throws RemoteException, ApplicationException
  {
    return this.provider.search(this.text);
  }

  /**
   * Liefert den zugehoerigen SearchProvider.
   * @return der SearchProvider.
   */
  public SearchProvider getSearchProvider()
  {
    return this.provider;
  }
}


/**********************************************************************
 * $Log: SearchResult.java,v $
 * Revision 1.1  2008/09/03 00:11:43  willuhn
 * @N Erste Version eine funktionsfaehigen Suche - zur Zeit in Navigation.java deaktiviert
 *
 **********************************************************************/
