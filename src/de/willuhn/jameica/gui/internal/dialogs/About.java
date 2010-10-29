/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/dialogs/About.java,v $
 * $Revision: 1.13 $
 * $Date: 2010/10/29 09:32:59 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * About-Dialog.
 */
public class About extends AbstractDialog
{

  /**
   * ct.
   * @param position
   */
  public About(int position)
  {
    super(position,false);
    this.setTitle(i18n.tr("‹ber ..."));
    this.setPanelText(i18n.tr("Jameica {0}",Application.getManifest().getVersion().toString()));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Label l = GUI.getStyleFactory().createLabel(parent,SWT.BORDER);
    l.setImage(SWTUtil.getImage("splash.png"));

    Container container = new LabelGroup(parent, i18n.tr("Versionsinformationen"),true);
    
    FormTextPart text = new FormTextPart();
    text.setText("<form>" +
      "<p><b>Jameica - Java based message interchange</b></p>" +
      "<p>Lizenz: GPL [<a href=\"http://www.gnu.org/copyleft/gpl.html\">www.gnu.org/copyleft/gpl.html</a>]<br/>" +
      "Copyright by Olaf Willuhn [<a href=\"mailto:info@jameica.org\">info@jameica.org</a>]<br/>" +
      "<a href=\"http://www.jameica.org\">www.jameica.org</a></p>" +
      "<p>Version: " + Application.getManifest().getVersion() + "<br/>" +
      "SWT-Version: " + SWT.getVersion() + " / " + SWT.getPlatform() + "<br/>" +
      "Build: " + Application.getBuildnumber() + " [Datum " + Application.getBuildDate() + "]</p>" +
      "<p>Benutzerverzeichnis: " + Application.getConfig().getWorkDir() + "</p>" +
      "</form>");

    container.addPart(text);

    ButtonArea buttons = container.createButtonArea(1);
    buttons.addButton(i18n.tr("Schlieﬂen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true,"window-close.png");
    setSize(SWT.DEFAULT,480);  // BUGZILLA 269
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

}


/**********************************************************************
 * $Log: About.java,v $
 * Revision 1.13  2010/10/29 09:32:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2010-10-29 09:31:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2010-10-29 09:31:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2010-10-29 09:24:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2010-10-28 22:48:13  willuhn
 * @C Groesse nicht mehr aenderbar
 *
 * Revision 1.8  2010-10-11 20:46:22  willuhn
 * @N BUGZILLA 928
 *
 * Revision 1.7  2010-09-06 20:44:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2009/03/10 23:51:28  willuhn
 * @C PluginResources#getPath als deprecated markiert - stattdessen sollte jetzt Manifest#getPluginDir() verwendet werden
 *
 * Revision 1.5  2008/07/18 10:41:30  willuhn
 * @N Zeitgesteuertes Ausfuehren von Reminder-Actions
 *
 * Revision 1.4  2008/04/15 16:16:36  willuhn
 * @B BUGZILLA 584
 *
 * Revision 1.3  2007/12/18 14:12:22  willuhn
 * @N Neuer Splash-Screen - wurde ja auch mal Zeit ;)
 *
 * Revision 1.2  2006/08/29 11:16:58  willuhn
 * @B Bug 269
 *
 * Revision 1.1  2005/11/07 19:07:59  web0
 * @N Update auf SWT 3.1
 *
 * Revision 1.6  2005/07/14 20:24:05  web0
 * *** empty log message ***
 *
 * Revision 1.5  2005/03/31 22:35:37  web0
 * @N flexible Actions fuer FormTexte
 *
 * Revision 1.4  2005/01/13 19:31:38  willuhn
 * @C SSLFactory geaendert
 * @N Settings auf property-Format umgestellt
 *
 * Revision 1.3  2004/11/05 20:00:43  willuhn
 * @D javadoc fixes
 *
 * Revision 1.2  2004/10/12 23:49:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/08 13:38:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.4  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.3  2004/04/26 22:57:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/04/26 22:42:17  willuhn
 * @N added InfoReader
 *
 * Revision 1.1  2004/04/14 23:53:44  willuhn
 * *** empty log message ***
 *
 **********************************************************************/