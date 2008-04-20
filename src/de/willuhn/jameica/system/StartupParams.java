/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/StartupParams.java,v $
 * $Revision: 1.7 $
 * $Date: 2008/04/20 23:30:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.system;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import de.willuhn.logging.Logger;

/**
 * Enthaelt die Start-Parameter von Jameica.
 */
public class StartupParams
{
	/**
	 * Konstante fuer "Anwendung laeuft standalone".
	 */
	public final static int MODE_STANDALONE		= 0;
	/**
	 * Konstante fuer "Anwendung laeuft im Server-Mode ohne GUI".
	 */
	public final static int MODE_SERVER				= 1;

	/**
	 * Konstante fuer "Anwendung laeuft im reinen Client-Mode".
	 */
	public final static int MODE_CLIENT = 2;

	private Options options = null;

	private String workDir  = null;
	private String password = null;
	private int mode 				= MODE_STANDALONE;

  private boolean noninteractive = false;
  
  private String[] params   = null;

  /**
	 * ct.
   * @param args Die Kommandozeilen-Parameter.
   */
  public StartupParams(String[] args)
	{
  	this.params       = args;
		Option server 		= new Option("d","server",false,"Startet die Anwendung im Server-Mode ohne Benutzeroberfläche.");
		Option client 		= new Option("c","client",false,"Startet die Anwendung im Client-Mode mit Benutzeroberfläche");
		Option standalone = new Option("s","standalone",false,"Startet die Anwendung im Standalone-Mode mit Benutzeroberfläche (Default)");

		OptionGroup mode = new OptionGroup();
		mode.setRequired(false);
		mode.addOption(server);
		mode.addOption(client);
		mode.addOption(standalone);
		
		options = new Options();

		options.addOptionGroup(mode);

		options.addOption("h","help",false,"Gibt diesen Hilfe-Text aus");
		options.addOption("f","file",true,"Optionale Angabe des Datenverzeichnisses (Workdir)");
		options.addOption("p","password",true,"Optionale Angabe des Master-Passworts");

    options.addOption("n","noninteractive",false,"Koppelt Jameica im Server-Mode von der Konsole ab. " +      "Es findet keine Benutzer-Interaktion mehr statt. Die Option wird nur ausgewertet, wenn Jameica " +      "im Server-Mode läuft.");


		PosixParser parser = new PosixParser();
		try
		{
			CommandLine line = parser.parse(options,args);

			if (line.hasOption("h"))
				printHelp();
			
			if (line.hasOption("d"))
			{
				Logger.info("starting in SERVER mode");
				this.mode = MODE_SERVER;
			}
			else if (line.hasOption("c")) 
			{
				Logger.info("starting in CLIENT mode");
				this.mode = MODE_CLIENT;
			} 
			else
			{
				Logger.info("starting in STANDALONE mode");
			}

      if (this.mode == MODE_SERVER && line.hasOption("n"))
      {
        Logger.info("activating noninteractive mode");
        this.noninteractive = true;
      }

			if (line.hasOption("f")) this.workDir  = line.getOptionValue("f");
			if (line.hasOption("p")) this.password = line.getOptionValue("p");

			Logger.info("workdir: " + this.workDir);
			if (this.password != null)
				Logger.info("master password given via commandline");

		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			printHelp();
		}

	}

	/**
   * Gibt einen Hilfe-Text aus und beendet Jameica.
   */
  private void printHelp()
	{
		new HelpFormatter().printHelp("java de.willuhn.jameica.Main", options);
		System.exit(1);
	}

	/**
	 * Liefert das ggf als Kommandozeilen-Parameter angegebene Master-Passwort.
   * @return Master-Passwort oder <code>null</code>.
   */
  public String getPassword()
	{
		return password;
	}
	
	/**
	 * Liefert den Start-Modus von Jameica.
	 * Zur Codierung sie Konstanten <code>MODE_*</code>.
   * @return Start-Modus.
   */
  public int getMode()
	{
		return mode;
	}
	
	/**
	 * Liefert das Arbeitsverzeichnis der Jameica-Instanz.
   * @return Arbeitsverzeichnis.
   */
  public String getWorkDir()
	{
		return workDir;
	}
  
  /**
   * Liefert true, wenn Jameica im nichtinteraktiven Server-Mode
   * laeuft und damit keine direkte Interaktion mit dem Benutzer ueber
   * die Konsole moeglich ist.
   * @return liefert true, wenn sich die Anwendung im nicht-interaktiven Mode befindet.
   */
  public boolean isNonInteractiveMode()
  {
    return this.noninteractive;
  }
  
  /**
   * Liefert die Kommandozeilen-Parameter.
   * @return Liste der ungeparsten Kommandozeilen-Parameter.
   */
  public String[] getParams()
  {
  	return this.params;
  }
}


/**********************************************************************
 * $Log: StartupParams.java,v $
 * Revision 1.7  2008/04/20 23:30:58  willuhn
 * @N MACOS Kommandozeilen-Parameter ausgeben
 *
 * Revision 1.6  2006/02/06 14:20:13  web0
 * @R removed parameter "ask"
 *
 * Revision 1.4  2005/06/15 16:10:57  web0
 * @B javadoc fixes
 *
 * Revision 1.3  2005/06/10 13:04:41  web0
 * @N non-interactive Mode
 * @N automatisches Abspeichern eingehender Zertifikate im nicht-interaktiven Mode
 *
 * Revision 1.2  2005/02/04 00:34:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/02/02 16:16:38  willuhn
 * @N Kommandozeilen-Parser auf jakarta-commons umgestellt
 *
 **********************************************************************/