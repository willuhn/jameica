/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/rmi/Attic/Service.java,v $
 * $Revision: 1.2 $
 * $Date: 2003/10/29 00:41:27 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Service extends Remote 
{

  public void open() throws RemoteException;
	
  public void close() throws RemoteException;

}

/*********************************************************************
 * $Log: Service.java,v $
 * Revision 1.2  2003/10/29 00:41:27  willuhn
 * *** empty log message ***
 *
 **********************************************************************/