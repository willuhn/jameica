/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Button.java,v $
 * $Revision: 1.7 $
 * $Date: 2009/01/20 10:51:51 $
 * $Author: willuhn $
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
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ein Button.
 */
public class Button implements Part
{

  private String title      = null;
  private String icon       = null;
  private Action action     = null;
  private Object context    = null;
  private boolean isDefault = false;
  private boolean enabled   = true;
  
  private org.eclipse.swt.widgets.Button button = null;
  
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
    this(title,action,context,defaultButton,null);
  }

  /**
   * ct.
   * @param title Beschriftung.
   * @param action Action, die beim Klick ausgefuehrt werden soll.
   * @param context ein Context-Objekt, welches beim Click der Action uebergeben wird.
   * @param defaultButton legt fest, ob das der Default-Button der Shell sein soll.
   * @param Icon, welches links neben der Beschriftung angezeigt werden soll.
   */
  public Button(String title, Action action, Object context, boolean defaultButton, String icon)
  {
    this.title     = title;
    this.action    = action;
    this.context   = context;
    this.isDefault = defaultButton;
    this.icon      = icon;
  }

  /**
   * Legt fest, ob der Button aktiviert oder deaktiviert sein soll.
   * @param enabled true, wenn der Button anklickbar sein soll, sonst false.
   */
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
    if (this.button != null && !this.button.isDisposed())
      this.button.setEnabled(this.enabled);
  }
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    button = GUI.getStyleFactory().createButton(parent);
    button.setText(this.title == null ? "" : this.title);
    button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    if (this.icon != null)
      button.setImage(SWTUtil.getImage(this.icon));

    try
    {
      if (this.isDefault)
        parent.getShell().setDefaultButton(button);
    }
    catch (IllegalArgumentException ae)
    {
      // Kann unter MacOS wohl passieren. Siehe Mail von
      // Jan Lolling vom 22.09.2006. Mal schauen, ob wir
      // Fehlertext: "Widget has the wrong parent"
      // Wir versuchen es mal mit der Shell der GUI.
      try
      {
        GUI.getShell().setDefaultButton(button);
      }
      catch (IllegalArgumentException ae2)
      {
        // Geht auch nicht? Na gut, dann lassen wir es halt bleiben
        Logger.warn("unable to set default button: " + ae2.getLocalizedMessage());
      }
    }
    
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
 * Revision 1.7  2009/01/20 10:51:51  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 * Revision 1.6  2006/10/02 16:15:13  willuhn
 * @B IllegalArgumentException unter MacOS
 *
 * Revision 1.5  2006/07/17 21:57:23  willuhn
 * @C NPE check
 *
 * Revision 1.4  2006/07/10 10:33:18  willuhn
 * @B OK-button was not set as default button
 *
 * Revision 1.3  2006/03/20 23:37:04  web0
 * @B misc widget updates
 *
 * Revision 1.2  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 * Revision 1.1  2005/08/30 22:38:10  web0
 * @N new button
 *
 **********************************************************************/