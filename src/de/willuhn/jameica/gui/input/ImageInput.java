/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/ImageInput.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/04/26 12:01:42 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.input;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Auswahlfeldes fuer ein Bild.
 */
public class ImageInput extends AbstractInput
{
  private final static Settings settings = new Settings(ImageInput.class);
  
  private byte[] data        = null;
  private boolean focus      = false;
  private boolean enabled    = true;
  private boolean hasComment = false;
  
  private Button button      = null;
  private MenuItem menu      = null;
  
  private int height         = 80;
  private int width          = 80;
  private int border         = 10;
  private boolean scale      = true;

  /**
   * ct.
   * @param image das Bild.
   */
  public ImageInput(byte[] image)
  {
    this(image,-1,-1);
  }

  /**
   * ct.
   * @param image das Bild.
   * @param width Breite des Buttons in Pixeln. Default: 80.
   * @param height Hoehe des Buttons in Pixeln. Default: 80.
   */
  public ImageInput(byte[] image,int width, int height)
  {
    this.data = image;
    this.setName(i18n.tr("Bild"));
    if (width > 0)  this.width = width;
    if (height > 0) this.height = height;
  }
  
  /**
   * Legt fest, wieviel Pixel Abstand vom Rand des Buttons gelassen werden soll.
   * @param border Anzahl der Pixel zum Button-Rand. Default: 10.
   */
  public void setBorder(int border)
  {
    if (border >= 0)
      this.border = border;
  }
  
  /**
   * Legt fest, ob das Bild auf die Button-Groesse skaliert werden soll.
   * Steht der Wert auf False, passt das Bild u.U. nicht auf den Button und wird nur teilweise angezeigt.
   * @param scale true, wenn das Bild skaliert werden soll. Default: true
   */
  public void setScale(boolean scale)
  {
    this.scale = scale;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#disable()
   */
  public void disable()
  {
    setEnabled(false);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#enable()
   */
  public void enable()
  {
    setEnabled(true);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#focus()
   */
  public void focus()
  {
    this.focus = true;
    if (this.button != null && !this.button.isDisposed())
      this.button.setFocus();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public Control getControl()
  {
    if (this.button != null)
      return this.button;

    this.button = GUI.getStyleFactory().createButton(getParent());
    this.button.setEnabled(this.enabled);
    if (this.focus)
      this.button.setFocus();

    final GridData gd = new GridData(GridData.BEGINNING);
    gd.widthHint = this.width;
    gd.heightHint = this.height;
    gd.horizontalSpan = this.hasComment ? 1 : 2;
    this.button.setLayoutData(gd);

    // Button fuer das Aendern des Bildes
    this.button.addSelectionListener(new SelectionAdapter() {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      public void widgetSelected(SelectionEvent e)
      {
        FileDialog dialog = new FileDialog(GUI.getShell(), SWT.OPEN);
        dialog.setFilterExtensions(new String[]{"*.jpg;*.jpeg;*.png;*.gif;*.bmp"});
        dialog.setText(i18n.tr("Bitte wählen Sie die Bild-Datei aus"));
        String lastDir = settings.getString("lastdir",System.getProperty("user.home"));
        if (lastDir != null)
          dialog.setFilterPath(lastDir);
        
        String s = dialog.open();
        if (s == null)
          return; // Vorgang abgebrochen
          
        File file = new File(s);
        if (!file.exists() || !file.canRead())
        {
          Logger.warn("file " + file + " not found or not readable");
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Datei {0} nicht lesbar",file.getName()),StatusBarMessage.TYPE_ERROR));
          return;
        }
        
        settings.setAttribute("lastdir",file.getParent());
        
        // Bild aus der Datei lesen
        int count = 0;
        byte[] buf = new byte[4096];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream is = null;
        try
        {
          is = new BufferedInputStream(new FileInputStream(file));
          while ((count = is.read(buf)) != -1)
          {
            if (count > 0)
              bos.write(buf,0,count);
          }
          data = bos.toByteArray();

          // Bild neu laden
          refreshImage();
        }
        catch (Exception ex)
        {
          Logger.error("error while reading image",ex);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Datei {0} nicht lesbar: {1}",new String[]{file.getName(),ex.getMessage()}),StatusBarMessage.TYPE_ERROR));
        }
        finally
        {
          if (is != null)
          {
            try {
              is.close();
            } catch (Exception ex) {/* ignore */}
          }
        }
      }
    });

    // Pop-Up-Menu zum Loeschen des Bildes
    Menu m = new Menu(getParent().getShell(), SWT.POP_UP);
    this.menu = new MenuItem(m,SWT.CASCADE);
    this.menu.setText(i18n.tr("Bild entfernen"));
    this.menu.setEnabled(this.data != null);
    this.menu.setImage(SWTUtil.getImage("user-trash-full.png"));
    this.menu.addSelectionListener(new SelectionAdapter() {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      public void widgetSelected(SelectionEvent e)
      {
        data = null;
        refreshImage();
      }
    });
    this.button.setMenu(m);
    
    // Bild auf den Button laden
    refreshImage();

    return this.button;
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#setComment(java.lang.String)
   */
  public void setComment(String comment)
  {
    super.setComment(comment);
    this.hasComment = comment != null;
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#update()
   */
  void update()
  {
    // Wir wollen nicht, dass die Hintergrund-Farbe geaendert wird
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    return this.data;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#isEnabled()
   */
  public boolean isEnabled()
  {
    return this.enabled;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
    if (this.button != null && !this.button.isDisposed())
    {
      this.button.setEnabled(enabled);
      this.update();
    }
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value != null && !(value instanceof byte[]))
    {
      Logger.warn("invalid data given (" + value + "), need byte[]");
      return;
    }
    this.data = (byte[]) value;
    refreshImage();
  }
  
  /**
   * Aktualisiert das angezeigte Bild auf dem Button.
   */
  private void refreshImage()
  {
    if (this.button == null || this.button.isDisposed())
      return;

    if (this.menu != null && !this.menu.isDisposed())
      this.menu.setEnabled(this.data != null);

    if (this.data == null)
    {
      this.button.setImage(null);
      this.button.setText(i18n.tr("Kein Bild"));
      return;
    }
    
    Image image = null;
    GC gc = null;
    try
    {
      image = new Image(GUI.getDisplay(),new ByteArrayInputStream(this.data));
      if (this.scale)
      {
        ////////////////////////////////////////////////////////////////////////
        // Bild proportional passend skalieren
        Rectangle r = image.getBounds();
        double w = (double) r.width / (double)(this.width - this.border);
        double h = (double) r.height / (double)(this.height - this.border);
        if (w > h)
        {
          h = r.height / w;
          w = this.width - this.border;
        }
        else
        {
          w = r.width / h;
          h = this.height - this.border;
        }
        ////////////////////////////////////////////////////////////////////////

        Image scaled = new Image(GUI.getDisplay(), this.width - this.border,this.height - this.border);
        gc = new GC(scaled);
        gc.setAntialias(SWT.ON);
        gc.setInterpolation(SWT.HIGH);
        Rectangle source = image.getBounds();
        gc.drawImage(image, 0, 0, source.width, source.height, 0, 0, (int) w, (int) h);
        this.button.setImage(scaled);
      }
      else
      {
        this.button.setImage(image);
      }
      this.button.setText(""); // Text entfernen
    }
    catch (Throwable t)
    {
      Logger.write(Level.INFO,"no valid image choosen",t);
      this.data = null;
      refreshImage(); // fuehrt zur Anzeige "Kein Bild"
    }
    finally
    {
      if (image != null && this.scale) // nur wegwerfen, wenn wir skaliert haben
      {
        try {
          image.dispose();
        } catch (Exception e) {/* ignore*/}
      }
      if (gc != null)
      {
        try {
          gc.dispose();
        } catch (Exception e) {/* ignore*/}
      }
    }
  }

}



/**********************************************************************
 * $Log: ImageInput.java,v $
 * Revision 1.4  2011/04/26 12:01:42  willuhn
 * @D javadoc Fixes
 *
 * Revision 1.3  2010-09-06 15:31:53  willuhn
 * @N Heiners Patch zum proportionalen Skalieren des Bildes
 *
 * Revision 1.2  2010-08-24 23:06:10  willuhn
 * @N Context-Menu deaktivieren, wenn kein Bild vorhanden ist.
 *
 * Revision 1.1  2010-08-24 22:43:57  willuhn
 * @N ImageInput - wollte Heiner in JVerein fuer Mitgliedsfotos haben
 *
 **********************************************************************/