/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/controller/Attic/ServiceSettingsControl.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/03/03 22:27:11 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.controller;

import java.rmi.RemoteException;

import de.willuhn.datasource.common.LocalServiceData;
import de.willuhn.datasource.rmi.ServiceData;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.Jameica;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.views.AbstractView;

/**
 * Controller fuer den Dialog "Einstellungen eines Services".
 */
public class ServiceSettingsControl extends AbstractControl {

	private ServiceData service = null;
  /**
   * ct.
   * @param view die zustaendige View.
   */
  public ServiceSettingsControl(AbstractView view) {
    super(view);

		try {
			String name = (String) getCurrentObject();

			this.service = (ServiceData) Application.getConfig().getLocalServiceData(name);
			if (this.service == null) 
				this.service = (ServiceData )Application.getConfig().getRemoteServiceData(name);
		}
		catch (RemoteException e)
		{
			Application.getLog().error("unable to read service data",e);
			GUI.setActionText(PluginLoader.getPlugin(Jameica.class).getResources().getI18N().tr("Fehler beim Lesen des Services."));
		}
  }

	/**
	 * Liefert den Service, der konfiguriert werden soll.
   * @return den zu konfigurierenden Service.
   */
  public ServiceData getServiceData()
	{
		return service;
	}

	/**
	 * Prueft, ob es sich um einen lokalen Service handelt.
   * @return true, wenn es ein lokaler Service ist. Andernfalls muss es ein Remoteservice sein.
   */
  public boolean isLocalService()
	{
		return (service instanceof LocalServiceData);
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleDelete()
   */
  public void handleDelete() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
   */
  public void handleCancel() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public void handleStore() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
   */
  public void handleOpen(Object o) {
  }

}


/**********************************************************************
 * $Log: ServiceSettingsControl.java,v $
 * Revision 1.3  2004/03/03 22:27:11  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.2  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.1  2004/02/11 00:10:42  willuhn
 * *** empty log message ***
 *
 **********************************************************************/