/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/LabelGroup.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/11/20 03:48:42 $
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
   */
  public LabelGroup(Group group)
  {
    this.group = group;
  }
  
  /**
   * Fuegt ein weiteres Label-Paar hinzu.
   */
  public void addLabelPair(String name, Input input)
  {
    // Label
    final GridData labelGrid = new GridData(GridData.HORIZONTAL_ALIGN_END);
    labelGrid.widthHint = 100;

    final Label label = new Label(group, SWT.NONE);
    label.setText(name);
    label.setLayoutData(labelGrid);

    // Inputfeld
    input.paint(group);
  }

}

/*********************************************************************
 * $Log: LabelGroup.java,v $
 * Revision 1.1  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 **********************************************************************/