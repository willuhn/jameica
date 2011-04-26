/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/views/FatalErrorView.java,v $
 * $Revision: 1.10 $
 * $Date: 2011/04/26 11:48:08 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.views;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.logging.Message;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * View, die gestartet wird, wenn der Main-GUI Loop gecrasht ist.
 */
public class FatalErrorView extends AbstractView
{
  private final static DateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd");
  private final static I18N i18n             = Application.getI18n();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
		try {
			GUI.getView().setTitle(i18n.tr("Fehler"));
	    
	    Throwable t = (Throwable) getCurrentObject();
	
	    Container group = new SimpleContainer(getParent());
	    group.addHeadline(i18n.tr("Unerwarteter Fehler"));
	    group.addText(i18n.tr("Es ist ein unerwarteter Fehler aufgetreten."),true,Color.ERROR);
	    group.addText(i18n.tr("Wenn Sie sich aktiv an der Verbesserung dieser Software beteiligen möchten, " +                    	      "dann klicken Sie einfach auf \"Diagnose-Protokoll speichern\" und senden " +                    	      "Sie die Datei zusammen mit einer kurzen Beschreibung per Mail an den Autor des Plugins.\n\n" +                    	      "Vielen Dank."),true);
	
	    final String s = toString(t);

	    if (t != null)
			{
			  Container stacktrace = new SimpleContainer(getParent(),true);
			  stacktrace.addHeadline(i18n.tr("Stacktrace"));
			  TextAreaInput text = new TextAreaInput(s);
			  text.paint(stacktrace.getComposite());
			  ((Text)text.getControl()).setEditable(false);
		  }

	    ButtonArea buttons = new ButtonArea();
	    buttons.addButton(i18n.tr("Diagnose-Protokoll speichern"),new Action()
      {
        public void handleAction(Object context) throws ApplicationException
        {
          FileDialog fd = new FileDialog(GUI.getShell(),SWT.SAVE);
          fd.setFileName("diagnose-" + DATEFORMAT.format(new Date()) + ".log");
          fd.setFilterExtensions(new String[]{"*.log"});
          fd.setFilterPath(System.getProperty("user.home"));
          fd.setOverwrite(true);
          
          String f = fd.open();
          if (f == null || f.length() == 0)
            return;
          
          Writer writer = null;
          try
          {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(f)),"iso-8859-1"));

            writer.write("*** " + i18n.tr("System-Informationen") + " ***\n\n");
            writer.write(getSystemStats() + "\n\n");

            // Der Stack-Trace:
            writer.write("*** " + i18n.tr("Stacktrace") + " ***\n\n");
            writer.write(s + "\n\n");

            writer.write("*** " + i18n.tr("Systemprotokoll") + " ***\n\n");
            for (Message m:Logger.getLastLines())
              writer.write(m + "\n");
            
            writer.close();
            writer = null;
            
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Diagnose-Protokoll gespeichert"),StatusBarMessage.TYPE_SUCCESS));
          }
          catch (Exception e)
          {
            Logger.error("error while writing diagnostic file",e);
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Speichern: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
          }
          finally
          {
            if (writer != null)
            {
              try {
                writer.close();
              } catch (Exception e) {/* useless */}
            }
          }
        }
      },null,false,"document-save.png");
	    buttons.paint(getParent());
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
   * Liefert den Stacktrace des Fehlers.
   * @param t der Fehler.
   * @return der Stacktrace.
   */
  private String toString(Throwable t)
  {
    if (t == null)
      return "";
    
    StringBuffer sb = new StringBuffer();

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    t.printStackTrace(new PrintStream(bos));
    sb.append(bos.toString());
    return sb.toString().replaceAll("\r\n","\n");
  }
  
  /**
   * Liefert einen String mit den Versionen und installierten Plugins.
   * @return String mit Versions-Infos.
   */
  private String getSystemStats()
  {
    StringBuffer sb = new StringBuffer();
    
    sb.append("Jameica: " + Application.getManifest().getVersion() + " (build date: " + Application.getManifest().getBuildDate() + ")\n\n");
    sb.append("Plugins:\n");
    List<Manifest> l = Application.getPluginLoader().getInstalledManifests();
    for (Manifest mf:l)
    {
      sb.append("  " + mf.getName() + " " + mf.getVersion() + "(build date: " + mf.getBuildDate() + ")\n");
    }

    sb.append("\n");

    sb.append("os.arch          : " + System.getProperty("os.arch") + "\n");
    sb.append("os.name          : " + System.getProperty("os.name") + "\n");
    sb.append("os.version       : " + System.getProperty("os.version") + "\n");

    sb.append("\n");

    sb.append("java.version     : " + System.getProperty("java.version") + "\n");
    sb.append("java.vendor      : " + System.getProperty("java.vendor") + "\n");
    sb.append("java.runtime.name: " + System.getProperty("java.runtime.name") + "\n");
    sb.append("java.vm.name     : " + System.getProperty("java.vm.name") + "\n");

    sb.append("\n");

    sb.append("file.encoding    : " + System.getProperty("file.encoding") + "\n");
    return sb.toString();
  }

}

/***************************************************************************
 * $Log: FatalErrorView.java,v $
 * Revision 1.10  2011/04/26 11:48:08  willuhn
 * @R Back-Button entfernt
 *
 * Revision 1.9  2010-11-25 11:34:08  willuhn
 * @N Error-View ueberarbeitet. Das System-Protokoll wird jetzt nicht mehr in die Zwischenablage kopiert sondern als Datei gespeichert. Das kann man als Attachment speichern und ist erheblich besser lesbar (keine Zeilenumbrueche in den Log-Ausgaben). Ausserdem enthaelt es jetzt auch noch Versionsinformationen zu OS, Java, Jameica und Plugins.
 *
 ***************************************************************************/