/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.system;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.logging.Logger;

/**
 * Ermittelt das zu verwendende Arbeitsverzeichnis durch Nachfrage beim User.
 */
public class WorkdirChooser
{
  private final static int WINDOW_WIDTH = 450;
 
  private Button apply     = null;
  private Display display  = null;
  private Shell shell      = null;
  private Combo dir        = null;
  private Label error      = null;
  private Button check     = null;
  
  /**
   * Liefert das zu verwendende Arbeitsverzeichnis.
   * @return das zu verwendende Arbeitsverzeichnis.
   */
  public String getWorkDir()
  {
    // Wenn der User die Auswahl gespeichert hat, fragen wir nicht mehr. Aber
    // nur, wenn wir auch wirklich was haben
    if (!BootstrapSettings.getAskWorkdir())
    {
      // OK, der User hat die Auswahl mal gespeichert. Mal schauen,
      // ob er auch was eingegeben hatte
      String dir = BootstrapSettings.getProperty("dir",null);
      if (dir != null && dir.trim().length() > 0)
      {
        Logger.info("using last used workdir " + dir);
        return dir; // Ja, wir haben auch wirklich etwas
      }
    }

    Logger.info("asking user for workdir");
    
    // OK, wir fragen den User
    this.display = Display.getDefault();
    this.shell = new Shell(display,SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

    this.shell.setText("Benutzer-Ordner"); // I18n gibts hier noch nicht
    this.shell.setSize(WINDOW_WIDTH,SWT.DEFAULT);
    this.shell.setLayout(new GridLayout(3,false));
    this.shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e)
      {
        e.doit = false; // Wir wollen nur das Schliessen ueber das Kreuz rechts oben verhindern
      }
    });
    
    // Zeile 1
    {
      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.horizontalSpan = 3;
      Label text = new Label(this.shell,SWT.WRAP);
      text.setText("Bitte w‰hlen Sie den Ordner, in dem die Benutzerdaten gespeichert werden sollen.");
      text.setLayoutData(gd);
    }

    // Zeile 2
    {
      String s = BootstrapSettings.getProperty("dir",null);     // Prio 1: Das beim letzten Mal ausgewaehlte
      if (s == null || s.trim().length() == 0)
        s = Application.getStartupParams().getWorkDir();        // Prio 2: Das explizit angegebene
      if (s == null || s.trim().length() == 0)
        s = Application.getPlatform().getDefaultWorkdir();      // Prio 3: Das Default-Verzeichnis
      if (s == null || s.trim().length() == 0)
        s = "";                                                 // Prio 4: gar kein Vorschlag
      
      final String suggest = s;

      Label label = new Label(this.shell,SWT.NONE);
      label.setText("Benutzer-Ordner");
      label.setLayoutData(new GridData(GridData.BEGINNING));

      this.dir = new Combo(this.shell,SWT.DROP_DOWN);
      List<String> history = BootstrapSettings.getHistory();
      this.dir.setItems(history.toArray(new String[history.size()]));
      this.dir.setText(suggest);
      this.dir.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      this.dir.addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent e)
        {
          getDir(); // Anzeigen aktualisieren
        }
      });
      
      final DirectoryDialog dialog = new DirectoryDialog(shell);
      dialog.setText("Benutzer-Ordner");
      dialog.setMessage("Bitte w‰hlen Sie den Ordner, in dem die Benutzerdaten gespeichert werden sollen.");
      dialog.setFilterPath(suggest);

      final Button button = new Button(this.shell,SWT.PUSH);
      button.setImage(getImage("folder.png"));
      button.setLayoutData(new GridData(GridData.END));
      button.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e)
        {
          String s = dialog.open();
          if (s != null && s.length() > 0)
          {
            dialog.setFilterPath(s); // Fuers naechste Mal merken
            dir.setText(s);
          }
        }
      });
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
      this.error.setText("\n");
    }


    // Zeile 4
    {
      Label dummy = new Label(this.shell,SWT.NONE);
      dummy.setLayoutData(new GridData(GridData.BEGINNING));

      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.horizontalSpan = 2;
      this.check = new Button(this.shell,SWT.CHECK);
      this.check.setText("K¸nftig immer diesen Ordner verwenden");
      this.check.setLayoutData(gd);
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

      apply = new Button(comp,SWT.PUSH);
      apply.setEnabled(false);
      apply.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
      apply.setImage(getImage("ok.png"));
      apply.setText("‹bernehmen");
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
    
    // Einmal initial ausloesen
    this.getDir();

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
    
    return BootstrapSettings.getProperty("dir",null);
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
   * Liefert das ausgewaehle Benutzerverzeichnis.
   * @return das Benutzer-Verzeichnis oder NULL, wenn ein ungueltiges Benutzerverzeichnis ausgewaehlt wurde.
   */
  private String getDir()
  {
    if (this.dir == null || this.dir.isDisposed())
    {
      Logger.warn("dialog already disposed");
      return null;
    }
    
    if (this.apply != null && !this.apply.isDisposed())
      this.apply.setEnabled(false);
    
    String dir = this.dir.getText();
    
    if (dir == null || dir.trim().length() == 0)
    {
      this.error.setText("Bitte w‰hlen Sie einen Ordner aus.");
      return null;
    }

    String path = null;

    // Checken, ob das Verzeichnis existiert und ob wir darin schreiben koennen.
    File file = new File(dir);
    
    // Checken, ob sich der Ordner innerhalb des Programmordners befindet
    try
    {
      if (Platform.inProgramDir(file))
      {
        this.error.setText("Bitte w‰hlen Sie einen Benutzer-Ordner, der sich\nauﬂerhalb des Programm-Verzeichnisses befindet.");
        return null;
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to check canonical path",e);
      this.error.setText("Benutzer-Ordner nicht ausw‰hlbar: " + e.getMessage());
      return null;
    }
    
    
    if (file.exists() && !file.canWrite())
    {
      this.error.setText("Sie besitzen keinen Schreibzugriff in diesem Ordner.");
      return null;
    }
    
    if (!file.exists() && !file.getParentFile().canWrite())
    {
      this.error.setText("Sie besitzen keinen Schreibzugriff in diesem Ordner.");
      return null;
    }
    
    this.error.setText("\n");
    
    if (this.apply != null && !this.apply.isDisposed())
      apply.setEnabled(true);
    
    return path;
  }
  
  /**
   * Uebernimmt die Daten.
   */
  private void apply()
  {
    String dir = this.getDir();
    if (dir == null)
      return;
    
    // Wir speichern die aktuelle Auswahl
    BootstrapSettings.setProperty("dir",dir);
    
    // und fuegen das Verzeichnis noch zur History hinzu
    BootstrapSettings.addHistory(dir);

    // Wir vermerken ausserdem, ob der User kuenftig noch gefragt werden moechte
    if (this.check != null && !this.check.isDisposed())
      BootstrapSettings.setAskWorkdir(!this.check.getSelection());
    
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
}
