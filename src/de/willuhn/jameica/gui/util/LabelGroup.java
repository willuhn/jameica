/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/LabelGroup.java,v $
 * $Revision: 1.12 $
 * $Date: 2004/09/13 23:27:12 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.util;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Diese Klasse kapselt Dialog-Teile in einer Gruppe.
 * Damit ist es einfacher, standardisierte Dialoge zu malen.
 * Man erstellt pro Dialog einfach ein oder mehrere solcher
 * Gruppen und tut dort seine Eingabefelder rein.
 * @author willuhn
 */
public class LabelGroup
{

	private I18N i18n;
  private Group group = null;

  /**
   * ct.
   * @param parent Das Composite, in dem die Group gemalt werden soll.
   * @param name Name der Group.
   */
  public LabelGroup(Composite parent, String name)
  {
		i18n = Application.getI18n();
    group = new Group(parent, SWT.NONE);
		group.setBackground(Color.BACKGROUND.getSWTColor());
    group.setText(name);
		group.setFont(Font.H2.getSWTFont());
    GridLayout layout = new GridLayout(2, false);
    group.setLayout(layout);
    GridData grid = new GridData(GridData.FILL_HORIZONTAL);
    group.setLayoutData(grid);
  }
  
	/**
	 * Liefert das allumfassende Control der Gruppe.
   * @return das Control der Group.
   */
  public Control getControl()
	{
		return group;
	}

  /**
   * Fuegt ein weiteres Label-Paar hinzu.
   * @param name Name des Feldes.
   * @param input Das Eingabefeld.
   */
  public void addLabelPair(String name, Input input)
  {
    // Label
    final GridData labelGrid = new GridData(GridData.HORIZONTAL_ALIGN_END);
    labelGrid.verticalAlignment = GridData.CENTER;
    final Label label = new Label(group, SWT.NONE);
		label.setBackground(Color.BACKGROUND.getSWTColor());
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
    final GridData labelGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    labelGrid.horizontalSpan = 2;
    final Composite comp = new Composite(group,SWT.NONE);
		comp.setBackground(Color.BACKGROUND.getSWTColor());
    GridLayout gl = new GridLayout(2,false);
    gl.marginHeight = 0;
    gl.marginWidth = 0;
    comp.setLayout(gl);
    comp.setLayoutData(labelGrid);

    checkbox.paint(comp,40);

    final Label label = new Label(comp , SWT.NONE);
		label.setBackground(Color.BACKGROUND.getSWTColor());
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
		label.setBackground(Color.BACKGROUND.getSWTColor());
    label.setText(text);
    label.setLayoutData(labelGrid);
  }

  /**
   * Fuegt ein generisches GUI-Element hinzu.
   * @param part anzuzeigender Part.
   */
  public void addPart(Part part)
  {
    try {
      final GridData grid = new GridData(GridData.FILL_HORIZONTAL);
      grid.horizontalSpan = 2;
      final Composite comp = new Composite(group,SWT.NONE);
      comp.setBackground(Color.BACKGROUND.getSWTColor());
      comp.setLayoutData(grid);

      GridLayout layout = new GridLayout(1,true);
      layout.marginHeight = 0;
      layout.marginWidth = 0;
      comp.setLayout(layout);

      part.paint(comp);
    }
    catch (RemoteException e)
    {
    	Logger.error("error while reading table",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der Tabelle"));
    }
  }

  /**
   * Fuegt eine Zwischenueberschrift zur Group hinzu.
   * @param text die anzuzeigende Ueberschrift.
   */
  public void addHeadline(String text)
  {
		final GridData grid = new GridData(GridData.FILL_HORIZONTAL);
		grid.horizontalSpan = 2;
		grid.horizontalIndent = 0;
		Composite comp = new Composite(group,SWT.NONE);
		comp.setBackground(Color.BACKGROUND.getSWTColor());
		comp.setLayoutData(grid);

		GridLayout layout = new GridLayout(2,false);
		layout.marginHeight = 3;
		layout.marginWidth = 2;
		comp.setLayout(layout);

		final Label label = new Label(comp,SWT.NONE);
		label.setFont(Font.H2.getSWTFont());
		label.setBackground(Color.BACKGROUND.getSWTColor());
		label.setText(text);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		final Label line = new Label(comp,SWT.SEPARATOR | SWT.HORIZONTAL);
		line.setBackground(Color.BACKGROUND.getSWTColor());
		line.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

  }
  
  /**
   * Fuegt eine Trennzeile ein.
   */
  public void addSeparator()
  {
    final GridData lineGrid = new GridData(GridData.FILL_HORIZONTAL);
    lineGrid.horizontalSpan = 2;
    final Label line = new Label(group,SWT.SEPARATOR | SWT.HORIZONTAL);
		line.setBackground(Color.BACKGROUND.getSWTColor());
    line.setLayoutData(lineGrid);
  }
  
  /**
   * Erstellt eine neue ButtonAres in der Gruppe.
   * @param numButtons Anzahl der Buttons.
   * @return die Button-Area.
   */
  public ButtonArea createButtonArea(int numButtons)
  {
  	addSeparator();
		final GridData g = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
		g.horizontalSpan = 2;
		final Composite comp = new Composite(group,SWT.NONE);
		comp.setBackground(Color.BACKGROUND.getSWTColor());
		comp.setLayoutData(g);

		final GridLayout gl = new GridLayout();
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		comp.setLayout(gl);
  	return new ButtonArea(comp,numButtons);
  }
}

/*********************************************************************
 * $Log: LabelGroup.java,v $
 * Revision 1.12  2004/09/13 23:27:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/08/18 23:14:19  willuhn
 * @D Javadoc
 *
 * Revision 1.10  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.9  2004/07/09 00:12:47  willuhn
 * @C Redesign
 *
 * Revision 1.8  2004/06/30 20:58:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/06/14 22:05:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/05/27 21:35:02  willuhn
 * @N PGP signing in ant script
 * @N MD5 checksum in ant script
 *
 * Revision 1.5  2004/05/25 23:23:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.3  2004/04/27 00:04:44  willuhn
 * @D javadoc
 *
 * Revision 1.2  2004/04/14 22:16:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/04/12 19:15:59  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.13  2004/04/05 23:29:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/04/01 00:23:24  willuhn
 * @N FontInput
 * @N ColorInput
 * @C improved ClassLoader
 * @N Tabs in Settings
 *
 * Revision 1.11  2004/03/30 22:08:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/03/24 00:46:03  willuhn
 * @C refactoring
 *
 * Revision 1.9  2004/03/19 01:44:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/03/11 08:56:56  willuhn
 * @C some refactoring
 *
 * Revision 1.7  2004/03/04 00:26:34  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.5  2004/02/27 01:09:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/02/18 01:40:30  willuhn
 * @N new white style
 *
 * Revision 1.3  2004/02/17 00:53:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/01/29 00:07:24  willuhn
 * @N Text widget
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.15  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
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
 * @N FatalErrorView
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