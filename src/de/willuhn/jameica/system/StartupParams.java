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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import de.willuhn.io.IOUtil;
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

	private String workDir         = null;
	private String username        = null;
	private String password        = null;
	private int mode 				       = MODE_STANDALONE;

  private boolean noninteractive = false;
  private boolean ignoreLockfile = false;
  
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
		options.addOption("f","file",true,"Optionale Angabe des Benutzer-Verzeichnisses (Workdir)");
    options.addOption("u","username",true,"Optionale Angabe des Benutzernamens");
		options.addOption("p","password",true,"Optionale Angabe des Master-Passworts");
    options.addOption("w","passwordfile",true,"Optionale Angabe des Master-Passworts, welches sich in der angegebenen Datei befindet");
    options.addOption("o","force-password",false,"Angabe des Master-Passworts via Kommandozeile ignorieren (für MacOS nötig)");

    options.addOption("n","noninteractive",false,"Koppelt Jameica im Server-Mode von der Konsole ab. " +
      "Es findet keine Benutzer-Interaktion mehr statt. Die Option wird nur ausgewertet, wenn Jameica " +
      "im Server-Mode läuft.");

    options.addOption("l","ignore-lock",false,"Ignoriert eine ggf. vorhandene Lock-Datei");

    CommandLineParser parser = new DefaultParser();
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
      
      if (line.hasOption("l"))
      {
        Logger.info("ignoring lock file");
        this.ignoreLockfile = true;
      }

			if (line.hasOption("f"))
			  this.workDir  = line.getOptionValue("f");
      Logger.info("workdir: " + this.workDir);
      
			if (line.hasOption("p") && !line.hasOption("o"))
			{
			  this.password = line.getOptionValue("p");
        Logger.info("master password given via commandline");
      }
			
      if (line.hasOption("u"))
      {
        this.username = line.getOptionValue("u");
        Logger.info("username given via commandline");
      }

			if (line.hasOption("w"))
			{
			  String file = line.getOptionValue("w");
			  File f = new File(file);
			  if (!f.exists() || !f.canRead() || !f.isFile())
			  {
			    Logger.warn("option \"w\" given, but file " + file + " not readable, ignoring");
			  }
			  else
			  {
			    BufferedReader r = null;
			    try
			    {
			      r = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			      this.password = r.readLine();
			      Logger.info("master password given via file " + file);
			    }
			    finally
			    {
			      IOUtil.close(r);
			    }
			  }
			}
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
		new HelpFormatter().printHelp("jameica.sh|jameica-*.exe|jameica-*.sh <Optionen>","\nOptionen", options,"");
		System.exit(1);
	}

	/**
	 * Liefert das ggf als Kommandozeilen-Parameter angegebene Master-Passwort.
   * @return Master-Passwort oder {@code null}.
   */
  public String getPassword()
	{
		return password;
	}
  
  /**
   * Liefert den ggf als Kommandozeilen-Parameter angegebenen Usernamen.
   * @return der Username oder {@code null}.
   */
  public String getUsername()
  {
    return username;
  }
	
	/**
	 * Liefert den Start-Modus von Jameica.
   * @return Start-Modus.
   * @see #MODE_STANDALONE
   * @see #MODE_SERVER
   * @see #MODE_CLIENT
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
   * Prüfe, ob Jameica im nichtinteraktiven Server-Mode
   * laeuft und damit keine direkte Interaktion mit dem Benutzer ueber
   * die Konsole moeglich ist.
   * @return liefert {@code true}, wenn sich die Anwendung im nicht-interaktiven Mode befindet.
   */
  public boolean isNonInteractiveMode()
  {
    return this.noninteractive;
  }
  
  /**
   * Prüfe, ob eine ggf vorhandene Lock-Datei ignoriert werden soll.
   * @return {@code true}, wenn die Lock-Datei ignoriert werden soll.
   */
  public boolean isIgnoreLockfile()
  {
    return this.ignoreLockfile;
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
 * Revision 1.17  2012/02/23 22:03:36  willuhn
 * @N wenn der User im Workdir-Chooser die Option "kuenftig nicht mehr anzeigen" aktiviert hat, kann er die Einstellung jetzt unter Datei->Einstellungen wieder rueckgaengig machen. Es gab sonst keine komfortable Moeglichkeit, den Dialog wieder "hervorzuholen"
 *
 * Revision 1.16  2012/02/21 15:03:32  willuhn
 * @N Parameter "-a" abgeschafft. Jetzt wird per Default immer nach dem Workdir gefragt - das vereinfacht die ganze Sache etwas.
 *
 * Revision 1.15  2011-04-26 12:09:18  willuhn
 * @B Potentielle Bugs gemaess Code-Checker
 *
 * Revision 1.14  2011-03-04 18:13:38  willuhn
 * @N Erster Code fuer einen Workdir-Chooser
 *
 * Revision 1.13  2010-11-22 11:36:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2010-11-22 11:32:04  willuhn
 * @N Beim Start von Jameica kann nun neben dem Masterpasswort optional auch ein Benutzername abgefragt werden. Dieser kann auch ueber den neuen Kommandozeilen-Parameter "-u" uebergeben werden.
 *
 * Revision 1.11  2009/08/17 09:29:22  willuhn
 * @N Neuer Startup-Parameter "-l", mit dem die Lock-Datei von Jameica ignoriert werden kann. Habe ich eigentlich nur wegen Eclipse eingebaut. Denn dort werden Shutdown-Hooks nicht ausgefuehrt, wenn man die Anwendung im Debugger laufen laesst und auf "Terminate" klickt. Da das Debuggen maechtig nervig ist, wenn man im Server-Mode immer erst auf "Y" druecken muss, um den Start trotz Lockfile fortzusetzen, kann man mit dem Parameter "-l" das Pruefen auf die Lock-Datei einfach ignorieren
 *
 * Revision 1.10  2009/04/14 09:25:53  willuhn
 * @N Neuer Parameter "-w <file>", mit dem das Masterpasswort auch ueber eine Datei uebergeben werden kann
 **********************************************************************/