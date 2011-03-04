/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/Attic/WorkdirChooser.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/03/04 18:13:38 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui;

import java.io.File;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Hilfsklasse zum Erfragen des Benutzerverzeichnisses.
 */
public class WorkdirChooser
{
  private final static int WINDOW_WIDTH = 450;
 
  private Display display = null;
  private Shell shell     = null;
  private Text dir        = null;
  private Label error     = null;
  private Button check    = null;
  
  public String getWorkDir()
  {
    this.display = Display.getDefault();
    this.shell = new Shell(display,SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);

    this.shell.setText("Benutzer-Ordner"); // I18n gibts hier noch nicht
    this.shell.setSize(WINDOW_WIDTH,SWT.DEFAULT);
    this.shell.setLayout(new GridLayout(3,false));
    this.shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e)
      {
        System.exit(1);
      }
    });
    this.shell.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        if (e.keyCode == SWT.ESC)
        {
          close();
          System.exit(1);
        }
      }
    });
    
    // Zeile 1
    {
      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.horizontalSpan = 3;
      Label text = new Label(this.shell,SWT.WRAP);
      text.setText("Bitte wählen Sie den Ordner,in dem die Benutzerdaten gespeichert werden sollen.");
      text.setLayoutData(gd);
    }

    // Zeile 2
    {
      final String suggest = this.getDir();

      Label label = new Label(this.shell,SWT.NONE);
      label.setText("Benutzer-Ordner");
      label.setLayoutData(new GridData(GridData.BEGINNING));

      this.dir = new Text(this.shell,SWT.BORDER | SWT.SINGLE);
      this.dir.setText(suggest);
      this.dir.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      
      final Button button = new Button(this.shell,SWT.PUSH);
      button.setImage(getImage("folder.png"));
      button.setLayoutData(new GridData(GridData.END));
      button.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e)
        {
          DirectoryDialog dialog = new DirectoryDialog(shell);
          dialog.setText("Bitte wählen Sie den Ordner aus.");
          dialog.setFilterPath(suggest);
          String s = dialog.open();
          if (s != null && s.length() > 0)
            dir.setText(s);
        }
      });
    }

    // Zeile 3
    {
      Label dummy = new Label(this.shell,SWT.NONE);
      dummy.setLayoutData(new GridData(GridData.BEGINNING));

      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.horizontalSpan = 2;
      this.check = new Button(this.shell,SWT.CHECK);
      this.check.setText("Künftig immer diesen Ordner verwenden");
      this.check.setLayoutData(gd);
    }
    
    // Zeile 3
    {
      Label dummy = new Label(this.shell,SWT.NONE);
      dummy.setLayoutData(new GridData(GridData.BEGINNING));

      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.horizontalSpan = 2;
      this.error = new Label(this.shell,SWT.NONE);
      this.error.setForeground(new Color(this.display,250,10,10));
      this.error.setLayoutData(gd);
    }


    // Zeile 5
    {
      GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
      gd.horizontalSpan = 3;
      Composite comp = new Composite(this.shell,SWT.NONE);
      comp.setLayoutData(gd);
      
      GridLayout gl = new GridLayout(2,false);
      gl.marginWidth = 0;
      comp.setLayout(gl);

      Button apply = new Button(comp,SWT.PUSH);
      apply.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
      apply.setImage(getImage("ok.png"));
      apply.setText("Übernehmen");
      apply.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e)
        {
          apply();
        }
      });
      this.shell.setDefaultButton(apply);
      
      Button cancel = new Button(comp,SWT.PUSH);
      cancel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
      cancel.setImage(getImage("process-stop.png"));
      cancel.setText("Abbrechen");
      cancel.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e)
        {
          close();
          System.exit(1);
        }
      });
    }

    this.shell.pack();
    this.shell.setMinimumSize(this.shell.computeSize(WINDOW_WIDTH,SWT.DEFAULT));

    // Splashscreen mittig positionieren
    Rectangle splashRect = this.shell.getBounds();
    Rectangle displayRect = this.display.getPrimaryMonitor().getBounds();
    int x = displayRect.x + ((displayRect.width - splashRect.width) / 2);
    int y = displayRect.y + ((displayRect.height - splashRect.height) / 2);
    this.shell.setLocation(x, y);
    
    this.shell.open();
    this.shell.forceActive();
    while (this.shell != null && !this.shell.isDisposed()) {
      if (!display.readAndDispatch()) display.sleep();
    }
    
    return getDir();
  }
  
  /**
   * Liefert ein Bild.
   * @param name das Bild.
   * @return das Bild.
   */
  private Image getImage(String name)
  {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("img/" + name);
    ImageData data = new ImageData(is);
    ImageData data2 = null;
    if (data.transparentPixel > 0) {
      data2 = data.getTransparencyMask();
      return new Image(this.display, data, data2);
    }
    return new Image(this.display, data);
  }
  
  /**
   * Uebernimmt die Daten.
   */
  private void apply()
  {
    String dir = this.dir.getText();
    
    if (dir == null || dir.length() == 0)
    {
      this.error.setText("Bitte wählen Sie einen Ordner aus.");
      return;
    }
    
    // Checken, ob das Verzeichnis existiert und ob wir darin schreiben koennen.
    File file = new File(dir);
    if (!file.exists() && !file.mkdirs())
    {
      this.error.setText("Der Ordner konnte nicht erstellt werden.");
      return;
    }
    
    // Scheint alles i.O.
    close();
  }
  
  /**
   * Schliesst den Dialog.
   */
  private void close()
  {
    if (shell != null)
    {
      try
      {
        shell.close();
        SWTUtil.disposeChildren(shell);
        shell.dispose();
      }
      catch (Throwable t)
      {
        Logger.error("unable to dispose shell",t);
      }
    }
    
    if (display != null)
    {
      try
      {
        display.dispose();
      }
      catch (Throwable t)
      {
        Logger.error("unable to dispose display",t);
      }
    }
  }
  
  /**
   * Liefert das zuletzt verwendete Verzeichnis oder das via -f angegebene.
   * @return das 
   */
  private String getDir()
  {
    return Application.getStartupParams().getWorkDir();
  }
}



/**********************************************************************
 * $Log: WorkdirChooser.java,v $
 * Revision 1.1  2011/03/04 18:13:38  willuhn
 * @N Erster Code fuer einen Workdir-Chooser
 *
 **********************************************************************/