/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/ButtonArea.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/06/30 20:58:39 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Diese Klasse erzeugt standardisierte Bereiche fuer die Dialog-Buttons.
 * @author willuhn
 */
public class ButtonArea
{
	private I18N i18n;

  private Composite buttonArea;
  private Button storeButton; // den Store-Button speichern wir extra damit wir ihn
                              // deaktivieren koennen, wenn Fehler auf dem Dialog sind.

  /**
   * Erzeugt einen neuen Standard-Button-Bereich.
   * @param parent Composite, in dem die Buttons gezeichnet werden sollen.
   * @param numButtons Anzahl der Buttons, die hier drin gespeichert werden sollen.
   */
  public ButtonArea(Composite parent, int numButtons)
  {
		i18n = Application.getI18n();
    GridLayout layout = new GridLayout();
    layout.marginHeight=0;
    layout.marginWidth=0;
    layout.numColumns = numButtons;

    buttonArea = new Composite(parent, SWT.NONE);
		buttonArea.setBackground(Color.BACKGROUND.getSWTColor());
    buttonArea.setLayout(layout);
    buttonArea.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
  }

  /**
   * Fuegt der Area einen Erstellen-Button hinzu.
   * Beim Click wird die Methode handleCreate() des Controllers ausgefuehrt.
   * @param name Bezeichnung des Buttons.
   * @param controller AbstractControl, der beim Klick aufgerufen werden soll.
   */
  public void addCreateButton(String name, final AbstractControl controller)
  {
    final Button button = GUI.getStyleFactory().createButton(buttonArea);
    button.setText(name);
    button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    button.addMouseListener(new MouseAdapter() {
      public void mouseUp(MouseEvent e) {
        controller.handleCreate();
      }
    });
  }

  /**
   * Fuegt der Area einen Speichern-Button hinzu.
   * Beim Click wird die Methode handleStore() des Controllers ausgefuehrt.
   * @param controller AbstractControl, der beim Klick aufgerufen werden soll.
   */
  public void addStoreButton(final AbstractControl controller)
  {
    storeButton = GUI.getStyleFactory().createButton(buttonArea);
    storeButton.setText(i18n.tr("Speichern"));
    storeButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    GUI.getShell().setDefaultButton(storeButton);
    storeButton.addListener(SWT.Traverse, new Listener()
    {
      public void handleEvent(Event event)
      {
        if (event.detail == SWT.TRAVERSE_RETURN)
          controller.handleStore();
      }
    });
		storeButton.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					controller.handleStore();
				}
			}
		});
		storeButton.addMouseListener(new MouseAdapter() {
      public void mouseUp(MouseEvent e) {
        controller.handleStore();
      }
    });
  }

  /**
   * Fuegt der Area einen Abbrechen-Button hinzu.
   * Beim Click wird die Methode handleCancel() des Controllers ausgefuehrt.
   * @param controller AbstractControl, der beim Klick aufgerufen werden soll.
   */
  public void addCancelButton(final AbstractControl controller)
  {
    final Button button = GUI.getStyleFactory().createButton(buttonArea);
    button.setText(i18n.tr("Zurück"));
    button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    button.addMouseListener(new MouseAdapter() {
      public void mouseUp(MouseEvent e) {
        controller.handleCancel();
      }
    });
  }

  /**
   * Fuegt der Area einen Loeschen-Button hinzu.
   * Beim Click wird die Methode handleDelete() des Controllers ausgefuehrt.
   * @param controller AbstractControl, der beim Klick aufgerufen werden soll.
   */
  public void addDeleteButton(final AbstractControl controller)
  {
    final Button button = GUI.getStyleFactory().createButton(buttonArea);
    button.setText(i18n.tr("Löschen"));
    button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    button.addMouseListener(new MouseAdapter() {
      public void mouseUp(MouseEvent e) {
        controller.handleDelete();
      }
    });
  }

  /**
   * Fuegt einen Custom Button hinzu.
   * @param text Beschriftung des Buttons.
   * @param adapter Adapter, der dem Button zugewiesen werden soll.
   */
  public void addCustomButton(String text, MouseAdapter adapter)
  {
    if (adapter == null)
    {
      // button without adapter makes no sense ;)
      Logger.warn("a button without a mouseAdapter makes no sense - skipping");
      return;
    }
    if (text == null || "".equals(text))
    {
      // button without text makes no sense ;)
      Logger.warn("a button without a text makes no sense - skipping");
      return;
    }

    final Button button = GUI.getStyleFactory().createButton(buttonArea);
    button.setText(text);
    button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    button.addMouseListener(adapter);
  }

}

/*********************************************************************
 * $Log: ButtonArea.java,v $
 * Revision 1.3  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.1  2004/04/12 19:15:59  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.7  2004/03/30 22:08:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.5  2004/02/20 01:25:06  willuhn
 * @N nice dialog
 * @N busy indicator
 * @N new status bar
 *
 * Revision 1.4  2004/02/18 20:28:45  willuhn
 * @N jameica now stores window position and size
 *
 * Revision 1.3  2004/02/18 11:38:49  willuhn
 * @N flat style
 *
 * Revision 1.2  2004/02/18 01:40:29  willuhn
 * @N new white style
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.12  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.10  2004/01/04 18:48:36  willuhn
 * @N config store support
 *
 * Revision 1.9  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.8  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.6  2003/12/10 01:12:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/12/10 00:47:12  willuhn
 * @N SearchDialog done
 * @N FatalErrorView
 *
 * Revision 1.4  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 * Revision 1.3  2003/11/24 17:27:50  willuhn
 * @N Context menu in table
 *
 * Revision 1.2  2003/11/22 20:43:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 **********************************************************************/