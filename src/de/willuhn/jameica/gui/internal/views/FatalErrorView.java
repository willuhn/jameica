/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/views/FatalErrorView.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/10/08 13:38:20 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.views;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * View, die gestartet wird, wenn der Main-GUI Loop gecrasht ist.
 */
public class FatalErrorView extends AbstractView
{

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception
  {
  	final I18N i18n = Application.getI18n();

		try {
			GUI.getView().setTitle(i18n.tr("Fehler"));
	    
	    Throwable t = (Throwable) getCurrentObject();
	
	    LabelGroup stacktrace = new LabelGroup(getParent(),"Unerwarteter Fehler");
	    
	    stacktrace.addText(i18n.tr("Es ist ein unerwarteter Fehler aufgetreten.\n" +	      "Wenn Sie sich aktiv an der Verbesserung dieser Software beteiligen möchten,\n" +	      "dann klicken Sie einfach auf \"in Zwischenablage kopieren\", öffnen Ihr\n" +	      "Mailprogramm, fügen den Fehlertext via \"Bearbeiten/Einfügen\" in eine neue\n" +	      "Mail ein und senden ihn zusammen mit ihrer Beschreibung an den Autor dieses Programms.\n" +	      "Vielen Dank."),false);
	
			String e1 = "";
			String e2 = "";
			if (t != null)
			{
		    stacktrace.addHeadline("Stacktrace");
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    t.printStackTrace(new PrintStream(bos));
		    e1 = bos.toString();
        e1 = e1.replaceAll("\r\n","\n");
		    stacktrace.addText(e1,false);
		
		    e2 = "";
		    Throwable t2 = t.getCause();
		    if (t2 != null)
		    {
		      stacktrace.addHeadline("caused by");
		  
		      bos = new ByteArrayOutputStream();
		      t2.printStackTrace(new PrintStream(bos));
		      e2 += bos.toString();
		      stacktrace.addText(e2,false);
		    }
		  }
	    final String s = e1 + e2;
	    ButtonArea buttons = new ButtonArea(getParent(),1);
	    buttons.addCustomButton(i18n.tr("in Zwischenablage kopieren"),new Listener()
      {
        public void handleEvent(Event event)
        {
	        final Clipboard cb = new Clipboard(GUI.getDisplay());
	        TextTransfer textTransfer = TextTransfer.getInstance();
	        String[] logEntries = Logger.getLastLines();
	        StringBuffer sb = new StringBuffer();
	        for (int i=0;i<logEntries.length;++i)
	        {
	          sb.append(logEntries[i]);
	        }
	        final String log = "\n" + i18n.tr("Auszug aus dem Systemprotokoll") + ":\n" + sb.toString();
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
				Logger.error("unable to show fatal error view, giving up and exiting application",e);
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
 * Revision 1.1  2004/10/08 13:38:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.13  2004/07/20 22:52:49  willuhn
 * @C Refactoring
 *
 * Revision 1.12  2004/07/09 00:12:47  willuhn
 * @C Redesign
 *
 * Revision 1.11  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.9  2004/03/30 22:08:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/03/24 00:46:02  willuhn
 * @C refactoring
 *
 * Revision 1.7  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.6  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.5  2004/02/26 18:47:03  willuhn
 * *** empty log message ***
 *
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