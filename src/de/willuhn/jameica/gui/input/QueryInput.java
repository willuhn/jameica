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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;

/**
 * Vorkonfiguriertes Input-Feld mit einer Lupe.
 */
public abstract class QueryInput extends TextInput
{
  /**
   * ct.
   */
  public QueryInput()
  {
    this(0);
  }

  /**
   * ct.
   * @param maxLength
   */
  public QueryInput(int maxLength)
  {
    this(maxLength,null);
  }

  /**
   * ct.
   * @param maxLength
   * @param hint
   */
  public QueryInput(int maxLength, String hint)
  {
    super(null, maxLength, hint);
    
    if (hint == null)
      this.setHint(Application.getI18n().tr("Suche..."));
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.TextInput#getTextWidget()
   */
  @Override
  protected Text getTextWidget()
  {
    if (this.text != null)
      return this.text;
    
    this.text = GUI.getStyleFactory().createText(getParent(),SWT.SINGLE | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
    text.addSelectionListener(new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
       */
      public void widgetDefaultSelected(SelectionEvent e)
      {
        if (text == null || text.isDisposed() || !text.isFocusControl())
          return;
        
        try
        {
          if (e.detail == SWT.ICON_SEARCH || e.detail == SWT.ICON_CANCEL)
            doSearch((String)getValue());
        }
        finally
        {
          // bewirkt, dass das Event nicht noch von weiteren
          // Listenern ausgewertet und die Suche u.U. ein
          // zweites Mal ausloest.
          e.doit = false;
        }
      }
    });
    text.addTraverseListener(new TraverseListener()
    {
      /**
       * @see org.eclipse.swt.events.TraverseListener#keyTraversed(org.eclipse.swt.events.TraverseEvent)
       */
      public void keyTraversed(TraverseEvent e)
      {
        if (text == null || text.isDisposed() || !text.isFocusControl())
          return;

        if (e.detail != SWT.TRAVERSE_RETURN)
          return;
        
        try
        {
          doSearch((String)getValue());
        }
        finally
        {
          // bewirkt, dass das Event nicht noch von weiteren
          // Listenern ausgewertet und die Suche u.U. ein
          // zweites Mal ausloest.
          e.doit = false;
        }
      }
    });

    return this.text;
  }
  
  /**
   * Fuehrt die Suche durch.
   * @param query der Suchbegriff.
   */
  public abstract void doSearch(String query);
  
}


