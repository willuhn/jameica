/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/Container.java,v $
 * $Revision: 1.13 $
 * $Date: 2007/09/06 22:21:55 $
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
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Abstrakte Klasse, die die Basis-Funktionalitaet zur Erstellung eines komplexen Dialogs
 * mitbringt. Man kann als Paare von Labels mit Eingabe-Feldern, Checkboxen, Tabellen und
 * dergleichen reintun. Eine der konkreten Implementierungen ist die LabelGroup.
 * @author willuhn
 */
public abstract class Container
{

  private boolean fullSize = false;
  /**
   * ct.
   * @param fullSize Legt fest, ob der Container die volle moegliche Hoehe einnehmen
   * soll oder nur die benoetigte.
   */
  public Container(boolean fullSize)
  {
    this.fullSize = fullSize;
  }

  /**
   * Liefert das Composite, in das die Label-Paare, Checkboxen und so weiter
   * gezeichnet werden.
   * @return das Composite, in das die Daten gezeichnet werden sollen.
   */
  public abstract Composite getComposite();

  /**
   * Prueft, ob der Container die volle moegliche Hoehe einnehmen soll oder nur
   * die tatsaechlich benoetigte.
   * @return true, wenn der Container die volle Groesse einnehmen soll.
   */
  protected final boolean isFullSize()
  {
    return this.fullSize;
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
    if (input instanceof TextAreaInput)
      labelGrid.verticalAlignment = GridData.BEGINNING;
    else
      labelGrid.verticalAlignment = GridData.CENTER;
    final Label label = GUI.getStyleFactory().createLabel(getComposite(),SWT.NONE);
    label.setText(name);
    if (input.isMandatory() && Application.getConfig().getMandatoryLabel())
      label.setForeground(Color.ERROR.getSWTColor());
    label.setLayoutData(labelGrid);

    // Inputfeld
    input.paint(getComposite());
  }
  
  /**
   * Fuegt ein Eingabe-Feld hinzu, welches ein eigenes Label mitbringt.
   * @param input Das Eingabefeld.
   */
  public void addInput(Input input)
  {
    addLabelPair(input.getName(),input);
  }
  
  /**
   * Fuegt ein Label-Paar hinzu, bei dem beide Seiten ein Eingabe-Feld sind.
   * @param left linkes Eingabe-Feld.
   * @param right rechtes Eingabe-Feld.
   */
  public void addLabelPair(Input left, Input right)
  {
    left.paint(getComposite(),50);
    right.paint(getComposite());
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
    final Composite comp = new Composite(getComposite(),SWT.NONE);
		comp.setBackground(Color.BACKGROUND.getSWTColor());
    GridLayout gl = new GridLayout(2,false);
    gl.marginHeight = 0;
    gl.marginWidth = 0;
    comp.setLayout(gl);
    comp.setLayoutData(labelGrid);

    checkbox.paint(comp,40);

    final Label label = GUI.getStyleFactory().createLabel(comp,SWT.NONE);
    label.setText(text);
    if (checkbox.isMandatory() && Application.getConfig().getMandatoryLabel())
      label.setForeground(Color.ERROR.getSWTColor());
    label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
  }

  /**
   * Fuegt Freitext zur Group hinzu.
   * @param text der anzuzeigende Text.
   * @param linewrap legt fest, ob der Text bei Erreichen der maximalen Breite umgebrochen werden darf.
   */
  public void addText(String text, boolean linewrap)
  {
    addText(text,linewrap,null);
  }

  /**
   * Fuegt Freitext zur Group hinzu.
   * @param text der anzuzeigende Text.
   * @param linewrap legt fest, ob der Text bei Erreichen der maximalen Breite umgebrochen werden darf.
   * @param color Farbe des Textes.
   */
  public void addText(String text, boolean linewrap, Color color)
  {
    final GridData labelGrid = new GridData(GridData.FILL_HORIZONTAL);
    labelGrid.horizontalSpan = 2;

    final Label label = GUI.getStyleFactory().createLabel(getComposite(),linewrap ? SWT.WRAP : SWT.NONE);
    if (color != null)
      label.setForeground(color.getSWTColor());
    label.setText(text);
    label.setLayoutData(labelGrid);
    if (linewrap)
    {
    	// Workaround fuer Windows, weil dort mehrzeilige
    	// Labels nicht korrekt umgebrochen werden.
      label.addControlListener(new ControlAdapter() {
        public void controlResized(ControlEvent e)
        {
          label.setSize(label.computeSize(label.getSize().x,SWT.DEFAULT));
        }
      });
    }
  }

  /**
   * Fuegt ein generisches GUI-Element hinzu.
   * @param part anzuzeigender Part.
   */
  public void addPart(Part part)
  {
    try {
      final GridData grid = new GridData(isFullSize() ? GridData.FILL_BOTH : GridData.FILL_HORIZONTAL);
      grid.horizontalSpan = 2;
      final Composite comp = new Composite(getComposite(),SWT.NONE);
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
    	Logger.error("error while adding part",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Anzeigen des Dialogs."),StatusBarMessage.TYPE_ERROR));
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
		Composite comp = new Composite(getComposite(),SWT.NONE);
		comp.setBackground(Color.BACKGROUND.getSWTColor());
		comp.setLayoutData(grid);

		GridLayout layout = new GridLayout(2,false);
		layout.marginHeight = 3;
		layout.marginWidth = 2;
		comp.setLayout(layout);

		final Label label = GUI.getStyleFactory().createLabel(comp,SWT.NONE);
		label.setFont(Font.H2.getSWTFont());
		label.setText(text);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		final Label line = GUI.getStyleFactory().createLabel(comp,SWT.SEPARATOR | SWT.HORIZONTAL);
		line.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

  }
  
  /**
   * Fuegt eine Trennzeile ein.
   */
  public void addSeparator()
  {
    final GridData lineGrid = new GridData(GridData.FILL_HORIZONTAL);
    lineGrid.horizontalSpan = 2;
    final Label line = GUI.getStyleFactory().createLabel(getComposite(),SWT.SEPARATOR | SWT.HORIZONTAL);
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
		final Composite comp = new Composite(getComposite(),SWT.NONE);
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
 * $Log: Container.java,v $
 * Revision 1.13  2007/09/06 22:21:55  willuhn
 * @N Hervorhebung von Pflichtfeldern konfigurierbar
 * @N Neustart-Hinweis nur bei Aenderungen, die dies wirklich erfordern
 *
 * Revision 1.12  2007/09/06 18:17:08  willuhn
 * @N colourize label when mandatory
 *
 * Revision 1.11  2007/05/14 11:18:09  willuhn
 * @N Hoehe der Statusleiste abhaengig von DPI-Zahl und Schriftgroesse
 * @N Default-Schrift konfigurierbar und Beruecksichtigung dieser an mehr Stellen
 *
 * Revision 1.10  2007/04/02 23:01:41  willuhn
 * @N SelectInput auf BeanUtil umgestellt
 *
 * Revision 1.9  2007/03/19 12:30:06  willuhn
 * @N Input can now have it's own label
 *
 * Revision 1.8  2006/08/05 20:44:59  willuhn
 * @B Bug 256
 *
 * Revision 1.7  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 * Revision 1.6  2005/07/11 18:12:39  web0
 * *** empty log message ***
 *
 * Revision 1.5  2005/06/21 20:02:03  web0
 * @C cvs merge
 *
 * Revision 1.3  2005/06/13 23:10:51  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/06/13 11:23:23  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/06/10 22:13:09  web0
 * @N new TabGroup
 * @N extended Settings
 *
 * Revision 1.15  2005/06/10 10:12:26  web0
 * @N Zertifikats-Dialog ergonomischer gestaltet
 * @C TrustManager prueft nun zuerst im Java-eigenen Keystore
 *
 * Revision 1.14  2004/11/12 18:23:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/10/25 17:59:15  willuhn
 * @N aenderbare Tabellen
 *
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