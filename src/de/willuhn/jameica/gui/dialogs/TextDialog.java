/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/TextDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/03/17 22:44:10 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;

/**
 * Dialog zur Eingabe eines kurzen Textes.
 */
public class TextDialog extends SimpleDialog {

	private Composite comp 	= null;
	private Label label			= null;
	private Label tLabel 		= null;
	private Text text			 	= null;
	private Button button 	= null;
	private Button cancel 	= null;
	
	private String labelText = "";
	
	private String value		= null;
	
	/**
	 * Erzeugt einen neuen Text-Dialog.
	 * @param position Position des Dialogs.
	 * @see AbstractDialog#POSITION_MOUSE
	 * @see AbstractDialog#POSITION_CENTER
	 */
  public TextDialog(int position) {
    super(position);
    
    this.labelText = Application.getI18n().tr("Ihre Eingabe");
  }

	/**
	 * Speichert den Text, der links neben dem Eingabefeld fuer die
	 * Text-Eingabe angezeigt werden soll. Wird er nicht definiert,
	 * wird "Ihre Eingabe" angezeigt.
   * @param t anzuzeigender Text.
   */
  public void setLabelText(String t)
	{
		labelText = t;
		if (text != null && !text.isDisposed())
			text.setText( labelText == null ? "" : labelText);
	}

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
	{
		// Composite um alles drumrum.
		comp = new Composite(parent,SWT.NONE);
		comp.setBackground(Color.BACKGROUND.getSWTColor());
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout(3,false));
		
		// Text
		label = GUI.getStyleFactory().createLabel(comp,SWT.WRAP);
		label.setText(getText());
		GridData grid = new GridData(GridData.FILL_HORIZONTAL);
		grid.horizontalSpan = 3;
		label.setLayoutData(grid);
		
		// Label vor Eingabefeld
		tLabel = GUI.getStyleFactory().createLabel(comp,SWT.NONE);
		tLabel.setText(labelText);
		tLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		text = GUI.getStyleFactory().createText(comp);
		GridData grid3 = new GridData(GridData.FILL_HORIZONTAL);
		grid3.horizontalSpan = 2;
		text.setLayoutData(grid3);

		// Dummy-Label damit die Buttons buendig unter dem Eingabefeld stehen
		Label dummy = GUI.getStyleFactory().createLabel(comp,SWT.NONE);
		dummy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// OK-Button
		button = GUI.getStyleFactory().createButton(comp);
		button.setText("    " + i18n.tr("OK") + "    ");
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		getShell().setDefaultButton(button);
		button.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				value = text.getText();
				close();
			}
		});

		// Abbrechen-Button
		cancel = GUI.getStyleFactory().createButton(comp);
		cancel.setText(i18n.tr("Abbrechen"));
		cancel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cancel.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				throw new OperationCanceledException("Dialog abgebrochen");
      }
    });

		// so und jetzt noch der Shell-Listener, damit beim
		// Klick auf das Schliessen-Kreuz rechts oben eine
		// OperationCancelledException ausgeloest wird.
		addShellListener(new ShellListener() {
			public void shellClosed(ShellEvent e) {
				throw new OperationCanceledException("dialog cancelled via close button");
			}
      public void shellActivated(ShellEvent e) {}
      public void shellDeactivated(ShellEvent e) {}
      public void shellDeiconified(ShellEvent e) {}
      public void shellIconified(ShellEvent e) {}
    });
	}		

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception {
    return value;
  }

}


/**********************************************************************
 * $Log: TextDialog.java,v $
 * Revision 1.1  2005/03/17 22:44:10  web0
 * @N added fallback if system is not able to determine hostname
 *
 **********************************************************************/