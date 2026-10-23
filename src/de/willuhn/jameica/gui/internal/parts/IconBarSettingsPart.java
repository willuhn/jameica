/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui.internal.parts;

import java.io.File;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.IconBarEntry;
import de.willuhn.jameica.gui.IconBarSettings;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.SearchableListDialog;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Einstellungen fuer die Symbolleiste.
 */
public class IconBarSettingsPart implements Part
{
  private CheckboxInput visible;
  private SelectInput size;
  private TablePart table;
  private Button add;
  private Button space;
  private Button remove;
  private Button up;
  private Button down;
  private Button icon;
  private Button resetIcon;
  private List<Image> tableImages = new ArrayList<Image>();
  private boolean entriesChanged = false;

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    final Container container = new SimpleContainer(parent,true);
    container.addInput(this.getVisible());
    container.addInput(this.getSize());
    container.addPart(getTable());
    
    parent.addDisposeListener(event -> disposeTableImages());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(getAddButton());
    buttons.addButton(getRemoveButton());
    buttons.addButton(getSpaceButton());
    buttons.addButton(getUpButton());
    buttons.addButton(getDownButton());
    buttons.addButton(getIconButton());
    buttons.addButton(getResetIconButton());
    
    container.addButtonArea(buttons);
  }

  /**
   * Speichert die Einstellungen.
   */
  public void apply()
  {
    try
    {
      IconBarSettings.setVisible(((Boolean)getVisible().getValue()).booleanValue());
      IconBarSettings.setSize((String)getSize().getValue());
      if (this.entriesChanged)
        IconBarSettings.setEntries(getTable().getItems(false));
      if (GUI.getIconBar() != null)
        GUI.getIconBar().redraw();
    }
    catch (Exception e)
    {
      Logger.error("unable to save icon bar settings",e);
    }
  }

  /**
   * Liefert die Checkbox fuer die Sichtbarkeit.
   * @return Checkbox.
   */
  private CheckboxInput getVisible()
  {
    if (this.visible != null)
      return this.visible;
    this.visible = new CheckboxInput(IconBarSettings.isVisible());
    this.visible.setName(Application.getI18n().tr("Symbolleiste anzeigen"));
    return this.visible;
  }

  /**
   * Liefert die Auswahl fuer die Groesse.
   * @return Auswahl.
   */
  private SelectInput getSize()
  {
    if (this.size != null)
      return this.size;

    List<String> sizes = new ArrayList<String>();
    sizes.add(IconBarSettings.SIZE_SMALL);
    sizes.add(IconBarSettings.SIZE_MEDIUM);
    sizes.add(IconBarSettings.SIZE_LARGE);
    this.size = new SelectInput(sizes,IconBarSettings.getSize()) {
      protected String format(Object bean)
      {
        if (IconBarSettings.SIZE_SMALL.equals(bean))
          return Application.getI18n().tr("Klein");
        if (IconBarSettings.SIZE_LARGE.equals(bean))
          return Application.getI18n().tr("Gro\u00df");
        return Application.getI18n().tr("Mittel");
      }
    };
    this.size.setName(Application.getI18n().tr("Icon-Gr\u00f6\u00dfe"));
    this.size.addListener(event -> refreshTable());
    return this.size;
  }

  /**
   * Liefert die Tabelle.
   * @return Tabelle.
   */
  private TablePart getTable()
  {
    if (this.table != null)
      return this.table;

    this.table = new TablePart(IconBarSettings.getEntries(),null);
    this.table.addColumn(Application.getI18n().tr("Bezeichnung"),"name");
    this.table.addColumn(Application.getI18n().tr("Quelle"),"source");
    this.table.addColumn(Application.getI18n().tr("Icon"),"icon");
    this.table.addColumn(Application.getI18n().tr("Ziel"),"itemId");
    this.table.setFormatter(new EntryFormatter());
    this.table.setMulti(false);
    this.table.setRememberOrder(false);
    this.table.setSortable(false);
    this.table.removeFeature(FeatureSummary.class);
    this.table.addSelectionListener(event -> updateButtons());
    return this.table;
  }

  /**
   * Aktualisiert die Buttons.
   */
  private void updateButtons()
  {
    IconBarEntry entry = (IconBarEntry) getTable().getSelection();
    boolean selected = entry != null;
    boolean spacer = selected && IconBarEntry.TYPE_SPACER.equals(entry.getType());
    boolean hasCustomIcon = selected && !spacer && StringUtils.trimToNull(entry.getIcon()) != null;
    if (this.remove != null)
      this.remove.setEnabled(selected);
    if (this.up != null)
      this.up.setEnabled(selected);
    if (this.down != null)
      this.down.setEnabled(selected);
    if (this.icon != null)
      this.icon.setEnabled(selected && !spacer);
    if (this.resetIcon != null)
      this.resetIcon.setEnabled(hasCustomIcon);
  }

  /**
   * Aktualisiert die Tabelle.
   */
  private void refreshTable()
  {
    if (this.table == null)
      return;

    try
    {
      Object selection = this.table.getSelection();
      List items = this.table.getItems(false);
      this.table.removeAll();
      disposeTableImages();
      for (Object item:items)
        this.table.addItem(item);
      if (selection != null)
        this.table.select(selection);
    }
    catch (Exception e)
    {
      Logger.error("unable to refresh icon bar settings table",e);
    }
  }

  /**
   * Entsorgt die fuer die Tabelle skalierten Bilder.
   */
  private void disposeTableImages()
  {
    for (Image image:this.tableImages)
    {
      if (image != null && !image.isDisposed())
        image.dispose();
    }
    this.tableImages.clear();
  }

  private Button getAddButton()
  {
    if (this.add != null)
      return this.add;
    this.add = new Button(Application.getI18n().tr("Hinzuf\u00fcgen"),new Add(),null,false,"list-add.png");
    return this.add;
  }

  private Button getSpaceButton()
  {
    if (this.space != null)
      return this.space;
    this.space = new Button(Application.getI18n().tr("Abstand einf\u00fcgen"),new AddSpacer(),null,false,"media-playback-stop.png");
    return this.space;
  }

  private Button getRemoveButton()
  {
    if (this.remove != null)
      return this.remove;
    this.remove = new Button(Application.getI18n().tr("Entfernen"),new Remove(),null,false,"list-remove.png");
    this.remove.setEnabled(false);
    return this.remove;
  }

  private Button getUpButton()
  {
    if (this.up != null)
      return this.up;
    this.up = new Button(Application.getI18n().tr("Nach oben"),new Move(-1),null,false,"maximize.png");
    this.up.setEnabled(false);
    return this.up;
  }

  private Button getDownButton()
  {
    if (this.down != null)
      return this.down;
    this.down = new Button(Application.getI18n().tr("Nach unten"),new Move(1),null,false,"minimize.png");
    this.down.setEnabled(false);
    return this.down;
  }

  private Button getIconButton()
  {
    if (this.icon != null)
      return this.icon;
    this.icon = new Button(Application.getI18n().tr("Icon w\u00e4hlen"),new ChooseIcon(),null,false,"document-open.png");
    this.icon.setEnabled(false);
    return this.icon;
  }

  private Button getResetIconButton()
  {
    if (this.resetIcon != null)
      return this.resetIcon;
    this.resetIcon = new Button(Application.getI18n().tr("Icon zur\u00fccksetzen"),new ResetIcon(),null,false,"edit-undo.png");
    this.resetIcon.setEnabled(false);
    return this.resetIcon;
  }

  /**
   * Formatiert die Eintraege der Symbolleiste.
   */
  private class EntryFormatter implements TableFormatter
  {
    public void format(TableItem item)
    {
      if (item == null || !(item.getData() instanceof IconBarEntry))
        return;

      IconBarEntry entry = (IconBarEntry) item.getData();
      if (IconBarEntry.TYPE_SPACER.equals(entry.getType()))
      {
        item.setText(0,Application.getI18n().tr("Abstand"));
        item.setText(1,Application.getI18n().tr("Abstand"));
        item.setText(2,"");
        item.setText(3,"");
        return;
      }

      org.eclipse.swt.graphics.Image image = null;
      String icon = StringUtils.trimToNull(entry.getIcon());
      if (icon != null)
      {
        image = SWTUtil.getImage(icon);
        item.setText(2,"");
      }
      else
      {
        de.willuhn.jameica.gui.Item source = de.willuhn.jameica.gui.IconBar.resolve(entry);
        image = getDefaultImage(source);
        item.setText(2,"");
      }

      if (image != null && !image.isDisposed())
        item.setImage(2,scaleTableImage(image));
    }
  }

  /**
   * Skaliert ein Bild fuer die Tabelle.
   * @param image Bild.
   * @return skaliertes Bild.
   */
  private Image scaleTableImage(Image image)
  {
    if (image == null || image.isDisposed())
      return null;

    int size = getSelectedIconSize();
    Rectangle bounds = image.getBounds();
    if (bounds.width <= 1 || bounds.height <= 1)
      return null;

    if (bounds.width == size && bounds.height == size)
      return image;

    ImageData data = image.getImageData().scaledTo(size,size);
    Image scaled = new Image(GUI.getDisplay(),data);
    this.tableImages.add(scaled);
    return scaled;
  }

  /**
   * Liefert die im Dialog ausgewaehlte Icon-Groesse.
   * @return Icon-Groesse.
   */
  private int getSelectedIconSize()
  {
    Object selected = getSize().getValue();
    if (IconBarSettings.SIZE_SMALL.equals(selected))
      return 16;
    if (IconBarSettings.SIZE_LARGE.equals(selected))
      return 32;
    return 24;
  }

  /**
   * Liefert das Standard-Icon eines Eintrags.
   * @param item Quell-Item.
   * @return Icon oder NULL.
   */
  private org.eclipse.swt.graphics.Image getDefaultImage(de.willuhn.jameica.gui.Item item)
  {
    try
    {
      if (item instanceof de.willuhn.jameica.gui.NavigationItem)
        return ((de.willuhn.jameica.gui.NavigationItem)item).getIconClose();
      if (item instanceof de.willuhn.jameica.gui.MenuItem)
        return ((de.willuhn.jameica.gui.MenuItem)item).getIcon();
      if (item != null && IconBarEntry.TYPE_BOOKMARK.equals(item.getAttribute("type")))
        return SWTUtil.getImage("starred.png");
    }
    catch (Exception e)
    {
      Logger.error("unable to resolve icon bar entry image",e);
    }
    return null;
  }

  /**
   * Action zum Hinzuf\u00fcgen.
   */
  private class Add implements Action
  {
    public void handleAction(Object context) throws ApplicationException
    {
      try
      {
        List<IconBarEntry> items = new ArrayList<IconBarEntry>();
        items.addAll(GUI.getNavigation().getActionItems());
        items.addAll(GUI.getMenu().getActionItems());
        items.addAll(de.willuhn.jameica.gui.IconBar.getBookmarkItems());
        SearchableListDialog dialog = new SearchableListDialog(items,AbstractDialog.POSITION_CENTER);
        dialog.setText(Application.getI18n().tr("Bitte wählen Sie das hinzuzufügende Element."));
        dialog.setTitle(Application.getI18n().tr("Eintrag w\u00e4hlen"));
        dialog.addColumn(Application.getI18n().tr("Bezeichnung"),"name");
        dialog.addColumn(Application.getI18n().tr("Quelle"),"source");
        dialog.addColumn(Application.getI18n().tr("Plugin"),"plugin");
        dialog.addColumn(Application.getI18n().tr("ID"),"itemId");
        IconBarEntry entry = (IconBarEntry) dialog.open();
        if (entry == null)
          return;

        for (Object current:getTable().getItems(false))
        {
          IconBarEntry e = (IconBarEntry) current;
          if (e.getType().equals(entry.getType()) && e.getItemId().equals(entry.getItemId()))
            return;
        }

        getTable().addItem(entry);
        getTable().select(entry);
        entriesChanged = true;
        updateButtons();
      }
      catch (Exception e)
      {
        if (e instanceof OperationCanceledException)
        {
          Logger.debug("operation cancelled");
          return;
        }
        Logger.error("unable to add icon bar entry",e);
      }
    }
  }

  /**
   * Action zum Einfuegen eines Abstands.
   */
  private class AddSpacer implements Action
  {
    public void handleAction(Object context) throws ApplicationException
    {
      try
      {
        IconBarEntry spacer = IconBarEntry.createSpacer();
        IconBarEntry selected = (IconBarEntry) getTable().getSelection();
        int target = getTable().size();
        if (selected != null)
        {
          List items = getTable().getItems(false);
          int index = items.indexOf(selected);
          if (index >= 0)
            target = index + 1;
        }

        getTable().addItem(spacer,target);
        getTable().select(spacer);
        entriesChanged = true;
        updateButtons();
      }
      catch (Exception e)
      {
        Logger.error("unable to add icon bar spacer",e);
      }
    }
  }

  /**
   * Action zum Entfernen.
   */
  private class Remove implements Action
  {
    public void handleAction(Object context) throws ApplicationException
    {
      IconBarEntry entry = (IconBarEntry) getTable().getSelection();
      if (entry == null)
        return;
      getTable().removeItem(entry);
      entriesChanged = true;
      updateButtons();
    }
  }

  /**
   * Action zum Verschieben.
   */
  private class Move implements Action
  {
    private int direction;

    private Move(int direction)
    {
      this.direction = direction;
    }

    public void handleAction(Object context) throws ApplicationException
    {
      try
      {
        IconBarEntry entry = (IconBarEntry) getTable().getSelection();
        if (entry == null)
          return;

        int index = getTable().removeItem(entry);
        if (index == -1)
          return;

        int target = index + this.direction;
        if (target < 0)
          target = 0;
        if (target > getTable().size())
          target = getTable().size();

        getTable().addItem(entry,target);
        getTable().select(entry);
        entriesChanged = true;
        updateButtons();
      }
      catch (Exception e)
      {
        Logger.error("unable to move icon bar entry",e);
      }
    }
  }

  /**
   * Action zum Ausw\u00e4hlen eines Icons.
   */
  private class ChooseIcon implements Action
  {
    public void handleAction(Object context) throws ApplicationException
    {
      try
      {
        IconBarEntry entry = (IconBarEntry) getTable().getSelection();
        if (entry == null)
          return;
        if (IconBarEntry.TYPE_SPACER.equals(entry.getType()))
          return;

        List<IconFile> icons = getIcons();
        SearchableListDialog dialog = new SearchableListDialog(icons,AbstractDialog.POSITION_CENTER);
        dialog.setText(Application.getI18n().tr("Bitte wählen Sie das hinzuzufügende Element."));
        dialog.setTitle(Application.getI18n().tr("Icon w\u00e4hlen"));
        dialog.addColumn(Application.getI18n().tr("Vorschau"),"preview");
        dialog.addColumn(Application.getI18n().tr("Datei"),"name");
        IconFileFormatter formatter = new IconFileFormatter();
        dialog.setFormatter(formatter);
        IconFile icon = null;
        try
        {
          icon = (IconFile) dialog.open();
        }
        finally
        {
          formatter.dispose();
        }
        if (icon == null)
          return;

        int index = getTable().removeItem(entry);
        entry.setIcon(icon.getName());
        getTable().addItem(entry,index);
        getTable().select(entry);
        entriesChanged = true;
        updateButtons();
        updateButtons();
      }
      catch (Exception e)
      {
        if (e instanceof OperationCanceledException)
        {
          Logger.debug("operation cancelled");
          return;
        }
        Logger.error("unable to choose icon",e);
      }
    }
  }

  /**
   * Action zum Zuruecksetzen des Icons.
   */
  private class ResetIcon implements Action
  {
    public void handleAction(Object context) throws ApplicationException
    {
      try
      {
        IconBarEntry entry = (IconBarEntry) getTable().getSelection();
        if (entry == null)
          return;
        if (IconBarEntry.TYPE_SPACER.equals(entry.getType()))
          return;

        int index = getTable().removeItem(entry);
        entry.setIcon(null);
        getTable().addItem(entry,index);
        getTable().select(entry);
        entriesChanged = true;
        updateButtons();
      }
      catch (Exception e)
      {
        Logger.error("unable to reset icon",e);
      }
    }
  }

  /**
   * Formatiert die Icon-Auswahl mit Vorschau.
   */
  private class IconFileFormatter implements TableFormatter
  {
    private List<Image> images = new ArrayList<Image>();

    public void format(TableItem item)
    {
      if (item == null || item.getData() == null || !(item.getData() instanceof IconFile))
        return;

      IconFile icon = (IconFile) item.getData();
      Image image = scale(SWTUtil.getImage(icon.getName()),IconBarSettings.getIconSize());
      if (image != null && !image.isDisposed())
        item.setImage(0,image);
    }

    /**
     * Skaliert ein Bild fuer die Vorschau.
     * @param image Bild.
     * @param size Zielgroesse.
     * @return skaliertes Bild.
     */
    private Image scale(Image image, int size)
    {
      if (image == null || image.isDisposed())
        return null;

      Rectangle bounds = image.getBounds();
      if (bounds.width <= 1 || bounds.height <= 1)
        return null;

      if (bounds.width == size && bounds.height == size)
        return image;

      ImageData data = image.getImageData().scaledTo(size,size);
      Image scaled = new Image(GUI.getDisplay(),data);
      this.images.add(scaled);
      return scaled;
    }

    /**
     * Entsorgt die fuer die Vorschau skalierten Bilder.
     */
    private void dispose()
    {
      for (Image image:this.images)
      {
        if (image != null && !image.isDisposed())
          image.dispose();
      }
      this.images.clear();
    }
  }

  /**
   * Liefert die verf\u00fcgbaren Icons.
   * @return Icons.
   */
  private List<IconFile> getIcons()
  {
    List<String> names = new ArrayList<String>();
    addIcons(new File("src/img"),names);
    addIcons(new File("img"),names);
    addIcons(new File(Application.getConfig().getWorkDir(),"img"),names);
    for (Manifest mf:Application.getPluginLoader().getInstalledManifests())
    {
      File dir = new File(mf.getPluginDir());
      addIcons(new File(dir,"img"),names);
      addIcons(new File(dir,"src/img"),names);
      addJarIcons(dir,names);
    }

    Collections.sort(names);
    List<IconFile> result = new ArrayList<IconFile>();
    String last = null;
    for (String name:names)
    {
      if (name.equals(last))
        continue;
      result.add(new IconFile(name));
      last = name;
    }
    return result;
  }

  /**
   * Fuegt Icons aus einem Verzeichnis hinzu.
   * @param dir Verzeichnis.
   * @param names Dateinamen.
   */
  private void addIcons(File dir, List<String> names)
  {
    addIcons(dir,dir,names);
  }

  /**
   * Fuegt Icons aus einem Verzeichnis und dessen Unterverzeichnissen hinzu.
   * @param root Wurzelverzeichnis, relativ zu dem die Icon-Namen gespeichert werden.
   * @param dir aktuelles Verzeichnis.
   * @param names Dateinamen.
   */
  private void addIcons(File root, File dir, List<String> names)
  {
    if (root == null || dir == null || !dir.isDirectory())
      return;

    File[] files = dir.listFiles();
    if (files == null)
      return;

    for (File file:files)
    {
      if (file.isDirectory())
      {
        addIcons(root,file,names);
        continue;
      }

      if (!file.isFile())
        continue;

      String name = root.toPath().relativize(file.toPath()).toString().replace(File.separatorChar,'/');
      if (isImageFile(name) && isIconSizeAllowed(file))
        names.add(name);
    }
  }

  /**
   * Fuegt Icons aus Jar-Dateien eines Plugin-Verzeichnisses hinzu.
   * @param dir Plugin-Verzeichnis.
   * @param names Dateinamen.
   */
  private void addJarIcons(File dir, List<String> names)
  {
    if (dir == null || !dir.isDirectory())
      return;

    File[] files = dir.listFiles();
    if (files == null)
      return;

    for (File file:files)
    {
      if (file.isDirectory())
      {
        addJarIcons(file,names);
        continue;
      }

      if (!file.isFile() || !file.getName().toLowerCase().endsWith(".jar"))
        continue;

      addJarFileIcons(file,names);
    }
  }

  /**
   * Fuegt Icons aus einer Jar-Datei hinzu.
   * @param file Jar-Datei.
   * @param names Dateinamen.
   */
  private void addJarFileIcons(File file, List<String> names)
  {
    JarFile jar = null;
    try
    {
      jar = new JarFile(file);
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements())
      {
        JarEntry entry = entries.nextElement();
        if (entry == null || entry.isDirectory())
          continue;

        String name = entry.getName();
        if (!name.startsWith("img/") || !isImageFile(name))
          continue;

        String icon = name.substring("img/".length());
        if (icon.length() == 0)
          continue;

        InputStream is = null;
        try
        {
          is = jar.getInputStream(entry);
          if (isIconSizeAllowed(is,name))
            names.add(icon);
        }
        finally
        {
          de.willuhn.io.IOUtil.close(is);
        }
      }
    }
    catch (Exception e)
    {
      Logger.warn("unable to inspect jar file " + file + ": " + e.getMessage());
    }
    finally
    {
      de.willuhn.io.IOUtil.close(jar);
    }
  }

  /**
   * Prueft, ob die Datei ein unterstuetztes Bildformat hat.
   * @param name Dateiname.
   * @return true, wenn das Format unterstuetzt wird.
   */
  private boolean isImageFile(String name)
  {
    if (name == null)
      return false;

    String lower = name.toLowerCase();
    return lower.endsWith(".png") || lower.endsWith(".gif") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".bmp");
  }

  /**
   * Prueft, ob das Bild maximal 512x512 Pixel gross ist.
   * @param file Bilddatei.
   * @return true, wenn es als Icon angeboten werden darf.
   */
  private boolean isIconSizeAllowed(File file)
  {
    try
    {
      org.eclipse.swt.graphics.ImageData data = new org.eclipse.swt.graphics.ImageData(file.getAbsolutePath());
      return isIconSizeAllowed(data);
    }
    catch (Exception e)
    {
      Logger.warn("unable to inspect icon file " + file + ": " + e.getMessage());
      return false;
    }
  }

  /**
   * Prueft, ob ein Bild maximal 512x512 Pixel gross ist.
   * @param is Bilddaten.
   * @param name Name fuer das Logging.
   * @return true, wenn es als Icon angeboten werden darf.
   */
  private boolean isIconSizeAllowed(InputStream is, String name)
  {
    try
    {
      org.eclipse.swt.graphics.ImageData data = new org.eclipse.swt.graphics.ImageData(is);
      return isIconSizeAllowed(data);
    }
    catch (Exception e)
    {
      Logger.warn("unable to inspect icon file " + name + ": " + e.getMessage());
      return false;
    }
  }

  /**
   * Prueft die Bildgroesse.
   * @param data Bilddaten.
   * @return true, wenn die Groesse erlaubt ist.
   */
  private boolean isIconSizeAllowed(org.eclipse.swt.graphics.ImageData data)
  {
    return data != null && data.width <= 512 && data.height <= 512;
  }

  /**
   * Icon-Datei fuer den Auswahl-Dialog.
   */
  public static class IconFile implements GenericObject
  {
    private String name;

    public IconFile(String name)
    {
      this.name = StringUtils.trimToNull(name);
    }

    public String getName()
    {
      return this.name;
    }

    public Object getAttribute(String name) throws RemoteException
    {
      if ("preview".equals(name))
        return "";
      if ("name".equals(name))
        return getName();
      return null;
    }

    public String[] getAttributeNames() throws RemoteException
    {
      return new String[] {"name"};
    }

    public String getID() throws RemoteException
    {
      return this.name;
    }

    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }

    public boolean equals(GenericObject other) throws RemoteException
    {
      return other != null && getID().equals(other.getID());
    }
  }
}
