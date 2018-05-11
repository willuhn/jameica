/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Diese Klasse kapselt Dialog-Teile in einer Gruppe.
 * Damit ist es einfacher, standardisierte Dialoge zu malen.
 * Man erstellt pro Dialog einfach ein oder mehrere solcher
 * Gruppen und tut dort seine Eingabefelder rein.
 * @author willuhn
 */
public class LabelGroup extends Container
{

  private Group group = null;

  /**
   * ct.
   * Erzeugt eine neue Labelgroup, welche so hoch ist, wie ihr Inhalt.
   * @param parent Das Composite, in dem die Group gemalt werden soll.
   * @param name Name der Group.
   */
  public LabelGroup(Composite parent, String name)
  {
  	this(parent,name,false);
  }
  
	/**
	 * ct.
	 * Erzeugt eine neue Labelgroup, jedoch kann fastgelegt werden, ob
	 * sie sich ueber die volle Hoehe der View erstreckt oder nur
	 * auf ihre tatsaechliche Hoehe.
   * @param parent Das Composite, in dem die Group gemalt werden soll.
   * @param name Name der Group.
   * @param fullSize true, wenn es voelle Hoehe haben soll.
   */
  public LabelGroup(Composite parent, String name, boolean fullSize)
	{
    super(fullSize);
		group = new Group(parent, SWT.NONE);
		
		if (name != null)
  		group.setText(name);
		group.setFont(Font.H2.getSWTFont());
		GridLayout layout = new GridLayout(2, false);
		group.setLayout(layout);

    GridData grid = new GridData(fullSize ? GridData.FILL_BOTH : (GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
		group.setLayoutData(grid);
	}


  /**
   * @see de.willuhn.jameica.gui.util.Container#getComposite()
   */
  public Composite getComposite()
  {
    return this.group;
  }
}

/*********************************************************************
 * $Log: LabelGroup.java,v $
 * Revision 1.20  2011/05/03 10:13:11  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.19  2010-08-19 11:59:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2008-02-22 16:20:40  willuhn
 * @N Mehrspalten-Layouts
 *
 * Revision 1.17  2007/07/17 15:57:42  willuhn
 * @B bug 435
 *
 * Revision 1.16  2005/06/10 22:13:09  web0
 * @N new TabGroup
 * @N extended Settings
 *
 * Revision 1.15  2005/06/10 10:12:26  web0
 * @N Zertifikats-Dialog ergonomischer gestaltet
 * @C TrustManager prueft nun zuerst im Java-eigenen Keystore
 *
 * Revision 1.14  2004/11/12 18:23:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/10/25 17:59:15  willuhn
 * @N aenderbare Tabellen
 *
 * Revision 1.12  2004/09/13 23:27:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/08/18 23:14:19  willuhn
 * @D Javadoc
 *
 * Revision 1.10  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.9  2004/07/09 00:12:47  willuhn
 * @C Redesign
 *
 * Revision 1.8  2004/06/30 20:58:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/06/14 22:05:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/05/27 21:35:02  willuhn
 * @N PGP signing in ant script
 * @N MD5 checksum in ant script
 *
 * Revision 1.5  2004/05/25 23:23:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.3  2004/04/27 00:04:44  willuhn
 * @D javadoc
 *
 * Revision 1.2  2004/04/14 22:16:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/04/12 19:15:59  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.13  2004/04/05 23:29:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/04/01 00:23:24  willuhn
 * @N FontInput
 * @N ColorInput
 * @C improved ClassLoader
 * @N Tabs in Settings
 *
 * Revision 1.11  2004/03/30 22:08:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/03/24 00:46:03  willuhn
 * @C refactoring
 *
 * Revision 1.9  2004/03/19 01:44:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/03/11 08:56:56  willuhn
 * @C some refactoring
 *
 * Revision 1.7  2004/03/04 00:26:34  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.5  2004/02/27 01:09:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/02/18 01:40:30  willuhn
 * @N new white style
 *
 * Revision 1.3  2004/02/17 00:53:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/01/29 00:07:24  willuhn
 * @N Text widget
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.15  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.13  2004/01/06 01:27:30  willuhn
 * @N table order
 *
 * Revision 1.12  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.11  2003/12/28 22:58:27  willuhn
 * @N synchronize mode
 *
 * Revision 1.10  2003/12/26 21:43:30  willuhn
 * @N customers changable
 *
 * Revision 1.9  2003/12/19 13:36:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2003/12/19 01:43:27  willuhn
 * @N added Tree
 *
 * Revision 1.7  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.6  2003/12/10 00:47:12  willuhn
 * @N SearchDialog done
 * @N FatalErrorView
 *
 * Revision 1.5  2003/12/01 20:28:57  willuhn
 * @B filter in DBIteratorImpl
 * @N InputFelder generalisiert
 *
 * Revision 1.4  2003/11/24 14:21:53  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/11/23 19:26:27  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 * Revision 1.1  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 **********************************************************************/