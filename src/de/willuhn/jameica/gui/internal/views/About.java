/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/views/Attic/About.java,v $
 * $Revision: 1.5 $
 * $Date: 2005/03/31 22:35:37 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.internal.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.internal.action.Program;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * About-Dialog.
 */
public class About extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {
  	I18N i18n = Application.getI18n();

		Label l = new Label(getParent(),SWT.BORDER);
		l.setImage(SWTUtil.getImage("splash.jpg"));

		FormTextPart text = new FormTextPart();
		text.setText("<form>" +
			"<p><b>Jameica - Java based message interchange</b></p>" +			"<br/><p>Licence: GPL [<a href=\"" + Program.class.getName() + "\">http://www.gnu.org/copyleft/gpl.html</a>]</p>" +
			"<br/><p>Copyright by Olaf Willuhn [<a href=\"" + Program.class.getName() + "\">mailto:info@jameica.org</a>]</p>" +			"<p><a href=\"" + Program.class.getName() + "\">http://www.jameica.org</a></p>" +			"</form>");

		text.paint(getParent());

  	LabelGroup group = new LabelGroup(getParent(),i18n.tr("Jameica"));
 	
  	group.addLabelPair(i18n.tr("Version"), 					new LabelInput(""+Application.getManifest().getVersion()));
		group.addLabelPair(i18n.tr("Datenverzeichnis"), new LabelInput(""+Application.getConfig().getWorkDir()));

  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: About.java,v $
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