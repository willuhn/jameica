/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Main.java,v $
 * $Revision: 1.2 $
 * $Date: 2003/10/29 00:41:26 $
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

  /**
   * starts jameica.
   * @param args
   */
  public static void main(String[] args) {
    String serverParam = "-server";

    if (args.length == 0)
    {
      // no parameter given -> starting in GUI mode with default config location
      Application.newInstance(false,null);
    }
    else if (args.length == 1 && serverParam.equalsIgnoreCase(args[0])) {
      // 1 parameter given and param is "-server" -> starting in server mode with default config location
      Application.newInstance(true,null);
    }
    else if (args.length == 1 && !serverParam.equalsIgnoreCase(args[0])) {
      // 1 parameter given and param is not "-server" -> starting in GUI mode with given config location
      Application.newInstance(false,args[0]);
    }
    else {
      // more than 1 parameter given -> starting with given data.
      Application.newInstance(serverParam.equalsIgnoreCase(args[0]),args[1]);
    }
  }


}


/*********************************************************************
 * $Log: Main.java,v $
 * Revision 1.2  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
