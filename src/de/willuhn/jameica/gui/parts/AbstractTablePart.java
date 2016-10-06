/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Klasse von Tabellen-aehnlichen Parts.
 */
public abstract class AbstractTablePart implements Part
{

  private I18N i18n   = Application.getI18n();
  protected ContextMenu menu               = null;
  protected boolean changeable             = false;
  protected boolean rememberColWidth       = false;
  protected boolean rememberOrder          = false;
  protected boolean rememberState          = false;
  protected List<Column> columns           = new LinkedList<Column>();
  protected final static Settings settings = new Settings(AbstractTablePart.class);

  protected boolean multi                  = false; // Multiple Markierung
  protected boolean checkable              = false;
  
  protected List<Listener> selectionListeners = new ArrayList<Listener>();
  protected Action action                  = null;

  private boolean showSummary           = true;
  private boolean canSetSummaryFlag     = true;
  private Label summary                 = null;

  /**
   * ct.
   * @param action die Default-Action.
   */
  public AbstractTablePart(Action action)
  {
    this.action = action;
    this.addSelectionListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        refreshSummary();
      }
    });
  }
  
  /**
   * Fuegt der Tabelle eine neue Spalte hinzu.
   * @param title Name der Spaltenueberschrift.
   * @param field Name des Feldes aus dem dbObject, der angezeigt werden soll.
   */
  public void addColumn(String title, String field)
  {
    addColumn(title,field,null);
  }
  
  /**
   * Fuegt der Tabelle eine neue Spalte hinzu und dazu noch einen Formatierer.
   * @param title Name der Spaltenueberschrift.
   * @param field Name des Feldes aus dem dbObject, der angezeigt werden soll.
   * @param f Formatter, der fuer die Anzeige des Wertes verwendet werden soll.
   */
  public void addColumn(String title, String field, Formatter f)
  {
    addColumn(title,field,f,false);
  }

  /**
   * Fuegt der Tabelle eine neue Spalte hinzu und dazu noch einen Formatierer.
   * @param title Name der Spaltenueberschrift.
   * @param field Name des Feldes aus dem dbObject, der angezeigt werden soll.
   * @param f Formatter, der fuer die Anzeige des Wertes verwendet werden soll.
   * @param changeable legt fest, ob die Werte in dieser Spalte direkt editierbar sein sollen.
   * Wenn der Parameter true ist, dann sollte der Tabelle via <code>addChangeListener</code>
   * ein Listener hinzugefuegt werden, der benachrichtigt wird, wenn der Benutzer einen
   * Wert geaendert hat. Es ist anschliessend Aufgabe des Listeners, den geaenderten
   * Wert im Fachobjekt zu uebernehmen.
   */
  public void addColumn(String title, String field, Formatter f, boolean changeable)
  {
    addColumn(title,field,f,changeable,Column.ALIGN_AUTO);
  }
  
  /**
   * Fuegt der Tabelle eine neue Spalte hinzu und dazu noch einen Formatierer.
   * @param title Name der Spaltenueberschrift.
   * @param field Name des Feldes aus dem dbObject, der angezeigt werden soll.
   * @param f Formatter, der fuer die Anzeige des Wertes verwendet werden soll.
   * @param changeable legt fest, ob die Werte in dieser Spalte direkt editierbar sein sollen.
   * Wenn der Parameter true ist, dann sollte der Tabelle via <code>addChangeListener</code>
   * ein Listener hinzugefuegt werden, der benachrichtigt wird, wenn der Benutzer einen
   * Wert geaendert hat. Es ist anschliessend Aufgabe des Listeners, den geaenderten
   * Wert im Fachobjekt zu uebernehmen.
   * @param align die Ausrichtung
   * @see Column#ALIGN_AUTO
   * @see Column#ALIGN_CENTER
   * @see Column#ALIGN_LEFT
   * @see Column#ALIGN_RIGHT
   */
  public void addColumn(String title, String field, Formatter f, boolean changeable, int align)
  {
    addColumn(new Column(field,title,f,changeable,align));
  }

  /**
   * Fuegt der Tabelle eine neue Spalte hinzu.
   * @param col das Spalten-Objekt.
   */
  public void addColumn(Column col)
  {
    this.columns.add(col);
    this.changeable |= col.canChange();
  }

  /**
   * Liefert die Sortierreihenfolge der Spalten.
   * @return Int-Array mit der Reihenfolge oder <code>null</code>.
   */
  int[] getColumnOrder()
  {
    try
    {
      // Mal schauen, ob wir eine gespeicherte Sortierung haben
      String order = settings.getString("column.order." + getID(),null);
      if (order == null || order.length() == 0 || order.indexOf(",") == -1)
        return null;
      String[] s = order.split(",");
      if (s.length != this.columns.size())
      {
        Logger.warn("column count missmatch. column order: " + order + ", columns: " + this.columns.size());
        return null;
      }
      int[] cols = new int[s.length];
      for (int i=0;i<s.length;++i)
      {
        cols[i] = Integer.parseInt(s[i]);
      }
      return cols;
    }
    catch (Exception e)
    {
      Logger.warn("unable to determine column order: " + e.toString());
    }
    return null;
  }
  
  /**
   * Speichert die Reihenfolge der Spalten.
   * @param cols die Reihenfolge der Spalten.
   */
  void setColumnOrder(int[] cols)
  {
    try
    {
      String s = "";
      if (cols != null && cols.length > 0)
      {
        for (int i=0;i<cols.length;++i)
        {
          s += Integer.toString(cols[i]);
          if (i+1<cols.length)
            s += ",";
        }
      }
      settings.setAttribute("column.order." + getID(),s.length() == 0 ? null : s);
    }
    catch (Exception e)
    {
      Logger.error("unable to save column order",e);
    }
  }

  /**
   * Fuegt ein KontextMenu hinzu.
   * @param menu das anzuzeigende Menu.
   */
  public void setContextMenu(ContextMenu menu)
  {
    this.menu = menu;
  }
  
  /**
   * Liefert eine eindeutige ID fuer genau diese Tabelle.
   * @return die ID.
   * @throws Exception
   */
  abstract String getID() throws Exception;

  /**
   * Liefert die Fach-Objekte der Tabelle.
   * @return Liste der Fachobjekte.
   * @throws RemoteException
   */
  public abstract List getItems() throws RemoteException;

  /**
   * Liefert die markierten Objekte.
   * Die Funktion liefert je nach Markierung <code>Object</code> oder <code>Object[]</code>.
   * @return das/die markierten Objekte.
   */
  public abstract Object getSelection();

  /**
   * Markiert die Liste der uebergebenen Objekte.
   * @param objects Liste der zu markierenden Objekte.
   */
  public abstract void select(Object[] objects);

  /**
   * Markiert das uebergebene Element.
   * @param o das zu markierende Element.
   */
  public void select(Object o)
  {
    select(new Object[]{o});
  }
  
  /**
   * Oeffnet das uebergebene Element ueber die Default-Action.
   * @param o das zu oeffnende Element. 
   */
  void open(Object o)
  {
    if (action == null || o == null)
      return;

    try
    {
      action.handleAction(o);
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Legt fest, ob sich die Tabelle die Spaltenbreiten merken soll.
   * @param remember true, wenn sie sich die Spaltenbreiten merken soll.
   */
  public void setRememberColWidths(boolean remember)
  {
    this.rememberColWidth = remember;
  }
  
  /**
   * Legt fest, ob sich die Tabelle die Sortierreihenfolge merken soll.
   * @param remember true, wenn sie sich die Reihenfolge merken soll.
   */
  public void setRememberOrder(boolean remember)
  {
    this.rememberOrder = remember;
  }
  
  /**
   * Legt fest, ob sich die Tabelle die zuletzt markierten Objekte samt der Scrollposition merken soll.
   * @param remember true, wenn sich die Tabelle Selektion und Position merken soll.
   */
  public void setRememberState(boolean remember)
  {
    this.rememberState = remember;
  }

  /**
   * Stellt den Zustand wieder her (markierte Objekte und Scroll-Position).
   * Geschieht jedoch nur, wenn das Feature mit setRememberState(true) aktiviert wurde.
   * Das ist eine Dummy-Implementierung, die in den abgeleiteten Klassen ueberschrieben werden kann.
   */
  public void restoreState()
  {
  }

  /**
   * Legt fest, ob mehrere Elemente gleichzeitig markiert werden koennen.
   * Default: False.
   * @param multi true, wenn mehrere Elemente gleichzeitig markiert werden koennen.
   */
  public void setMulti(boolean multi)
  {
    this.multi = multi;
  }
  
  /**
   * Legt fest, ob jede Zeile der Tabelle mit einer Checkbox versehen werden soll.
   * Ist dies der Fall, liefert <code>getItems</code> nur noch die aktiven
   * Elemente zurueck.
   * Default: false
   * @param checkable
   */
  public void setCheckable(boolean checkable)
  {
    this.checkable = checkable;
  }
  
  /**
   * Wenn die Tabelle mit Checkboxen versehen ist, kann man damit bei einem Element 
   * das Haeckchen setzen oder entfernen.
   * Hinweis: Dies hier ist eine leere Dummy-Implementierung. Sie muss von
   * abgeleiteten Klassen ueberschrieben werden.
   * @param objects Liste der zu checkenden Objekte.
   * @param checked true, wenn das Haekchen gesetzt werden soll.
   */
  public void setChecked(Object[] objects, boolean checked)
  {
    Logger.warn("setChecked not implemented in " + this.getClass().getName());
  }
  
  /**
   * Wenn die Tabelle mit Checkboxen versehen ist, kann man damit bei einem Element das Haeckchen setzen oder entfernen.
   * @param o das zu checkende Element.
   * @param checked true, wenn das Haekchen gesetzt werden soll.
   */
  public void setChecked(Object o, boolean checked)
  {
    setChecked(new Object[]{o},checked);
  }
  
  /**
   * Fuegt der Tabelle einen Listener hinzu, der ausgeloest wird, wenn
   * ein oder mehrere Elemente markiert wurden.
   * @param l der Listener.
   */
  public void addSelectionListener(Listener l)
  {
    if (l != null)
      this.selectionListeners.add(l);
  }

  /**
   * Entfernt alle Elemente aus der Tabelle.
   */
  public abstract void removeAll();
  
  /**
   * Hilfsmethode, um die RemoteException im Konstruktor zu vermeiden.
   * @param iterator zu konvertierender Iterator.
   * @return Liste mit den Objekten.
   */
  protected static List asList(GenericIterator iterator)
  {
    if (iterator == null)
      return null;
    try
    {
      return PseudoIterator.asList(iterator);
    }
    catch (RemoteException re)
    {
      Logger.error("unable to init list",re);
    }
    return new ArrayList();
  }

  /**
   * Legt fest, ob eine Summenzeile am Ende angezeigt werden soll.
   * @param show true, wenn die Summenzeile angezeigt werden soll (Default) oder false
   * wenn sie nicht angezeigt werden soll.
   */
  public void setSummary(boolean show)
  { 
    if(canSetSummaryFlag){
      this.showSummary = show;
    }else{
      throw new IllegalStateException("setSummary can be called only before the composite the label is initialized.");
    }
  }

  protected boolean showSummary(){
    return this.showSummary;
  }

  /**
   * Aktualisiert die Summenzeile.
   */
  protected void refreshSummary()
  {
    if (!showSummary || summary == null || summary.isDisposed())
      return;
    
    // Machen wir verzoegert, weil das sonst bei Bulk-Updates unnoetig oft aufgerufen wird
    delayedSummary.handleEvent(null);
  }
  
  private Listener delayedSummary = new DelayedListener(new Listener() {
    public void handleEvent(Event event)
    {
      if (summary != null && !summary.isDisposed())
        summary.setText(getSummary());
    }
  });

  /**
   * Liefert den anzuzeigenden Summen-Text.
   * Kann von abgeleiteten Klassen ueberschrieben werde, um etwas anderes anzuzeigen.
   * @return anzuzeigender Text oder null, wenn nichts angezeigt werden soll.
   */
  protected String getSummary()
  {
    int size = size();
    if (size != 1)
      return i18n.tr("{0} Datensätze",Integer.toString(size));
    return i18n.tr("1 Datensatz");
  }

  protected void initSummaryLabel(Composite composite){
    canSetSummaryFlag=false;
    if (this.showSummary)
    {
      this.summary = GUI.getStyleFactory().createLabel(composite,SWT.NONE);
      this.summary.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      refreshSummary();
    }
  }

  protected abstract int size();
}
