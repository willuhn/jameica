/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/WorkdirChooser.java,v $
 * $Revision: 1.8 $
 * $Date: 2012/02/23 22:03:36 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.system;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.eclipse.swt.SWT;
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
      text.setText("Bitte wählen Sie den Ordner, in dem die Benutzerdaten gespeichert werden sollen.");
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
      
      final DirectoryDialog dialog = new DirectoryDialog(shell);
      dialog.setText("Bitte wählen Sie den Ordner aus.");
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
    }


    // Zeile 4
    {
      Label dummy = new Label(this.shell,SWT.NONE);
      dummy.setLayoutData(new GridData(GridData.BEGINNING));

      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.horizontalSpan = 2;
      this.check = new Button(this.shell,SWT.CHECK);
      this.check.setText("Künftig immer diesen Ordner verwenden");
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
   * Uebernimmt die Daten.
   */
  private void apply()
  {
    if (this.dir == null || this.dir.isDisposed())
    {
      Logger.warn("dialog already disposed");
      return;
    }
    
    String dir = this.dir.getText();
    
    if (dir == null || dir.trim().length() == 0)
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
    
    if (!file.canWrite())
    {
      this.error.setText("Sie besitzen keinen Schreibzugriff in diesem Ordner.");
      return;
    }
    
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



/**********************************************************************
 * $Log: WorkdirChooser.java,v $
 * Revision 1.8  2012/02/23 22:03:36  willuhn
 * @N wenn der User im Workdir-Chooser die Option "kuenftig nicht mehr anzeigen" aktiviert hat, kann er die Einstellung jetzt unter Datei->Einstellungen wieder rueckgaengig machen. Es gab sonst keine komfortable Moeglichkeit, den Dialog wieder "hervorzuholen"
 *
 * Revision 1.7  2012/02/21 15:03:32  willuhn
 * @N Parameter "-a" abgeschafft. Jetzt wird per Default immer nach dem Workdir gefragt - das vereinfacht die ganze Sache etwas.
 *
 * Revision 1.6  2011-08-17 08:21:32  willuhn
 * @N BUGZILLA 937
 *
 * Revision 1.5  2011-04-07 08:04:05  willuhn
 * @B da fehlte ein Leerzeichen
 *
 * Revision 1.4  2011-03-08 14:53:25  willuhn
 * @B Achtung, NPE-Gefahr
 *
 * Revision 1.3  2011-03-08 14:49:04  willuhn
 * @N Liste der letzten 5 Verzeichnisse merken und als Auswahlbox anzeigen
 *
 * Revision 1.2  2011-03-08 13:43:46  willuhn
 * @B Debugging/Cleanup
 *
 * Revision 1.1  2011-03-07 12:52:11  willuhn
 * @N Neuer Start-Parameter "-a", mit dem die Abfrage des Work-Verzeichnisses via Dialog aktiviert wird
 *
 * Revision 1.1  2011-03-04 18:13:38  willuhn
 * @N Erster Code fuer einen Workdir-Chooser
 *
 **********************************************************************/