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

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Ist zustaendig fuer Eingabefelder des Typs "Select" aka "Combo".
 * Wird die Combo-Box mit einer Liste von {@link GenericObject}s erzeugt,
 * dann wird das Primaerattribut eines jeden Objektes angezeigt.
 * @author willuhn
 */
public class SelectInput extends AbstractInput
{
  // Fachdaten
  private List list           = null;
  private Object preselected  = null;
  private String attribute    = null;
  
  // SWT-Daten
  private Combo combo         = null;
  private boolean enabled     = true;
  private boolean editable    = false;
  private String pleaseChoose = null;


  /**
   * Erzeugt eine neue Combo-Box und schreibt die Werte der uebergebenen Liste rein.
   *
   * @param list Liste von Objekten.
   * @param preselected das Object, welches vorselektiert sein soll. Optional.
   * @throws RemoteException
   * @deprecated Um Jameica von spezifischem Code aus de.willuhn.datasource zu befreien,
   *    sollte kuenftig besser {@link SelectInput#SelectInput(List, Object)}
   *    verwendet werden. Damit kann die Anwendung spaeter auch auf ein anderes Persistierungsframework
   *    umgestellt werden.
   */
  @Deprecated
  public SelectInput(GenericIterator list, GenericObject preselected) throws RemoteException
  {
    this(list != null ? PseudoIterator.asList(list) : null,preselected);
  }
  
  /**
   * Erzeugt die Combox-Box mit Beans oder Strings.
   * @param list Liste der Objekte.
   * @param preselected das vorausgewaehlte Objekt.
   */
  public SelectInput(Object[] list, Object preselected)
  {
    this(list != null ? Arrays.asList(list) : null,preselected);
  }

  /**
   * Erzeugt die Combox-Box mit Beans oder Strings.
   * @param list Liste der Objekte.
   * @param preselected das vorausgewaehlte Objekt.
   */
  public SelectInput(List list, Object preselected)
  {
    super();
    this.list        = list;
    this.preselected = preselected;
  }

	/**
	 * Aendert nachtraeglich das vorausgewaehlte Element.
   * @param preselected neues vorausgewaehltes Element.
   */
  public void setPreselected(Object preselected)
  {
    this.preselected = preselected;
    
    if (this.combo == null || this.combo.isDisposed() || this.list == null)
      return;
    
    if (this.preselected == null)
      this.combo.select(0);

    boolean havePleaseChoose = this.pleaseChoose != null && this.pleaseChoose.length() > 0;
    int size = this.list.size();
    for (int i=0;i<size;++i)
    {
      int pos = havePleaseChoose ? (i+1) : i;
      Object value = this.combo.getData(Integer.toString(pos));
      if (value == null) // Fuer den Fall, dass die equals-Methode von preselected nicht mit null umgehen kann
        continue;

      try
      {
        if (BeanUtil.equals(preselected,value))
        {
          this.combo.select(pos);
          return;
        }
      }
      catch (RemoteException re)
      {
        Logger.error("unable to compare objects",re);
        return;
      }
    }
  }
	
  /**
   * Optionale Angabe eines Textes, der an Position 1 angezeigt werden soll.
   *
   * <p>Als Default wird {@code null} zurueckgeliefert.
   *
   * @param choose Anzuzeigender "Bitte wählen..."-Text.
   */
  public void setPleaseChoose(String choose)
  {
    this.pleaseChoose = choose;
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#addListener(org.eclipse.swt.widgets.Widget, org.eclipse.swt.widgets.Listener)
   */
  @Override
  protected void addListener(Widget w, Listener l)
  {
    // Bei Select-Boxen reagieren wir nicht auf die Focus-Events
    w.addListener(SWT.Selection,l);
  }
  
  /**
   * Legt den Namen des Attributes fest, welches von den Objekten angezeigt werden
   * soll. Bei herkoemmlichen Beans wird also ein Getter mit diesem Namen aufgerufen. 
   * Wird kein Attribut angegeben, wird bei Objekten des Typs {@link GenericObject}
   * der Wert des Primaer-Attributes angezeigt, andernfalls der Wert von {@link #toString()}.
   * @param name Name des anzuzeigenden Attributes (muss via
   *             {@link GenericObject#getAttribute(String)} abrufbar sein).
   */
  public void setAttribute(String name)
	{
		if (name != null)
			this.attribute = name;
	}

  @Override
  public Control getControl()
  {
    if (this.combo != null)
      return this.combo;

    this.combo = GUI.getStyleFactory().createCombo(getParent(),this.editable ? SWT.NONE : SWT.READ_ONLY);
    this.combo.setVisibleItemCount(15); // Patch von Heiner
    this.combo.setEnabled(enabled);

    Object tooltip = this.getData(DATAKEY_TOOLTIP);
    if (tooltip != null)
      this.combo.setToolTipText(tooltip.toString());

    // Daten in die Liste uebernehmen
    applyList();
    
    return this.combo;
  }
  
  /**
   * Uebernimmt die Liste mit den Daten in das Control
   */
  private void applyList()
  {
    if (this.combo == null || this.combo.isDisposed())
      return;

    // Erstmal alles aus der Liste entfernen
    this.combo.removeAll();

    int selected             = -1;
    boolean havePleaseChoose = false;

    // Haben wir einen "bitte waehlen..."-Text?
    if (this.pleaseChoose != null && this.pleaseChoose.length() > 0)
    {
      this.combo.add(this.pleaseChoose);
      havePleaseChoose = true;
    }

    if (this.list != null)
    {
      try
      {
        int size = this.list.size();
        for (int i=0;i<size;++i)
        {
          Object object = this.list.get(i);

          if (object == null)
            continue;

          // Anzuzeigenden Text ermitteln
          String text = format(object);
          if (text == null)
            continue;
          this.combo.add(text);
          this.combo.setData(Integer.toString(havePleaseChoose ? i+1 : i),object);
          
          // Wenn unser Objekt dem vorausgewaehlten entspricht, und wir noch
          // keines ausgewaehlt haben merken wir uns dessen Index
          if (selected == -1 && this.preselected != null)
          {
            if (BeanUtil.equals(object,this.preselected))
            {
              selected = i;
              if (havePleaseChoose)
                selected++;
            }
          }
        }
      }
      catch (RemoteException e)
      {
        this.combo.removeAll();
        this.combo.add(Application.getI18n().tr("Fehler beim Laden der Daten..."));
        Logger.error("unable to create combo box",e);
      }
    }

    this.combo.select(selected > -1 ? selected : 0);

    // BUGZILLA 550
    if (this.editable && this.preselected != null && !this.list.contains(this.preselected) && (this.preselected instanceof String))
      this.combo.setText((String)this.preselected);
  }
  
  /**
   * Ersetzt den Inhalt der Selectbox komplett gegen die angegebene Liste.
   * @param list die neue Liste der Daten.
   */
  public void setList(List list)
  {
    this.list = list;
    this.applyList();
  }
  
  /**
   * Liefert die komplette Liste der Fachobjekte in der Liste.
   * @return Liste der Fachobjekte.
   */
  public List getList()
  {
    return this.list;
  }

  /**
   * Formatiert die Bean passend fuer die Anzeige in der Combo-Box.
   * @param bean die Bean.
   * @return anzuzeigender Wert.
   */
  protected String format(Object bean)
  {
    if (bean == null)
      return null;
    try
    {
      if (this.attribute == null || this.attribute.length() == 0)
        return BeanUtil.toString(bean);

      Object value = BeanUtil.get(bean,this.attribute);
      return value == null ? null : value.toString();
    }
    catch (RemoteException re)
    {
      Logger.error("unable to format object",re);
      return null;
    }
  }

  /**
   * Liefert das ausgewaehlte {@link GenericObject}.
   * Folglich kann der Rueckgabewert direkt nach {@link GenericObject} gecastet werden.
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  @Override
  public Object getValue()
  {
    if (this.combo == null || this.combo.isDisposed())
      return this.preselected;

    if (this.editable)
      return this.combo.getText();
    
    int selected = this.combo.getSelectionIndex();
    if (selected == -1)
      return null;
    
    return this.combo.getData(Integer.toString(selected));
  }

	/**
	 * Liefert den derzeit angezeigten Text zurueck.
   * @return Text.
   */
  public String getText()
	{
    if (this.combo == null || this.combo.isDisposed())
      return null;
		return combo.getText();
	}

  @Override
  public void focus()
  {
    if (this.combo == null || this.combo.isDisposed())
      return;
    
    combo.setFocus();
  }

  @Override
  public void disable()
  {
    setEnabled(false);
  }

  @Override
  public void enable()
  {
    setEnabled(true);
  }

  @Override
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
    if (combo != null && !combo.isDisposed())
      combo.setEnabled(enabled);
  }
  
  /**
   * Markiert die Combo-Box als editierbar.
   *
   * <p>Wenn diese Option aktiviert ist, wird jedoch in {@link #getValue()}
   * generell der angezeigte Text zurueckgeliefert statt des
   * Fachobjektes. Hintergrund: Normalerweise wird die Combo-Box
   * ja mit einer Liste von Fachobjekten/Beans gefuellt.
   * Abhaengig von der Auswahl wird dann das zugehoerige
   * dahinterstehende Objekt zurueckgeliefert. Bei Freitext-Eingabe
   * existiert jedoch kein solches. Daher wird in diesem Fall
   * der eingebene Text zurueckgeliefert.
   * @param editable
   */
  public void setEditable(boolean editable)
  {
    this.editable = editable;
  }

  @Override
  public void setValue(Object o)
  {
    this.setPreselected(o);
  }

  @Override
  public boolean isEnabled()
  {
    return enabled;
  }
}

