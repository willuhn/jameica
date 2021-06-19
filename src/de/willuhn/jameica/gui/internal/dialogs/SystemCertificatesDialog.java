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

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.internal.parts.CertificateList;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Dialog zum Anzeigen der System-Zertifikate.
 */
public class SystemCertificatesDialog extends AbstractDialog<Object>
{
  private static final int WINDOW_WIDTH  = 600;
  private static final int WINDOW_HEIGHT = 500;
  
  /**
   * ct.
   * @param position
   */
  public SystemCertificatesDialog(int position)
  {
    super(position);
    this.setTitle(i18n.tr("Aussteller-Zertifikate von Java"));
    this.setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container c = new SimpleContainer(parent);
    c.addText(i18n.tr("Liste der Aussteller-Zertifikate, die bereits in Ihrer Java-Installation enthalten sind."),true);
    
    CertificateList list = new CertificateList(Application.getSSLFactory().getTrustManager().getSystemTrustManager(),false);
    list.paint(parent);
    
    Container c2 = new SimpleContainer(parent);
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Schließen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true,"process-stop.png");
    c2.addButtonArea(buttons);
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
 * $Log: SystemCertificatesDialog.java,v $
 * Revision 1.1  2011/06/27 17:51:43  willuhn
 * @N Man kann sich jetzt die Liste der von Java bereits mitgelieferten Aussteller-Zertifikate unter Datei->Einstellungen anzeigen lassen - um mal einen Ueberblick zu kriegen, wem man so eigentlich alles blind vertraut ;)
 * @N Mit der neuen Option "Aussteller-Zertifikaten von Java vertrauen" kann man die Vertrauensstellung zu diesen Zertifikaten deaktivieren - dann muss der User jedes Zertifikate explizit bestaetigen - auch wenn Java die CA kennt
 *
 **********************************************************************/