/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/ErrorView.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/12/09 11:38:50 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.views;

import de.willuhn.jameica.I18N;
import de.willuhn.jameica.views.parts.Headline;
import de.willuhn.jameica.views.parts.LabelGroup;
import de.willuhn.jameica.views.parts.LabelInput;


public class ErrorView extends AbstractView
{

  public ErrorView(Object o)
  {
    super(o);
  }

  public void bind()
  {

    new Headline(getParent(),I18N.tr("Fehler"));
    
    Exception e = (Exception) getCurrentObject();
    LabelGroup stacktrace = new LabelGroup(getParent(),I18N.tr("Fehlertext: ") + e.getClass() + ":" + e.getLocalizedMessage());
    
    StackTraceElement[] lines = e.getStackTrace();
    for (int i=0;i<lines.length;++i)
    {
      LabelInput line = new LabelInput(" at " + lines[i].getClassName() + "("+lines[i].getFileName()+":"+lines[i].getLineNumber() +")");
      stacktrace.addLabelPair(" ",line);
    }
  }        


  public void unbind()
  {
  }

}

/***************************************************************************
 * $Log: ErrorView.java,v $
 * Revision 1.1  2003/12/09 11:38:50  willuhn
 * @N error page
 *
 * Revision 1.5  2003/12/05 17:12:23  willuhn
 * @C SelectInput
 *
 * Revision 1.4  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 * Revision 1.3  2003/10/29 00:41:27  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/10/23 22:36:34  willuhn
 * @N added Menu
 *
 * Revision 1.1  2003/10/23 21:50:06  willuhn
 * initial checkin
 *
 ***************************************************************************/