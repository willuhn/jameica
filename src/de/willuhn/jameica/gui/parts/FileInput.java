/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Attic/FileInput.java,v $
 * $Revision: 1.6 $
 * $Date: 2004/03/03 22:27:10 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Style;

/**
 * @author willuhn
 * Ist zustaendig fuer Text-Eingabefelder, hinter denen sich jedoch noch ein
 * zusaetzlicher Button fuer eine Dateisuche befindet.
 */
public class FileInput extends Input
{

  private Composite comp;
  private Text text;
  private Button button;
  private String value;

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param value der initial einzufuegende Wert fuer das Eingabefeld.
   */
  public FileInput(String value)
  {
  	super();
    this.value = value;
  }

  /**
   * @see de.willuhn.jameica.gui.parts.Input#getControl()
   */
  public Control getControl()
  {

    comp = new Composite(getParent(),SWT.NONE);
		comp.setBackground(Style.COLOR_BG);
    GridLayout layout = new GridLayout(2, false);
    layout.marginHeight=0;
    layout.marginWidth=0;
    comp.setLayout(layout);
  
    Composite around = new Composite(comp,SWT.NONE);
    around.setBackground(Style.COLOR_BORDER);
    around.setLayout(new FormLayout());
		around.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    FormData comboFD = new FormData();
    comboFD.left = new FormAttachment(0, 1);
    comboFD.top = new FormAttachment(0, 1);
    comboFD.right = new FormAttachment(100, -1);
    comboFD.bottom = new FormAttachment(100, -1);
    
    Composite around2 = new Composite(around,SWT.NONE);
    around2.setBackground(Style.COLOR_WHITE);
    around2.setLayout(new FormLayout());
    around2.setLayoutData(comboFD);

    FormData comboFD2 = new FormData();
    comboFD2.left = new FormAttachment(0, 2);
    comboFD2.top = new FormAttachment(0, 2);
    comboFD2.right = new FormAttachment(100, -2);
    comboFD2.bottom = new FormAttachment(100, -2);

    text = new Text(around2, SWT.NONE);
    text.setBackground(Style.COLOR_WHITE);
    text.setLayoutData(comboFD2);
//    GridData grid = new GridData(GridData.FILL_HORIZONTAL);
//    text.setLayoutData(grid);
    text.setText((value == null ? "" : value));
    text.addFocusListener(new FocusAdapter(){
      public void focusGained(FocusEvent e){
        text.setSelection(0, text.getText().length());
      }
    });

    button = new Button(comp,SWT.FLAT);
    //button.setImage(Style.getImage("search.gif"));
    button.setText(i18n.tr("öffnen..."));
    button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    button.setAlignment(SWT.RIGHT);
    button.addMouseListener(new MouseAdapter()
    {
      public void mouseUp(MouseEvent e)
      {
        Application.getLog().debug("starting file dialog");
        FileDialog dialog = new FileDialog(GUI.getShell(),SWT.OPEN);
        String s = dialog.open();
        if (s != null && !"".equals(s))
          text.setText(s); // wir schreiben den Wert nur rein, wenn etwas uebergeben wurde
        text.redraw();
        text.forceFocus(); // das muessen wir machen, damit die CommentLister ausgeloest werden
      }
    });
 
    return comp;
  }

  /**
   * @see de.willuhn.jameica.gui.parts.Input#getValue()
   */
  public String getValue()
  {
    return text.getText();
  }

  /**
   * @see de.willuhn.jameica.gui.parts.Input#setValue(java.lang.String)
   */
  public void setValue(String value)
  {
    if (value == null)
      return;
    this.text.setText(value);
    this.text.redraw();
  }

  /**
   * @see de.willuhn.jameica.gui.parts.Input#focus()
   */
  public void focus()
  {
    text.setFocus();
  }

  /**
   * @see de.willuhn.jameica.gui.parts.Input#disable()
   */
  public void disable()
  {
    text.setEnabled(false);
  }

  /**
   * @see de.willuhn.jameica.gui.parts.Input#enable()
   */
  public void enable()
  {
    text.setEnabled(true);
  }

}

/*********************************************************************
 * $Log: FileInput.java,v $
 * Revision 1.6  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.5  2004/02/18 20:28:45  willuhn
 * @N jameica now stores window position and size
 *
 * Revision 1.4  2004/02/18 17:14:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/02/18 01:40:30  willuhn
 * @N new white style
 *
 * Revision 1.2  2004/01/29 00:07:24  willuhn
 * @N Text widget
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.3  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.2  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.1  2003/12/21 20:59:00  willuhn
 * @N added internal SSH tunnel
 *
 **********************************************************************/