/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/ErrorView.java,v $
 * $Revision: 1.6 $
 * $Date: 2004/01/23 00:29:03 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.views;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.views.parts.LabelGroup;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Diese Fehlerseite wird angezeigt, wenn eine andere View in der
 * Methode bind() eine Exception nicht gefangen hat. Der Text dieser
 * Exception wird dann hier angezeigt. Zum Verstaendnis: Eine ErrorView
 * kann durchaus im regulaeren Betrieb verwendet werden, indem die
 * View ihre Exceptions nicht faengt. Allerdings sollte die View dann
 * aber darauf achten, dass Exception mit lokalisierten und fuer den
 * Benutzer verstaendlichen Texten geworfen werden. Im Gegensatz dazu wird
 * die View <code>FatalErrorView</code> wenn ein fataler Fehler aufgetreten
 * ist, der nicht zum geplanten Anwendungsablauf gehoert. Daher wird
 * dort dann auch der gesamte Stacktrace ausgegeben. Hier jedoch nicht.
 * Sprich: Dieser Dialog hier kann bei regulaeren und einkalkulierten
 * Anwendungsfehlern verwendet werden.
 */
public class ErrorView extends AbstractView
{

  /**
   * @param parent
   */
  public ErrorView(Composite parent)
  {
    super(parent);
  }

  /**
   * Die Exception steht zwar hier in der Methoden-Signatur
   * drin, wird jedoch nie geworfen, weil es sonst zu einem
   * Loop kommen wuerde.
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception
  {
		LabelGroup group = new LabelGroup(getParent(),I18N.tr("Fehler"));
		String unknownError = "Es ist ein unerwarteter Fehler aufgetreten.\n" +
			"Prüfen Sie bitte das Systemprotokoll (durch Klick auf\n" +
			"den linken Teil der Status-Leiste im unteren Fensterrand).";

  	try {
  		Exception e = (Exception) getCurrentObject();
  		
  		if (e != null)
	  		group.addText(e.getLocalizedMessage(),true);
	  	else
	  		group.addText(unknownError,false);
  	}
  	catch (Exception e)
  	{
			group.addText(unknownError,false);
  		Application.getLog().error("exception while binding error page",e);
  	}
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
  }

}


/**********************************************************************
 * $Log: ErrorView.java,v $
 * Revision 1.6  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 **********************************************************************/