/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/views/Attic/UserErrorView.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/01/05 16:10:46 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.internal.action.Back;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Diese Fehlerseite wird angezeigt, wenn eine andere View in der
 * Methode bind() eine ApplicationException nicht gefangen hat. Der Text dieser
 * Exception wird dann hier angezeigt. Zum Verstaendnis: Eine UserErrorView
 * kann durchaus im regulaeren Betrieb verwendet werden. Allerdings sollte die View
 * dann aber darauf achten, dass Exceptions mit lokalisierten und fuer den
 * Benutzer verstaendlichen Texten geworfen werden. Im Gegensatz dazu wird
 * die View <code>FatalErrorView</code> wenn eine Exception != ApplicationException
 * aufgetreten ist, der nicht zum geplanten Anwendungsablauf gehoert. Daher wird
 * dort dann auch der gesamte Stacktrace ausgegeben. Hier jedoch nicht.
 * Sprich: Dieser Dialog hier kann bei regulaeren und einkalkulierten
 * Anwendungsfehlern verwendet werden.
 */
public class UserErrorView extends AbstractView
{

  /**
   * Die Exception steht zwar hier in der Methoden-Signatur
   * drin, wird jedoch nie geworfen, weil es sonst zu einem
   * Loop kommen wuerde.
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
  	I18N i18n = Application.getI18n();
		LabelGroup group = new LabelGroup(getParent(),i18n.tr("Fehler"));
		String unknownError = "Es ist ein unerwarteter Fehler aufgetreten.\n" +
			"Prüfen Sie bitte das Systemprotokoll (durch Klick auf\n" +
			"den linken Teil der Status-Leiste im unteren Fensterrand).";

		Exception e = (Exception) getCurrentObject();
  		
		if (e != null && e.getMessage() != null)
		  group.addText(e.getMessage(),true);
		else
  		group.addText(unknownError,false);

		ButtonArea buttons = new ButtonArea(getParent(),1);
    buttons.addButton(i18n.tr("Zurück"),new Back(),null,true);
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
  }

}


/**********************************************************************
 * $Log: UserErrorView.java,v $
 * Revision 1.1  2006/01/05 16:10:46  web0
 * @C error handling
 *
 **********************************************************************/