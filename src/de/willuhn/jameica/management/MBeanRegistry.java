/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/management/Attic/MBeanRegistry.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/12/05 13:35:30 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.management;

import java.lang.management.ManagementFactory;
import java.security.PrivilegedAction;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import de.willuhn.jameica.security.JameicaSecurityManager;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Registry, welche die MBeans registriert.
 */
public class MBeanRegistry
{
  /**
   * System-Property "com.sun.management.jmxremote".
   */
  public final static String SYSTEM_PROPERTY = "com.sun.management.jmxremote";

  /**
   * Initialisiert und registriert die MBeans.
   */
  public static void init()
  {
    if (System.getProperty(SYSTEM_PROPERTY) == null)
    {
      Logger.warn("system property " + SYSTEM_PROPERTY + " not set, skip MBean-Registry");
      return;
    }

    Logger.info("init MBeanRegistry");
    try
    {
      Class[] classes = Application.getClassLoader().getClassFinder().findImplementors(JameicaMBean.class);
      if (classes == null || classes.length == 0)
      {
        Logger.info("no mbeans found");
        return;
      }
      
      final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

      for (int i=0;i<classes.length;++i)
      {
        try
        {
          final JameicaMBean bean = (JameicaMBean) classes[i].newInstance();
          // String name = bean.getClass().getPackage().getName() + ":type=" + bean.getType();
          final String name = "de.willuhn.jameica:type=" + bean.getType();
          Logger.info("  register " + name);
          
          JameicaSecurityManager sm = (JameicaSecurityManager) System.getSecurityManager();
          sm.doPrivileged(new PrivilegedAction() {
            /**
             * @see java.security.PrivilegedAction#run()
             */
            public Object run()
            {
              try
              {
                return server.registerMBean(bean,new ObjectName(name));
              }
              catch (Exception re)
              {
                throw new RuntimeException(re);
              }
            }
          
          });
        }
        catch (Exception e)
        {
          Logger.error("unable to register mbean " + classes[i],e);
        }
      }
    }
    catch (ClassNotFoundException e)
    {
      Logger.info("no mbeans found");
    }
  }
}


/*********************************************************************
 * $Log: MBeanRegistry.java,v $
 * Revision 1.1  2007/12/05 13:35:30  willuhn
 * @N Unterstuetzung fuer JMX
 *
 **********************************************************************/