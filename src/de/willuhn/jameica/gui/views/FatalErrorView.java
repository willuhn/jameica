/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/FatalErrorView.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/02/22 20:05:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.views;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.LabelGroup;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

public class FatalErrorView extends AbstractView
{

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception
  {

		try {
			GUI.setTitleText(I18N.tr("Fehler"));
	    
	    Exception e = (Exception) getCurrentObject();
	
	    LabelGroup stacktrace = new LabelGroup(getParent(),"Unerwarteter Fehler");
	    
	    stacktrace.addText(I18N.tr("Es ist ein unerwarteter Fehler aufgetreten.\n" +	      "Wenn Sie sich aktiv an der Verbesserung dieser Software beteiligen möchten,\n" +	      "dann klicken Sie einfach auf \"in Zwischenablage kopieren\", öffnen Ihr\n" +	      "Mailprogramm, fügen den Fehlertext via \"Bearbeiten/Einfügen\" in eine neue\n" +	      "Mail ein und senden ihn zusammen mit ihrer Beschreibung an den Autor dieses Programms.\n" +	      "Vielen Dank."),false);
	
			String e1 = "";
			String e2 = "";
			if (e != null)
			{
		    stacktrace.addHeadline("Stacktrace");
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    e.printStackTrace(new PrintStream(bos));
		    e1 = bos.toString();
		    stacktrace.addText(e1,false);
		
		    e2 = "";
		    Throwable t = e.getCause();
		    if (t != null)
		    {
		      stacktrace.addHeadline("caused by");
		  
		      bos = new ByteArrayOutputStream();
		      t.printStackTrace(new PrintStream(bos));
		      e2 += bos.toString();
		      stacktrace.addText(e2,false);
		    }
		  }
	    final String s = e1 + e2;
	    ButtonArea buttons = new ButtonArea(getParent(),1);
	    buttons.addCustomButton(I18N.tr("in Zwischenablage kopieren"),new MouseAdapter()
	    {
	      public void mouseUp(MouseEvent e)
	      {
	        final Clipboard cb = new Clipboard(GUI.getDisplay());
	        TextTransfer textTransfer = TextTransfer.getInstance();
	        String[] logEntries = Application.getLog().getLastLines();
	        StringBuffer sb = new StringBuffer();
	        for (int i=0;i<logEntries.length;++i)
	        {
	          sb.append(logEntries[i]);
	        }
	        final String log = "\n" + I18N.tr("Auszug aus dem Systemprotokoll") + ":\n" + sb.toString();
	        cb.setContents(new Object[]{(s + log)}, new Transfer[]{textTransfer});
	      }
	    });

		}
		catch (Exception e)
		{
			// Hui, extrem boese. Nicht mal das Anzeigen der FatalErrorView
			// klappt. Dann koemmer die ganze Anwendung eigentlich in
			// die Tonne treten.
			try {
				Application.getLog().error("unable to show fatal error view, giving up and exiting application",e);
			}
			catch (Exception e2)
			{
				System.exit(1);
			}
		}
  }        

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
  }

}

/***************************************************************************
 * $Log: FatalErrorView.java,v $
 * Revision 1.4  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 * Revision 1.3  2004/02/20 20:45:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/01/28 20:51:25  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.1  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.4  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.2  2003/12/10 00:47:12  willuhn
 * @N SearchDialog done
 * @N FatalErrorView
 *
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