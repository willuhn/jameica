/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/ButtonArea.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/11/21 02:10:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.views.parts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.GUI;
import de.willuhn.jameica.I18N;

/**
 * Diese Klasse erzeugt standardisierte Bereiche fuer die Dialog-Buttons.
 * @author willuhn
 */
public class ButtonArea
{

  private Composite buttonArea;

  /**
   * Erzeugt einen neuen Standard-Button-Bereich.
   * @param parent Composite, in dem die Buttons gezeichnet werden sollen.
   * @param numButtons Anzahl der Buttons, die hier drin gespeichert werden sollen.
   */
  public ButtonArea(Composite parent, int numButtons)
  {

    GridLayout layout = new GridLayout();
    layout.marginHeight=0;
    layout.marginWidth=0;
    layout.numColumns = numButtons;

    buttonArea = new Composite(parent, SWT.NONE);
    buttonArea.setLayout(layout);
    buttonArea.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
  }

  /**
   * Fuegt der Area einen generischen Button hinzu.
   * Beim Click wird die Methode handle(Button) des Controllers ausgefuehrt.
   * @param name
   * @param controller
   */
  public void addButton(String name, final Controller controller)
  {
    final Button button = new Button(buttonArea,SWT.NONE);
    button.setText(name);
    button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    button.addMouseListener(new MouseAdapter() {
      public void mouseUp(MouseEvent e) {
        controller.handle(button);
      }
    });
  }

  /**
   * Fuegt der Area einen Speichern-Button hinzu.
   * Beim Click wird die Methode handleStore(Button) des Controllers ausgefuehrt.
   * @param name
   * @param controller
   */
  public void addStoreButton(final Controller controller)
  {
    final Button button = new Button(buttonArea,SWT.PUSH);
    button.setText(I18N.tr("Speichern"));
    button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    GUI.shell.setDefaultButton(button);
    // TODO: Button Behaviour
    button.addListener(SWT.Traverse, new Listener()
    {
      public void handleEvent(Event event)
      {
        if (event.detail == SWT.TRAVERSE_RETURN)
          controller.handleStore(button);
      }
    });
    button.addMouseListener(new MouseAdapter() {
      public void mouseUp(MouseEvent e) {
        controller.handleStore(button);
      }
    });
  }

  /**
   * Fuegt der Area einen Abbrechen-Button hinzu.
   * Beim Click wird die Methode handleCancel(Button) des Controllers ausgefuehrt.
   * @param name
   * @param controller
   */
  public void addCancelButton(final Controller controller)
  {
    final Button button = new Button(buttonArea,SWT.NONE);
    button.setText(I18N.tr("Abbrechen"));
    button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    button.addMouseListener(new MouseAdapter() {
      public void mouseUp(MouseEvent e) {
        controller.handleCancel(button);
      }
    });
  }

  /**
   * Fuegt der Area einen Loeschen-Button hinzu.
   * Beim Click wird die Methode handleDelete(Button) des Controllers ausgefuehrt.
   * @param name
   * @param controller
   */
  public void addDeleteButton(final Controller controller)
  {
    final Button button = new Button(buttonArea,SWT.NONE);
    button.setText(I18N.tr("Löschen"));
    button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    button.addMouseListener(new MouseAdapter() {
      public void mouseUp(MouseEvent e) {
        controller.handleDelete(button);
      }
    });
  }

}

/*********************************************************************
 * $Log: ButtonArea.java,v $
 * Revision 1.1  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 **********************************************************************/