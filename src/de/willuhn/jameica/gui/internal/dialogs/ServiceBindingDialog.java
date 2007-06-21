/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/dialogs/ServiceBindingDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2007/06/21 11:03:01 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.dialogs;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Config;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

/**
 * Ein Dialog zur Konfiguration eines Service-Bindings mit einem Jameica-Server.
 * Der Dialog wird benoetigt, wenn Jameica im Netzwerk-Betrieb als Client
 * laeuft. Dann kann der User hier den Hostnamen und Port des Servers angeben.
 * @author willuhn
 */
public class ServiceBindingDialog extends AbstractDialog
{

  private Class pluginclass     = null;
  private String serviceName    = null;
  
  private String value          = null;

  /**
   * ct.
   * @param pluginclass Klasse des Plugins.
   * @param servicename Der Service-Name.
   * @param position
   */
  public ServiceBindingDialog(Class pluginclass, String servicename, int position)
  {
    super(position);
    this.pluginclass = pluginclass;
    this.serviceName = servicename;
    this.setTitle(i18n.tr("Verbindung zu Jameica-Server"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("Eigenschaften"));
    String h = Application.getServiceFactory().getLookupHost(this.pluginclass,this.serviceName);
    int p    = Application.getServiceFactory().getLookupPort(this.pluginclass,this.serviceName);

    if (p <= 0)
      p = Config.RMI_DEFAULT_PORT;

    group.addText(i18n.tr("Bitte geben Sie den Hostnamen/IP-Adresse " +
      "des Jameica-Servers an, auf dem dieser Service verfügbar ist"),true);

    final TextInput hostname = new TextInput(h);
    final IntegerInput port  = new IntegerInput(p);
    
    group.addLabelPair(i18n.tr("Hostname des Jameica-Servers"),hostname);
    group.addLabelPair(i18n.tr("TCP-Port des Jameica-Servers"),port);

    ButtonArea a = group.createButtonArea(2);
    a.addButton(i18n.tr("Übernehmen"), new Action()
    {
      /**
       * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
       */
      public void handleAction(Object context) throws ApplicationException
      {
        String h = (String) hostname.getValue();
        if (h != null && h.length() > 0)
        {
          Integer p = (Integer) port.getValue();
          int pd = Config.RMI_DEFAULT_PORT;
          if (p != null && p.intValue() > 0)
            pd = p.intValue();
          
          value = h + ":" + pd; 
        }
        close();
      }
    },null,true);
    a.addButton(i18n.tr("Abbrechen"), new Action()
    {
      /**
       * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
       */
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    });
  }

  /**
   * Liefert einen String des Formats "hostname:port" zurueck
   * oder <code>null</code>, wenn nichts sinnvolles eingegeben wurde.
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return this.value;
  }

}

/*********************************************************************
 * $Log: ServiceBindingDialog.java,v $
 * Revision 1.3  2007/06/21 11:03:01  willuhn
 * @C ServiceSettings in ServiceFactory verschoben
 * @N Aenderungen an Service-Bindings sofort uebernehmen
 * @C Moeglichkeit, Service-Bindings wieder entfernen zu koennen
 *
 * Revision 1.2  2005/06/16 13:29:20  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/06/15 17:51:31  web0
 * @N Code zum Konfigurieren der Service-Bindings
 *
 **********************************************************************/