/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Main.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/10/23 21:49:46 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica;

public class Main {

  public static void main(String[] args) {
  	if (args.length == 0)
  	{
			Application.newInstance(false);
  	}
	  else {
			Application.newInstance("-server".equalsIgnoreCase(args[0]));
	  }
  }

}


/*********************************************************************
 * $Log: Main.java,v $
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
