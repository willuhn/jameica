/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/LabelGroup.java,v $
 * $Revision: 1.4 $
 * $Date: 2003/11/24 14:21:53 $
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * @author willuhn
 */
public class LabelGroup
{

  private Group group = null;

  /**
   * Diese Klasse kapselt Dialog-Teile des Typs:
   * Gruppe mit Bezeichnung und da drin eine Liste von
   * Label- und Eingabefeldpaaren.
   * @param parent Das Composite, in dem die Group gemalt werden soll.
   * @param name Name der Group.
   */
  public LabelGroup(Composite parent, String name)
  {
    group = new Group(parent, SWT.NONE);
    group.setText(name);
    group.setLayout(new GridLayout(2, false));
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
  }
  
  /**
   * Fuegt ein weiteres Label-Paar hinzu.
   */
  public void addLabelPair(String name, Input input)
  {
    // Label
    final GridData labelGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    labelGrid.widthHint = 130;

    final Label label = new Label(group, SWT.NONE);
    label.setText(name);
    label.setLayoutData(labelGrid);

    // Inputfeld
    input.paint(group);
  }

}

/*********************************************************************
 * $Log: LabelGroup.java,v $
 * Revision 1.4  2003/11/24 14:21:53  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/11/23 19:26:27  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 * Revision 1.1  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 **********************************************************************/