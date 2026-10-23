/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui;

import java.io.File;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.bookmark.Bookmark;
import de.willuhn.jameica.bookmark.BookmarkService;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.SearchableListDialog;
import de.willuhn.jameica.gui.internal.action.BookmarkOpen;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Symbolleiste unterhalb des Hauptmenus.
 */
public class IconBar implements Part
{
  private Composite parent;
  private Composite container;
  private ToolBar toolBar;
  private List<Image> images = new ArrayList<Image>();
  private IconBarEntry contextEntry = null;
  private MessageConsumer bookmarkDeletedConsumer = null;

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    this.parent = parent;
    this.container = new Composite(parent,SWT.NONE);
    this.container.setLayout(de.willuhn.jameica.gui.util.SWTUtil.createGrid(1,true));
    this.container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    registerBookmarkDeletedConsumer();
    this.container.addDisposeListener(new DisposeListener()
    {
      public void widgetDisposed(DisposeEvent e)
      {
        unregisterBookmarkDeletedConsumer();
      }
    });
    redraw();
  }

  /**
   * Zeichnet die Leiste neu.
   */
  public void redraw()
  {
    if (this.container == null || this.container.isDisposed())
      return;

    disposeToolBar();

    GridData data = (GridData) this.container.getLayoutData();
    if (!IconBarSettings.isVisible())
    {
      data.exclude = true;
      data.heightHint = 0;
      this.container.setVisible(false);
      this.parent.layout(true,true);
      return;
    }

    int height = IconBarSettings.getIconSize() + 8;
    data.exclude = false;
    data.heightHint = height;
    this.container.setVisible(true);
    this.toolBar = new ToolBar(this.container,SWT.FLAT);
    createContextMenu(this.container);
    createContextMenu(this.toolBar);
    GridData toolBarData = new GridData(GridData.FILL_HORIZONTAL);
    toolBarData.heightHint = height;
    this.toolBar.setLayoutData(toolBarData);

    List<IconBarEntry> entries = IconBarSettings.getEntries();
    for (IconBarEntry entry:entries)
      add(entry);

    this.toolBar.addDisposeListener(new DisposeListener()
    {
      public void widgetDisposed(DisposeEvent e)
      {
        disposeImages();
      }
    });

    this.container.layout(true,true);
    this.parent.layout(true,true);
  }

  /**
   * Registriert den Consumer fuer geloeschte Lesezeichen.
   */
  private void registerBookmarkDeletedConsumer()
  {
    if (this.bookmarkDeletedConsumer != null)
      return;

    this.bookmarkDeletedConsumer = new BookmarkDeletedConsumer();
    Application.getMessagingFactory().getMessagingQueue(BookmarkService.QUEUE_DELETED).registerMessageConsumer(this.bookmarkDeletedConsumer);
  }

  /**
   * Meldet den Consumer fuer geloeschte Lesezeichen ab.
   */
  private void unregisterBookmarkDeletedConsumer()
  {
    if (this.bookmarkDeletedConsumer == null)
      return;

    Application.getMessagingFactory().getMessagingQueue(BookmarkService.QUEUE_DELETED).unRegisterMessageConsumer(this.bookmarkDeletedConsumer);
    this.bookmarkDeletedConsumer = null;
  }

  /**
   * Erzeugt das Kontextmenu der Symbolleiste.
   * @param control Control.
   */
  private void createContextMenu(org.eclipse.swt.widgets.Control control)
  {
    if (control == null || control.isDisposed())
      return;

    if (control instanceof ToolBar)
    {
      final ToolBar bar = (ToolBar) control;
      bar.addListener(SWT.MenuDetect,event -> {
        ToolItem item = bar.getItem(bar.toControl(event.x,event.y));
        Object data = item != null ? item.getData("entry") : null;
        contextEntry = data instanceof IconBarEntry ? (IconBarEntry) data : null;
      });
    }
    else
    {
      control.addListener(SWT.MenuDetect,event -> contextEntry = null);
    }

    final org.eclipse.swt.widgets.Menu menu = new org.eclipse.swt.widgets.Menu(control);
    control.setMenu(menu);

    org.eclipse.swt.widgets.MenuItem add = new org.eclipse.swt.widgets.MenuItem(menu,SWT.PUSH);
    add.setText(Application.getI18n().tr("Hinzuf\u00fcgen"));
    add.setImage(SWTUtil.getImage("list-add.png"));
    add.addListener(SWT.Selection,event -> openAddDialog());

    final org.eclipse.swt.widgets.MenuItem remove = new org.eclipse.swt.widgets.MenuItem(menu,SWT.PUSH);
    remove.setText(Application.getI18n().tr("Entfernen"));
    remove.setImage(SWTUtil.getImage("list-remove.png"));
    remove.addListener(SWT.Selection,event -> removeContextEntry());

    new org.eclipse.swt.widgets.MenuItem(menu,SWT.SEPARATOR);

    org.eclipse.swt.widgets.MenuItem settings = new org.eclipse.swt.widgets.MenuItem(menu,SWT.PUSH);
    settings.setText(Application.getI18n().tr("Anpassen..."));
    settings.setImage(SWTUtil.getImage("document-properties.png"));
    settings.addListener(SWT.Selection,event -> openSettings());

    menu.addListener(SWT.Show,event -> remove.setEnabled(contextEntry != null));
  }

  /**
   * Oeffnet den Hinzufuegen-Dialog fuer die Symbolleiste.
   */
  private void openAddDialog()
  {
    try
    {
      List<IconBarEntry> items = new ArrayList<IconBarEntry>();
      if (GUI.getNavigation() != null)
        items.addAll(GUI.getNavigation().getActionItems());
      if (GUI.getMenu() != null)
        items.addAll(GUI.getMenu().getActionItems());
      items.addAll(getBookmarkItems());

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

      IconBarSettings.add(entry);
      IconBarSettings.setVisible(true);
      redraw();
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Eintrag zur Symbolleiste hinzugef\u00fcgt"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (OperationCanceledException oce)
    {
      Logger.debug("operation cancelled");
    }
    catch (Exception e)
    {
      Logger.error("unable to add icon bar entry",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Hinzuf\u00fcgen zur Symbolleiste"),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Entfernt den per Rechtsklick gewaehlten Eintrag.
   */
  private void removeContextEntry()
  {
    if (this.contextEntry == null)
      return;

    IconBarSettings.remove(this.contextEntry);
    this.contextEntry = null;
    redraw();
  }

  /**
   * Oeffnet die Einstellungen im Tab Look and Feel.
   */
  private void openSettings()
  {
    try
    {
      new de.willuhn.jameica.gui.internal.action.Settings().handleAction(Application.getI18n().tr("Look and Feel"));
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (Exception e)
    {
      Logger.error("unable to open icon bar settings",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Öffnen der Einstellungen"),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Fuegt einen Eintrag hinzu.
   * @param entry Eintrag.
   */
  private void add(final IconBarEntry entry)
  {
    try
    {
      if (IconBarEntry.TYPE_SPACER.equals(entry.getType()))
      {
        addSpacer();
        return;
      }

      final Item item = resolve(entry);
      if (item == null)
      {
        Logger.warn("icon bar item not found: " + entry.getType() + "/" + entry.getItemId());
        return;
      }

      final Action action = item.getAction();
      if (action == null)
        return;

      String name = StringUtils.trimToNull(item.getName());
      if (name == null)
        name = entry.getName();
      entry.setName(name);

      ToolItem toolItem = new ToolItem(this.toolBar,SWT.PUSH);
      toolItem.setData("entry",entry);
      toolItem.setToolTipText(name);
      toolItem.setEnabled(item.isEnabled());

      Image image = getImage(entry,item);
      if (isUsableImage(image))
      {
        toolItem.setImage(image);
      }
      else
      {
        toolItem.setText(getReplacementText(name));
      }

      toolItem.addListener(SWT.Selection, event -> execute(action,event));
    }
    catch (Exception e)
    {
      Logger.error("unable to add icon bar entry",e);
    }
  }

  /**
   * Fuegt einen Abstandshalter hinzu.
   */
  private void addSpacer()
  {
    ToolItem toolItem = new ToolItem(this.toolBar,SWT.SEPARATOR);
    toolItem.setWidth(IconBarSettings.getIconSize() + 8);
  }

  /**
   * Fuehrt die Action aus.
   * @param action Action.
   * @param event SWT-Event.
   */
  private void execute(final Action action, final Event event)
  {
    try
    {
      action.handleAction(event);
    }
    catch (OperationCanceledException oce)
    {
      Logger.debug("operation cancelled");
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (Exception e)
    {
      Logger.error("unable to execute icon bar action",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Ausf\u00fchren des Menu-Eintrages"),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Liefert das Image fuer den Eintrag.
   * @param entry Eintrag.
   * @param item Quell-Item.
   * @return Image.
   * @throws RemoteException
   */
  private Image getImage(IconBarEntry entry, Item item) throws RemoteException
  {
    String override = StringUtils.trimToNull(entry.getIcon());
    Image image = override != null && hasImage(override) ? SWTUtil.getImage(override) : getDefaultImage(item);
    return scale(image,IconBarSettings.getIconSize());
  }

  /**
   * Prueft, ob ein Bild als Icon verwendbar ist.
   * @param image Bild.
   * @return true, wenn es verwendbar ist.
   */
  private boolean isUsableImage(Image image)
  {
    if (image == null || image.isDisposed())
      return false;

    Rectangle bounds = image.getBounds();
    return bounds.width > 1 && bounds.height > 1;
  }

  /**
   * Liefert den Text-Ersatz fuer fehlende Icons.
   * @param name Beschreibung.
   * @return maximal drei Zeichen.
   */
  private String getReplacementText(String name)
  {
    name = StringUtils.trimToEmpty(name);
    if (name.length() <= 3)
      return name;
    return name.substring(0,3);
  }

  /**
   * Prueft, ob das Icon existiert.
   * @param name Dateiname.
   * @return true, wenn das Icon existiert.
   */
  private boolean hasImage(String name)
  {
    InputStream is = null;
    try
    {
      is = Application.getClassLoader().getResourceAsStream("img/" + name);
      if (is != null)
        return true;

      File file = new File(new File(Application.getConfig().getWorkDir(),"img"),name);
      return file.isFile() && file.canRead();
    }
    finally
    {
      IOUtil.close(is);
    }
  }

  /**
   * Liefert das Default-Icon des Quell-Items.
   * @param item Quell-Item.
   * @return Image.
   * @throws RemoteException
   */
  private Image getDefaultImage(Item item) throws RemoteException
  {
    if (item instanceof NavigationItem)
      return ((NavigationItem)item).getIconClose();
    if (item instanceof MenuItem)
      return ((MenuItem)item).getIcon();
    if (item instanceof BookmarkItem)
      return SWTUtil.getImage(BookmarkItem.ICON);
    return null;
  }

  /**
   * Skaliert ein Image auf die konfigurierte Groesse.
   * @param image Image.
   * @param size Zielgroesse.
   * @return skaliertes Image.
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
   * Loest einen gespeicherten Eintrag auf.
   * @param entry Eintrag.
   * @return Item oder NULL.
   */
  public static Item resolve(IconBarEntry entry)
  {
    if (entry == null)
      return null;

    if (IconBarEntry.TYPE_NAVIGATION.equals(entry.getType()))
      return GUI.getNavigation() != null ? GUI.getNavigation().getItem(entry.getItemId()) : null;

    if (IconBarEntry.TYPE_MENU.equals(entry.getType()))
      return GUI.getMenu() != null ? GUI.getMenu().getItem(entry.getItemId()) : null;

    if (IconBarEntry.TYPE_BOOKMARK.equals(entry.getType()))
      return resolveBookmark(entry.getItemId());

    return null;
  }

  /**
   * Entfernt ein geloeschtes Lesezeichen aus der Symbolleiste.
   * @param bookmark geloeschtes Lesezeichen.
   */
  private void removeBookmark(Bookmark bookmark)
  {
    String id = getBookmarkId(bookmark);
    if (!IconBarSettings.contains(IconBarEntry.TYPE_BOOKMARK,id))
      return;

    IconBarSettings.remove(new IconBarEntry(IconBarEntry.TYPE_BOOKMARK,id,"",null));
    GUI.getDisplay().asyncExec(new Runnable()
    {
      public void run()
      {
        redraw();
      }
    });
  }

  /**
   * Message-Consumer fuer geloeschte Lesezeichen.
   */
  private class BookmarkDeletedConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{QueryMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      if (!(message instanceof QueryMessage))
        return;

      Object data = ((QueryMessage)message).getData();
      if (data instanceof Bookmark)
        removeBookmark((Bookmark)data);
    }
  }

  /**
   * Liefert alle Lesezeichen als Symbolleisten-Eintraege.
   * @return Lesezeichen-Eintraege.
   */
  public static List<IconBarEntry> getBookmarkItems()
  {
    List<IconBarEntry> result = new ArrayList<IconBarEntry>();
    try
    {
      for (Bookmark bookmark:getBookmarkService().getBookmarks())
      {
        IconBarEntry entry = new IconBarEntry(IconBarEntry.TYPE_BOOKMARK,getBookmarkId(bookmark),getBookmarkName(bookmark),null);
        entry.setPlugin("Jameica");
        result.add(entry);
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to load bookmarks for icon bar",e);
    }
    return result;
  }

  /**
   * Loest ein Lesezeichen auf.
   * @param id Lesezeichen-ID.
   * @return Item oder NULL.
   */
  private static Item resolveBookmark(String id)
  {
    try
    {
      Bookmark bookmark = findBookmark(id);
      return bookmark != null ? new BookmarkItem(bookmark) : null;
    }
    catch (Exception e)
    {
      Logger.error("unable to resolve bookmark",e);
      return null;
    }
  }

  /**
   * Findet ein Lesezeichen anhand der Symbolleisten-ID.
   * @param id Lesezeichen-ID.
   * @return Lesezeichen oder NULL.
   * @throws ApplicationException
   */
  private static Bookmark findBookmark(String id) throws ApplicationException
  {
    if (id == null)
      return null;

    for (Bookmark bookmark:getBookmarkService().getBookmarks())
    {
      if (id.equals(getBookmarkId(bookmark)))
        return bookmark;
    }
    return null;
  }

  /**
   * Liefert den Lesezeichen-Service.
   * @return Service.
   */
  private static BookmarkService getBookmarkService()
  {
    BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    return service.get(BookmarkService.class);
  }

  /**
   * Liefert eine stabile ID fuer ein Lesezeichen.
   * @param bookmark Lesezeichen.
   * @return ID.
   */
  private static String getBookmarkId(Bookmark bookmark)
  {
    if (bookmark == null)
      return "";

    Date created = bookmark.getCreated();
    long time = created != null ? created.getTime() : 0L;
    return time + ":" + StringUtils.trimToEmpty(bookmark.getView()) + ":" + StringUtils.trimToEmpty(bookmark.getTitle());
  }

  /**
   * Liefert den Anzeigenamen eines Lesezeichens.
   * @param bookmark Lesezeichen.
   * @return Anzeigename.
   */
  private static String getBookmarkName(Bookmark bookmark)
  {
    if (bookmark == null)
      return Application.getI18n().tr("Lesezeichen");

    String name = StringUtils.trimToNull(bookmark.getTitle());
    if (name != null)
      return name;
    name = StringUtils.trimToNull(bookmark.getComment());
    return name != null ? name : Application.getI18n().tr("Lesezeichen");
  }

  /**
   * Item-Wrapper fuer Lesezeichen.
   */
  private static class BookmarkItem implements Item
  {
    private static final String ICON = "starred.png";
    private Bookmark bookmark;

    private BookmarkItem(Bookmark bookmark)
    {
      this.bookmark = bookmark;
    }

    public String getName() throws RemoteException
    {
      return getBookmarkName(this.bookmark);
    }

    public Action getAction() throws RemoteException
    {
      final Bookmark bookmark = this.bookmark;
      return new Action()
      {
        public void handleAction(Object context) throws ApplicationException
        {
          new BookmarkOpen().handleAction(bookmark);
        }
      };
    }

    public void addChild(Item i) throws RemoteException
    {
    }

    public boolean isEnabled() throws RemoteException
    {
      return this.bookmark != null;
    }

    public void setEnabled(boolean enabled, boolean recursive) throws RemoteException
    {
    }

    public GenericIterator getChildren() throws RemoteException
    {
      return PseudoIterator.fromArray(new Item[0]);
    }

    public boolean hasChild(GenericObjectNode object) throws RemoteException
    {
      return false;
    }

    public GenericObjectNode getParent() throws RemoteException
    {
      return null;
    }

    public GenericIterator getPossibleParents() throws RemoteException
    {
      return PseudoIterator.fromArray(new Item[0]);
    }

    public GenericIterator getPath() throws RemoteException
    {
      return PseudoIterator.fromArray(new Item[0]);
    }

    public Object getAttribute(String name) throws RemoteException
    {
      if ("name".equals(name))
        return getName();
      if ("type".equals(name))
        return IconBarEntry.TYPE_BOOKMARK;
      return null;
    }

    public String[] getAttributeNames() throws RemoteException
    {
      return new String[] {"name","type"};
    }

    public String getID() throws RemoteException
    {
      return getBookmarkId(this.bookmark);
    }

    public String getExtendableID()
    {
      return null;
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

  /**
   * Entsorgt die Toolbar.
   */
  private void disposeToolBar()
  {
    if (this.toolBar != null && !this.toolBar.isDisposed())
      this.toolBar.dispose();
    this.toolBar = null;
    disposeImages();
  }

  /**
   * Entsorgt skalierte Images.
   */
  private void disposeImages()
  {
    for (Image image:this.images)
    {
      if (image != null && !image.isDisposed())
        image.dispose();
    }
    this.images.clear();
  }
}
