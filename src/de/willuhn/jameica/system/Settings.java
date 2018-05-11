/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.system;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * Diese Klasse stellt eine Art Storage-Provider fuer Einstellungen
 * zur Verfuegung. Sprich: Man muss sich um die Speicherung seiner
 * Einstellungen nicht kuemmern, sondern holt sie sich hier. 
 * @author willuhn
 */
public final class Settings extends de.willuhn.util.Settings
{

  /**
   * Erzeugt eine neue Instanz der Settings, die exclusiv
   * nur fuer diese Klasse gelten. Existieren bereits Settings
   * fuer die Klasse, werden sie gleich geladen.
   * @param clazz Klasse, fuer die diese Settings gelten.
   */
  public Settings(Class clazz)
  {
		this(clazz,true);
  }

  /**
   * Erzeugt eine neue Instanz der Settings, die exclusiv
   * nur fuer diese Klasse gelten. Existieren bereits Settings
   * fuer die Klasse, werden sie gleich geladen.
   * @param clazz Klasse, fuer die diese Settings gelten.
   * @param overridable legt fest, ob die Settings durch den User ueberschrieben werden koennen. Default: true.
   */
  public Settings(Class clazz, boolean overridable)
  {
    super("cfg",overridable ? Application.getConfig().getConfigDir() : null,clazz);
  }

	/**
	 * Liefert den Wert des Attributes als Farbe.
	 * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
	 * @param name Name des Attribut.
	 * @param defaultValue DefaultWert, wenn das Attribut nicht existiert.
	 * @return der Wert des Attributs.
	 */
	public RGB getRGB(String name, RGB defaultValue)
	{
		if (defaultValue == null) defaultValue = new RGB(0,0,0);

		return new RGB(
			getInt(name + ".r",defaultValue.red),
			getInt(name + ".g",defaultValue.green),
			getInt(name + ".b",defaultValue.blue)
		);
	}

	/**
	 * Liefert den Wert des Attributes als Font.
	 * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
	 * @param name Name des Attribut.
	 * @param defaultValue DefaultWert, wenn das Attribut nicht existiert.
	 * @return der Wert des Attributs.
	 */
	public FontData getFontData(String name, FontData defaultValue)
	{
		if (defaultValue == null) defaultValue = new FontData();

		return new FontData(
			getString(name + ".name",defaultValue.getName()),
			getInt(name + ".height",defaultValue.getHeight()),
			getInt(name + ".style",defaultValue.getStyle())
		);
	}

	/**
	 * Speichert einen Farb-Wert.
	 * @param name Name des Attributs.
	 * @param value Wert des Attributs.
	 */
	public void setAttribute(String name, RGB value)
	{
		if (value == null)
			return;
		setAttribute(name + ".r",value.red);
		setAttribute(name + ".g",value.green);
		setAttribute(name + ".b",value.blue);
	}

	/**
	 * Speichert einen Font-Wert.
	 * @param name Name des Attributs.
	 * @param value Wert des Attributs.
	 */
	public void setAttribute(String name, FontData value)
	{
		if (value == null)
			return;
		setAttribute(name + ".name",value.getName());
		setAttribute(name + ".height",value.getHeight());
		setAttribute(name + ".style",value.getStyle());
	}
}

/*********************************************************************
 * $Log: Settings.java,v $
 * Revision 1.9  2012/03/20 23:28:01  willuhn
 * @N BUGZILLA 1209
 *
 * Revision 1.8  2007/06/21 09:03:31  willuhn
 * @N System-Presets
 *
 * Revision 1.7  2005/01/14 00:48:57  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2005/01/13 19:31:37  willuhn
 * @C SSLFactory geaendert
 * @N Settings auf property-Format umgestellt
 *
 * Revision 1.5  2004/12/15 01:43:04  willuhn
 * @N Properties koennen via Settings.getFoo() direkt beim Lesen gespeichert werden
 *
 * Revision 1.4  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/08/11 00:39:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/07/21 23:54:53  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.1  2004/07/21 20:08:45  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.12  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/05/27 23:12:58  willuhn
 * @B NoSuchFieldError in Settings
 * @C s/java/javaw.exe in build/*.bat
 *
 * Revision 1.10  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.9  2004/04/12 19:15:59  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.8  2004/03/30 22:08:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/03/16 23:59:40  willuhn
 * @N 2 new Input fields
 *
 * Revision 1.6  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.5  2004/02/21 19:49:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/01/03 18:08:05  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.3  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.2  2003/12/27 21:23:33  willuhn
 * @N object serialization
 *
 * Revision 1.1  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 **********************************************************************/