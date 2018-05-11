/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.util;

import java.io.InputStream;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

/**
 * Kleine Hilfsklasse, die Informationen aus info.xml Files ausliest.
 * Diese Dateien werden im Lib-Verzeichnis verwendet, um zu den
 * verwendeten Komponenten Name, Beschreibung und insbesondere die
 * Lizenz zu hinterlegen.
 * <p>
 * Die Dateien haben folgenden beispielhaften Aufbau:
 * &lt;info&gt;<br>
 *   &lt;name&gt;de.willuhn.datasource&lt;/name&gt;<br>
 *   &lt;description&gt;an object relational mapper with RMI support&lt;/description&gt;<br>
 *   &lt;url&gt;http://www.willuhn.de/projects/datasource&lt;/url&gt;<br>
 *   &lt;license&gt;LGPL - http://www.gnu.org/copyleft/lesser.html&lt;/license&gt;<br>
 * &lt;/info&gt;
* </p>
 */
public class InfoReader {

	private String name;
	private String description;
	private String url;
	private String license;

  /**
   * ct.
   * @param is InputStream mit dem XML-Code.
   * @throws Exception Wenn beim Lesen des XML-Codes ein Fehler auftrat.
   */
  public InfoReader(InputStream is) throws Exception
  {
  	if (is == null)
  		return;
		IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
		parser.setReader(new StdXMLReader(is));
		IXMLElement xml = (IXMLElement) parser.parse();

		name 				= xml.getFirstChildNamed("name").getContent();
		description = xml.getFirstChildNamed("description").getContent();
		url					= xml.getFirstChildNamed("url").getContent();
		license			= xml.getFirstChildNamed("license").getContent();
  }

	/**
	 * Liefert den Namen der Komponente.
   * @return Name.
   */
  public String getName()
	{
		return name;
	}
	
	/**
	 * Liefert die Beschreibung der Komponente.
	 * @return Beschreibung.
	 */
	public String getDescription()
	{
		return description;
	}
	
	/**
	 * Liefert die URL der Komponente.
	 * @return URL.
	 */
	public String getUrl()
	{
		return url;
	}
	
	/**
	 * Liefert die Lizenz der Komponente.
	 * @return Lizenz.
	 */
	public String getLicense()
	{
		return license;
	}
}


/**********************************************************************
 * $Log: InfoReader.java,v $
 * Revision 1.1  2004/07/21 20:09:59  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.1  2004/04/26 22:42:18  willuhn
 * @N added InfoReader
 *
 **********************************************************************/