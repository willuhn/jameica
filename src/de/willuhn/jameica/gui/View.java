/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/View.java,v $
 * $Revision: 1.2 $
 * $Date: 2003/10/29 00:41:26 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 ****************************************************************************/
package de.willuhn.jameica;



import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.util.SWTFactory;
import de.willuhn.jameica.util.Style;

public class View
{
	private Composite view;
	private Composite content;

  public View()
	{
		setLayout();
		setLogoPanel();
		cleanContent();
	}
	
  private void setLayout()
	{
		view = new Composite(GUI.shell, SWT.NONE);
		view.setBackground(new Color(GUI.display, 255, 255, 255));
		view.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		view.setLayout(layout);
	}
	
  private void setLogoPanel()
	{
		Label logoPanel = new Label(view, SWT.NONE);
		logoPanel.setImage(Style.getImage("logo.jpg"));
		logoPanel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
	}
	
  public void cleanContent()
	{
		if (content != null)
			content.dispose();

		content = new Composite(view, SWT.BORDER);
		content.setBackground(SWTFactory.getDefaultBackgroundColor());
		GridLayout l = new GridLayout();
		l.marginHeight = 0;
		l.marginWidth = 0;
		content.setLayout(l);
		content.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

  public void refreshContent()
	{
		view.layout();
	}

  public Composite getContent()
	{
		return content;
	}
}



/***************************************************************************
 * $Log: View.java,v $
 * Revision 1.2  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 ***************************************************************************/