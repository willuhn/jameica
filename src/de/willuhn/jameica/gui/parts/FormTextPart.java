/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.internal.action.Program;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Freiformatierbarer Text.
 */
public class FormTextPart implements Part {

  private StringBuilder content        = new StringBuilder();
  private ScrolledComposite container = null;
  private FormText text               = null;

  /**
   * ct.
   */
  public FormTextPart()
  {
  }

  /**
   * ct.
   * @param text der anzuzeigenden Text.
   */
  public FormTextPart(String text)
  {
    setText(text);
  }
  
  /**
   * ct.
   * @param text die PlainText-Datei.
   * @throws IOException Wenn beim Lesen der Datei Fehler auftreten.
   */
  public FormTextPart(Reader text) throws IOException
  {
    setText(text);
  }

  /**
   * Zeigt den Text aus der uebergebenen Datei an.
   * @param text anzuzeigender Text.
   * @throws IOException
   */
  public void setText(Reader text) throws IOException
  {
    try(BufferedReader br = new BufferedReader(text);) {
      String thisLine = null;
      StringBuilder builder = new StringBuilder();
      while ((thisLine = br.readLine()) != null)
      {
        if (thisLine.length() == 0) // Leerzeile
        {
          builder.append("\n\n");
          continue;
        }
        builder.append(thisLine.trim() + " "); // Leerzeichen am Ende einfuegen.
      }
      content = builder; // machen wir erst wenn die gesamte Datei gelesen werden konnte
      refresh();
    }
  }

  /**
   * Zeigt den uebergebenen Hilfe-Text an.
   * @param s anzuzeigender Hilfe-Text.
   */
  public void setText(String s)
  {
    content = new StringBuilder(s);
    refresh();
  }


  /**
   */
  public void refresh()
  {
    if (text == null || content == null)
      return;
    String s = content.toString();
    boolean b = s != null && s.startsWith("<form>");
    text.setText(s == null ? "" : s,b,b);
    resize();
  }

  /**
   * Passt die Groesse des Textes an die Umgebung an.
   */
  private void resize()
  {
    if (text == null || container == null)
      return;
    text.setSize(text.computeSize(text.getParent().getClientArea().width,SWT.DEFAULT));
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException {

    container = new ScrolledComposite(parent,SWT.H_SCROLL | SWT.V_SCROLL);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.horizontalIndent = 4;
    container.setLayoutData(gd);
    container.setLayout(SWTUtil.createGrid(1,true));
    container.addListener(SWT.Resize, new Listener() {
      public void handleEvent(Event event) {
        resize();
      }
    });

    text = new FormText(container,SWT.WRAP);

    // Die HyperlinkSettings muessen zwingend an den
    // FormText uebergeben werden, BEVOR der eigentliche Text uebergeben
    // wird. Das gibts sonst eine NPE. Und das laesst sich extrem
    // schlecht debuggen.
    // Den Quellcode von eclipse.ui.forms hab ich hier gefunden:
    // http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.ui.forms/src/org/eclipse/ui/internal/forms/widgets/?hideattic=0
    
    // Daher machen wir das gleich als erstes. Sicher ist sicher.
    HyperlinkSettings hs = new HyperlinkSettings(GUI.getDisplay());
    hs.setBackground(Color.BACKGROUND.getSWTColor());
    hs.setForeground(Color.LINK.getSWTColor());
    hs.setActiveBackground(Color.BACKGROUND.getSWTColor());
    hs.setActiveForeground(Color.LINK_ACTIVE.getSWTColor());
    text.setHyperlinkSettings(hs);

    text.setFont(Font.DEFAULT.getSWTFont());

    text.setColor("header",Color.COMMENT.getSWTColor());
    text.setColor("error",Color.ERROR.getSWTColor());
    text.setColor("success",Color.SUCCESS.getSWTColor());
    text.setFont("header", Font.H1.getSWTFont());

    container.setContent(text);

    text.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      public void linkActivated(HyperlinkEvent e) {
        Object href = e.getHref();
        if (href == null)
        {
          Logger.info("got hyperlink event, but data was null. nothing to do");
          return;
        }
        if (!(href instanceof String))
        {
          Logger.info("got hyperlink event, but data is not a string, skipping");
          return;
        }
        String action = (String) href;
        
        // Wir versuchen die Action als Klasse zu laden. Wenn das fehlschlaegt,
        // starten wir die Action einfach als Programm
        Logger.info("executing action \"" + action);
        try
        {
          Logger.debug("trying to load class " + action);
          Class<Action> c = Application.getClassLoader().load(action);
          BeanService beanService = Application.getBootLoader().getBootable(BeanService.class);
          Action a = beanService.get(c);
          a.handleAction(e);
          return;
        }
        catch (Throwable t)
        {
          // ignore
        }
        
        // Fallback
        try
        {
          new Program().handleAction(action);
        }
        catch (ApplicationException ae)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
        }
      }
    });

    refresh();
  }
}


/**********************************************************************
 * $Log: FormTextPart.java,v $
 * Revision 1.19  2011/08/30 16:02:23  willuhn
 * @N Alle restlichen Stellen, in denen Instanzen via Class#newInstance erzeugt wurden, gegen BeanService ersetzt. Damit kann jetzt quasi ueberall Dependency-Injection verwendet werden, wo Jameica selbst die Instanzen erzeugt
 *
 * Revision 1.18  2011-05-03 10:13:10  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.17  2011-04-26 12:09:17  willuhn
 * @B Potentielle Bugs gemaess Code-Checker
 *
 * Revision 1.16  2010-10-28 22:08:44  willuhn
 * @N HyperlinkSettings so frueh wie moeglich setzen - wegen moeglicher NPE
 *
 * Revision 1.14  2007-12-21 13:46:27  willuhn
 * @N H2-Migration scharf geschaltet
 *
 * Revision 1.13  2007/05/14 11:18:09  willuhn
 * @N Hoehe der Statusleiste abhaengig von DPI-Zahl und Schriftgroesse
 * @N Default-Schrift konfigurierbar und Beruecksichtigung dieser an mehr Stellen
 *
 * Revision 1.12  2007/03/29 15:29:48  willuhn
 * @N Uebersichtlichere Darstellung der Systemstart-Meldungen
 *
 * Revision 1.11  2005/08/25 21:18:24  web0
 * @C changes accoring to findbugs eclipse plugin
 *
 * Revision 1.10  2005/08/15 13:15:32  web0
 * @C fillLayout removed
 *
 * Revision 1.9  2005/03/31 22:35:37  web0
 * @N flexible Actions fuer FormTexte
 *
 * Revision 1.8  2005/03/09 01:06:36  web0
 * @D javadoc fixes
 *
 * Revision 1.7  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/08/18 23:14:19  willuhn
 * @D Javadoc
 *
 * Revision 1.5  2004/07/31 15:03:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/07/09 00:12:46  willuhn
 * @C Redesign
 *
 * Revision 1.3  2004/05/23 16:34:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 **********************************************************************/