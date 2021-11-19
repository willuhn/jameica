/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.plugin;

import java.util.ArrayList;
import java.util.StringTokenizer;

import net.n3.nanoxml.IXMLElement;

/**
 * Ein Service-Deskriptor beschreibt Name und Klasse
 * eines Services, welcher von einem Plugin zur Verfuegung
 * gestellt werden kann.
 * Diese finden sich in der plugin.xml eines jeden Plugins.
 *
 * <p>Bsp.:
 * <pre>{@code
 * <services>
 *   <service name="service 1" class="de.willuhn.jameica.plugin.Service1"
 *            autostart="true" depends="service 2" />
 *   <service name="service 2" class="de.willuhn.jameica.plugin.Service2"
 *            autostart="false" />
 * </services>
 * }</pre>
 */
public class ServiceDescriptor
{

	private IXMLElement root 	= null;
	private String name 			= null;
	private String className 	= null;
	private String[] depends	= null;

  /**
   * ct.
   * @param root
   */
  public ServiceDescriptor(IXMLElement root)
  {
  	this.root = root;
  }

  /**
   * Liefert den Namen des Services.
   * @return Name des Services.
   */
  public String getName()
  {
  	if (name != null)
  		return name;
    name = root.getAttribute("name",null);
    return name;
  }

  /**
   * Liefert den Namen der Java-Klasse des Services.
   *
   * <p><b>HINWEIS:</b>Hier muss keine Implementierung angegeben werden. Ist ein
   * Interface eingetragen, sucht Jameica selbstaendig die
   * zugehoerige Implementierung.
   *
   * @return Name der Klasse.
   */
  public String getClassname()
  {
  	if (className != null)
  		return className;
		className = root.getAttribute("class",null);
		return className;
  }

  /**
   * Legt fest, ob der Service beim Starten von Jameica automatisch
   * gestartet werden soll.
   * @return {@code true}, wenn er automatisch gestartet werden soll.
   */
  public boolean autostart()
  {
  	String s = root.getAttribute("autostart","true");
  	return "true".equalsIgnoreCase(s);
  }

  /**
   * Liefert eine Liste von Service-Namen, von denen dieser Service
   * abhaengig ist.
   * @return Liste von Services, die vorher instanziiert werden muessen oder
   *         {@code null}, wenn der Service von keinen anderen Services abhaengig ist.
   */
  public String[] depends()
  {
  	if (depends != null)
  		return depends;

		String s = root.getAttribute("depends",null);
		if (s == null || s.length() == 0)
			return new String[0];

    s = s.replaceAll("\n|\r","");
		StringTokenizer st = new StringTokenizer(s,",");
    ArrayList l = new ArrayList();
		while (st.hasMoreTokens())
		{
		  s = st.nextToken();
      if (s == null || s.length() == 0)
        continue;
			l.add(s.trim());
		}
    this.depends = (String[]) l.toArray(new String[l.size()]);
		return depends;
  }

  /**
   * Legt fest, ob der Service im Netzwerk freigegeben werden soll.
   * @return {@code true}, wenn er freigegeben werden soll.
   */
  public boolean share()
  {
    String s = root.getAttribute("share","true");
    return "true".equalsIgnoreCase(s);
  }

}


/**********************************************************************
 * $Log: ServiceDescriptor.java,v $
 * Revision 1.3  2012/04/04 20:43:37  willuhn
 * @R Ueberfluessige Interface+XMLImpl entfernt
 * @N MessageDescriptor
 *
 * Revision 1.4  2007/04/16 13:19:33  willuhn
 * @C return empty list instead of null
 *
 * Revision 1.3  2005/08/18 23:57:20  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/07/14 22:58:36  web0
 * *** empty log message ***
 *
 * Revision 1.1  2004/12/21 01:08:01  willuhn
 * @N new service configuration system in plugin.xml with auostart and dependencies
 *
 **********************************************************************/