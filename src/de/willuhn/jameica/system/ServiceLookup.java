/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Attic/ServiceLookup.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/07/21 23:54:54 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.system;

/**
 * Diese Klasse dient der Konfiguration der Lookup-Adressen fuer Services.
 * Jameica stellt einen Config-Dialog zur Verfuegung, ueber den definiert
 * werden kann, welcher Service ueber welchen Jameica-Server bezogen
 * werden kann. Genau diese Einstellungen werden hier gespeichert.
 */
public class ServiceLookup
{

	private static Settings settings = new Settings(ServiceLookup.class);

	/**
	 * Liefert eine Liste aller Service-Namen, fuer die bereits
	 * Lookup-Adressen definiert wurden.
   * @return Liste der vorhandenen Lookup-Addressen.
   */
  public static String[] getServiceNames()
	{
		return settings.getAttributes();
	}

	/**
	 * Speichert Host und Port fuer genannten Service.
   * @param serviceName Name des Service.
   * @param host Host (IP oder Hostname).
   * @param port TCP-Port.
   */
  public static void setLookup(String serviceName, String host, int port)
	{
		settings.setAttribute(serviceName,host + ":" + port);
	}

	/**
	 * Liefert den Host, auf dem nach diesem Service gesucht werden soll.
   * @param serviceName Name des gesuchten Service.
   * @return Hostname, auf dem sich der Service befindet oder <code>null</code> wenn nicht definiert.
   */
  public static String getLookupHost(String serviceName)
	{
		String value = settings.getString(serviceName,null);
		if (value == null)
			return null;
		return value.substring(0,value.lastIndexOf(":"));
	}

	/**
	 * Liefert den TCP-Port, auf dem nach diesem Service gesucht werden soll.
	 * @param serviceName Name des gesuchten Service.
	 * @return TCP-Port, auf dem sich der Service befindet oder <code>-1</code> wenn nicht definiert.
	 */
	public static int getLookupPort(String serviceName)
	{
		String value = settings.getString(serviceName,null);
		if (value == null)
			return -1;
		try {
			return Integer.parseInt(value.substring(value.lastIndexOf(":")+1));
		}
		catch (NumberFormatException e)
		{
			return -1;
		}
	}
}


/**********************************************************************
 * $Log: ServiceLookup.java,v $
 * Revision 1.1  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 **********************************************************************/