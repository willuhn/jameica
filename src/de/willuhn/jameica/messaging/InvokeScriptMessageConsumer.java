/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.util.ArrayList;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;

import de.willuhn.jameica.services.ScriptingService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Message-Consumer, der QueryMessages mit Script-Funktionen ausfuehrt.
 */
public class InvokeScriptMessageConsumer implements MessageConsumer
{
  private static final I18N i18n = Application.getI18n();
  private static final String PREFIX_FX = "function.";
  
  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    // Wird manuell ueber den Service registriert.
    return false;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{QueryMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    if (!(message instanceof QueryMessage))
      return;
    
    QueryMessage msg = (QueryMessage) message;

    ScriptingService service = Application.getBootLoader().getBootable(ScriptingService.class);
    ScriptEngine engine = service.getEngine();
    if (engine == null)
    {
      msg.setData(new ApplicationException(i18n.tr("Die installierte Java-Version enthält keine JavaScript-Unterstützung (RhinoScript)")));
      return;
    }
    
    String event = msg.getName();
    if (event == null || event.length() == 0)
    {
      Logger.warn("no event name given for script execution");
      msg.setData(new ApplicationException(i18n.tr("Kein Event-Name angegeben")));
      return;
    }
    
    List<String> functions = null;

    // Wenn der Event-Name mit "function." beginnt, rufen wir
    // diese Funktion direkt auf - ohne Mapping ueber die Events
    if (event.startsWith(PREFIX_FX) && event.length() > PREFIX_FX.length())
    {
      functions = new ArrayList<>();
      functions.add(event.substring(PREFIX_FX.length()));
    }
    else
    {
      // Checken, ob Funktionen fuer das Event registriert sind.
      functions = service.getFunction(event);
    }
    
    if (functions.isEmpty())
    {
      Logger.debug("no script functions registered for event " + event);
      msg.setData(new ApplicationException(i18n.tr("Kein passendes Script gefunden")));
      return;
    }
    
    List<Object> returns = new ArrayList<>();
    Invocable i = (Invocable) engine;
    Object params = msg.getData();

    for (String method:functions)
    {
      try
      {
        Object value = null;
        
        if (params != null && params.getClass().isArray())
          value = i.invokeFunction(method,(Object[]) params);
        else
          value = i.invokeFunction(method,params);
        
        if (value != null)
          returns.add(value);
      }
      catch (NoSuchMethodException nme)
      {
        Logger.debug("script method not found: " + method);
        returns.add(new ApplicationException(i18n.tr("Funktion nicht in Script gefunden")));
      }
      catch (Exception e)
      {
        Logger.error("error while executing script method " + method + ", adding exception to return list",e);
        returns.add(e);
      }
    }
    
    // Rueckgabewert der Funktionen
    if (returns.isEmpty())
      msg.setData(null); // Rueckgabewert leeren
    else if (returns.size() == 1)
      msg.setData(returns.get(0)); // nur ein Wert, dann nehmen wir den direkt
    else
      msg.setData(returns); // mehrere Werte, dann liefern wir alle zurueck
  }

}
