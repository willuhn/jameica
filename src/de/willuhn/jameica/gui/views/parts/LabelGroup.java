/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/LabelGroup.java,v $
 * $Revision: 1.14 $
 * $Date: 2004/01/08 20:50:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.views.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.views.util.Style;
import de.willuhn.util.I18N;

/**
   * Diese Klasse kapselt Dialog-Teile in einer Gruppe.
   * Damit ist es einfacher, standardisierte Dialoge zu malen.
   * Man erstellt pro Dialog einfach ein oder mehrere solcher
   * Gruppen und tut dort seine Eingabefelder rein.
 * @author willuhn
 */
public class LabelGroup
{

  private Group group = null;

  /**
   * ct.
   * @param parent Das Composite, in dem die Group gemalt werden soll.
   * @param name Name der Group.
   */
  public LabelGroup(Composite parent, String name)
  {
    group = new Group(parent, SWT.NONE);
    group.setText(name);
    GridLayout layout = new GridLayout(2, false);
    group.setLayout(layout);
    GridData grid = new GridData(GridData.FILL_HORIZONTAL);
    group.setLayoutData(grid);
  }
  
  /**
   * Fuegt ein weiteres Label-Paar hinzu.
   * @param name Name des Feldes.
   * @param input Das Eingabefeld.
   */
  public void addLabelPair(String name, Input input)
  {
    // Label
    final GridData labelGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    labelGrid.widthHint = 130;
    labelGrid.verticalAlignment = GridData.BEGINNING;
    final Label label = new Label(group, SWT.NONE);
    label.setText(name);
    label.setLayoutData(labelGrid);

    // Inputfeld
    input.paint(group);
  }
  
  /**
   * Fuegt eine Checkbox mit Kommentar hinzu.
   * @param checkbox die Checkbox.
   * @param text Text dahinter.
   */
  public void addCheckbox(CheckboxInput checkbox, String text)
  {
    final GridData labelGrid = new GridData(GridData.FILL_HORIZONTAL);
    labelGrid.horizontalSpan = 2;
    final Composite comp = new Composite(group,SWT.NONE);
    GridLayout gl = new GridLayout(2,false);
    gl.marginHeight = 0;
    gl.marginWidth = 0;
    comp.setLayout(gl);
    comp.setLayoutData(labelGrid);

    checkbox.paint(comp,40);

    final Label label = new Label(comp , SWT.NONE);
    label.setText(text);
    label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
  }

  /**
   * Fuegt Freitext zur Group hinzu.
   * @param text der anzuzeigende Text.
   * @param linewrap legt fest, ob der Text bei Erreichen der maximalen Breite umgebrochen werden darf.
   */
  public void addText(String text, boolean linewrap)
  {
    final GridData labelGrid = new GridData(GridData.FILL_HORIZONTAL);
    labelGrid.horizontalSpan = 2;
    final Label label = new Label(group,linewrap ? SWT.WRAP : SWT.NONE);
    label.setText(text);
    label.setLayoutData(labelGrid);
  }

  /**
   * Fuegt eine Tabelle zur Group hinzu.
   * @param table anzuzeigende Tabelle.
   */
  public void addTable(Table table)
  {
    try {
      final GridData grid = new GridData(GridData.FILL_HORIZONTAL);
      grid.horizontalSpan = 2;
      final Composite comp = new Composite(group,SWT.NONE);
      comp.setLayoutData(grid);

      GridLayout layout = new GridLayout(1,true);
      layout.marginHeight = 0;
      layout.marginWidth = 0;
      comp.setLayout(layout);

      table.paint(comp);
    }
    catch (RemoteException e)
    {
      GUI.setActionText(I18N.tr("Fehler beim Lesen der Tabelle"));
    }
  }

  /**
   * Fuegt eine Zwischenueberschrift zur Group hinzu.
   * @param text die anzuzeigende Ueberschrift.
   */
  public void addHeadline(String text)
  {
    final GridData labelGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    labelGrid.horizontalSpan = 2;
    labelGrid.widthHint = 400;
    final Label label = new Label(group,SWT.NONE);
    label.setFont(Style.FONT_HEADLINE);
    label.setText(text);
    label.setLayoutData(labelGrid);
    addSeparator();
  }
  
  /**
   * Fuegt eine Trennzeile ein.
   */
  public void addSeparator()
  {
    final GridData lineGrid = new GridData(GridData.FILL_HORIZONTAL);
    lineGrid.horizontalSpan = 2;
    final Label line = new Label(group,SWT.SEPARATOR | SWT.HORIZONTAL);
    line.setLayoutData(lineGrid);
  }
  
  /**
   * Erstellt eine neue ButtonAres in der Gruppe.
   * @param numButtons Anzahl der Buttons.
   * @return die Button-Area.
   */
  public ButtonArea createButtonArea(int numButtons)
  {
		final GridData g = new GridData(GridData.HORIZONTAL_ALIGN_END);
		g.horizontalSpan = 2;
		final Composite comp = new Composite(group,SWT.NONE);
		final GridLayout gl = new GridLayout();
		gl.marginHeight = 1;
		gl.marginWidth = 1;
		comp.setLayout(gl);
		comp.setLayoutData(g);
  	return new ButtonArea(comp,numButtons);
  }
}

/*********************************************************************
 * $Log: LabelGroup.java,v $
 * Revision 1.14  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.13  2004/01/06 01:27:30  willuhn
 * @N table order
 *
 * Revision 1.12  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.11  2003/12/28 22:58:27  willuhn
 * @N synchronize mode
 *
 * Revision 1.10  2003/12/26 21:43:30  willuhn
 * @N customers changable
 *
 * Revision 1.9  2003/12/19 13:36:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2003/12/19 01:43:27  willuhn
 * @N added Tree
 *
 * Revision 1.7  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.6  2003/12/10 00:47:12  willuhn
 * @N SearchDialog done
 * @N ErrorView
 *
 * Revision 1.5  2003/12/01 20:28:57  willuhn
 * @B filter in DBIteratorImpl
 * @N InputFelder generalisiert
 *
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