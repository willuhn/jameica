/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/Manifest.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/10/08 00:19:19 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.plugin;

import java.io.InputStream;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

/**
 * Enthaelt die Manifest-Informationen des Plugins aus plugin.xml.
 */
public class Manifest
{

  /**
   * ct.
   * @param is der Stream mit der plugin.xml.
   */
  public Manifest(InputStream is) throws Exception
  {
		if (is == null)
			return;
		IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
		parser.setReader(new StdXMLReader(is));
		IXMLElement xml = (IXMLElement) parser.parse();

//		name 				= xml.getFirstChildNamed("name").getContent();
  }

	public double getVersion()
	{
		return 1.0;
	}

	public String getName()
	{
		return null;
	}
	
	public String getDescription()
	{
		return null;
	}
	
	public String getURL()
	{
		return null;
	}
	
	public String getLicense()
	{
		return null;
	}
}


/**********************************************************************
 * $Log: Manifest.java,v $
 * Revision 1.1  2004/10/08 00:19:19  willuhn
 * *** empty log message ***
 *
 **********************************************************************/