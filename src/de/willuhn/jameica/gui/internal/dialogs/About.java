/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui.internal.dialogs;

import org.apache.commons.lang.StringEscapeUtils;
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
      "<p><b>Jameica Application Platform</b></p>" +
      "<p>Lizenz: GPL [<a href=\"http://www.gnu.org/copyleft/gpl.html\">www.gnu.org/copyleft/gpl.html</a>]<br/>" +
      "Copyright by Olaf Willuhn [<a href=\"mailto:info@jameica.org\">info@jameica.org</a>]<br/>" +
      "<a href=\"http://www.jameica.org\">www.jameica.org</a></p>" +
      "<p>Version: " + Application.getManifest().getVersion() + "<br/>" +
      "SWT-Version: " + SWT.getVersion() + " / " + SWT.getPlatform() + "<br/>" +
      "Java-Version: " + System.getProperty("java.version") + " / " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + "<br/>" +
      "[Datum " + Application.getBuildDate() + "]</p>" +
      "<p>Benutzerverzeichnis: " + StringEscapeUtils.escapeXml(Application.getConfig().getWorkDir()) + "</p>" +
      "</form>");

    container.addPart(text);

    ButtonArea buttons = container.createButtonArea(1);
    buttons.addButton(i18n.tr("Schlieﬂen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true,"window-close.png");
    setSize(SWT.DEFAULT,600);  // BUGZILLA 269
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

}
