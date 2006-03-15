/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Button.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/03/15 16:25:32 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Ein Button.
 */
public class Button implements Part
{

  private String title      = null;
  private Action action     = null;
  private Object context    = null;
  private boolean isDefault = false;
  private boolean enabled   = true;
  
  /**
   * ct.
   * @param title Beschriftung.
   * @param action Action, die beim Klick ausgefuehrt werden soll.
   */
  public Button(String title, Action action)
  {
    this(title,action,null,false);
  }

  /**
   * ct.
   * @param title Beschriftung.
   * @param action Action, die beim Klick ausgefuehrt werden soll.
   * @param context ein Context-Objekt, welches beim Click der Action uebergeben wird.
   */
  public Button(String title, Action action, Object context)
  {
    this(title,action,context,false);
  }

  /**
   * ct.
   * @param title Beschriftung.
   * @param action Action, die beim Klick ausgefuehrt werden soll.
   * @param context ein Context-Objekt, welches beim Click der Action uebergeben wird.
   * @param defaultButton legt fest, ob das der Default-Button der Shell sein soll.
   */
  public Button(String title, Action action, Object context, boolean defaultButton)
  {
    this.title = title;
    this.action = action;
    this.context = context;
    this.isDefault = defaultButton;
  }

  /**
   * Legt fest, ob der Button aktiviert oder deaktiviert sein soll.
   * @param enabled true, wenn der Button anklickbar sein soll, sonst false.
   */
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
  }
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    final org.eclipse.swt.widgets.Button button = GUI.getStyleFactory().createButton(parent);
    button.setText(this.title);
    button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

    if (this.isDefault)
      GUI.getShell().setDefaultButton(button);
    
    button.setEnabled(this.enabled);

    button.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent e) {
        GUI.startSync(new Runnable()
        {
          public void run()
          {
            try
            {
              action.handleAction(context);
            }
            catch (ApplicationException e)
            {
              Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getMessage(),StatusBarMessage.TYPE_ERROR));
            }
          }
        });
      }
    });
  }

}


/*********************************************************************
 * $Log: Button.java,v $
 * Revision 1.2  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 * Revision 1.1  2005/08/30 22:38:10  web0
 * @N new button
 *
 **********************************************************************/