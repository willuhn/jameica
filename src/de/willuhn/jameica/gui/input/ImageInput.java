/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.input;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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

import de.willuhn.io.IOUtil;
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
  private static final Settings settings = new Settings(ImageInput.class);
  
  private byte[] data        = null;
  private boolean focus      = false;
  private boolean enabled    = true;
  private boolean hasComment = false;
  
  private Button button      = null;
  private MenuItem menudel   = null;
  private MenuItem menucpy   = null;
  
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
    
    Object tooltip = this.getData(DATAKEY_TOOLTIP);
    if (tooltip != null)
      this.button.setToolTipText(tooltip.toString());

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
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        FileDialog dialog = new FileDialog(GUI.getShell(), SWT.OPEN);
        dialog.setFilterExtensions(new String[]{"*.jpg;*.jpeg;*.png;*.gif;*.bmp"});
        dialog.setText(i18n.tr("Bitte w�hlen Sie die Bild-Datei aus"));
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
        
        try(
          InputStream is = new BufferedInputStream(new FileInputStream(file));
          ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ) {
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
      }
    });

    // Pop-Up-Menu zum Loeschen des Bildes
    Menu m = new Menu(getParent().getShell(), SWT.POP_UP);
    this.menudel = new MenuItem(m,SWT.CASCADE);
    this.menudel.setText(i18n.tr("Bild entfernen"));
    this.menudel.setEnabled(this.data != null);
    this.menudel.setImage(SWTUtil.getImage("user-trash-full.png"));
    this.menudel.addSelectionListener(new SelectionAdapter() {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        data = null;
        refreshImage();
      }
    });
    
    this.menucpy = new MenuItem(m,SWT.CASCADE);
    this.menucpy.setText(i18n.tr("In Zwischenablage kopieren"));
    this.menucpy.setEnabled(this.data != null);
    this.menucpy.setImage(SWTUtil.getImage("edit-copy.png"));
    this.menucpy.addSelectionListener(new SelectionAdapter() {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        Clipboard clipboard =  Toolkit.getDefaultToolkit().getSystemClipboard();
        ClipImage ci = new ClipImage(data);
        clipboard.setContents(ci, null);
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
  @Override
  public void setComment(String comment)
  {
    super.setComment(comment);
    this.hasComment = comment != null;
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#update()
   */
  @Override
  protected void update()
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

    if (this.menudel != null && !this.menudel.isDisposed())
      this.menudel.setEnabled(this.data != null);
    if (this.menucpy != null && !this.menucpy.isDisposed())
      this.menucpy.setEnabled(this.data != null);

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
        gc.dispose();
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

  /**
   * Uebernimmt das Kopieren des Bildes in die Zwischenablage.
   */
  private class ClipImage implements Transferable, ClipboardOwner
  {
    private byte[] image;

    /**
     * ct.
     * @param im Byte-Array mit den Bild-Daten.
     */
    public ClipImage(byte[] im)
    {
      image = im;
    }

    /**
     * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
     */
    public DataFlavor[] getTransferDataFlavors()
    {
      return new DataFlavor[]
      {
          DataFlavor.imageFlavor
      };
    }

    /**
     * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
     */
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
      return DataFlavor.imageFlavor.equals(flavor);
    }

    /**
     * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
     */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
    {
      if (!isDataFlavorSupported(flavor))
        throw new UnsupportedFlavorException(flavor);
      
      return Toolkit.getDefaultToolkit().createImage(image);
    }

    /**
     * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
     */
    public void lostOwnership(Clipboard clip, Transferable tr)
    {
      return;
    }
  }
}



/**********************************************************************
 * $Log: ImageInput.java,v $
 * Revision 1.8  2012/06/17 09:36:53  willuhn
 * @N Heiner's Patch zum Kopieren des Bildes in die Zwischenablage
 *
 * Revision 1.7  2012/01/21 23:34:56  willuhn
 * @B BUGZILLA 1177
 *
 * Revision 1.6  2011-08-08 10:45:05  willuhn
 * @C AbstractInput#update() ist jetzt "protected" (war package-private)
 *
 * Revision 1.5  2011-05-03 11:56:48  willuhn
 * @B GC wurde nicht korrekt disposed - siehe http://www.eclipse.org/forums/index.php?t=msg&goto=528906
 *
 * Revision 1.4  2011-04-26 12:01:42  willuhn
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