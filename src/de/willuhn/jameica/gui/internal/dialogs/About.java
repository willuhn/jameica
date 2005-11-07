/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/dialogs/About.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/11/07 19:07:59 $
 * $Author: web0 $
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
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.internal.action.Program;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

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
    super(position);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    I18N i18n = Application.getI18n();

    setTitle(i18n.tr("About"));

    Label l = new Label(parent,SWT.BORDER);
    l.setImage(SWTUtil.getImage("splash.jpg"));

    FormTextPart text = new FormTextPart();
    text.setText("<form>" +
      "<p><b>Jameica - Java based message interchange</b></p>" +
      "<br/><p>Licence: GPL [<a href=\"" + Program.class.getName() + "\">http://www.gnu.org/copyleft/gpl.html</a>]</p>" +
      "<br/><p>Copyright by Olaf Willuhn [<a href=\"" + Program.class.getName() + "\">mailto:info@jameica.org</a>]</p>" +
      "<p><a href=\"" + Program.class.getName() + "\">http://www.jameica.org</a></p>" +
      "<br/><p>Version: " + Application.getManifest().getVersion() + "</p>" +
      "<br/><p>SWT-Version: " + SWT.getVersion() + " / " + SWT.getPlatform() + "</p>" +
      "<br/><p>Build: " + Application.getBuildnumber() + " [Datum " + Application.getBuildDate() + "]</p>" +
      "<br/><p>Work-Dir: " + Application.getConfig().getWorkDir() + "</p>" +
      "</form>");

    text.paint(parent);

    ButtonArea buttons = new ButtonArea(parent,1);
    buttons.addButton("   " + i18n.tr("OK") + "   ",new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true);
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