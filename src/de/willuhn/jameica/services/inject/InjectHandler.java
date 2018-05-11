/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.services.inject;


/**
 * Interface, welches beim BeanService registriert werden kann,
 * um beim Instanziieren von Beans zusaetzliche Injections vorzunehmen.
 */
public interface InjectHandler
{
  /**
   * Wird aufgerufen, wenn eine Instanz vom Beanservice erzeugt wird.
   * @param o die Instanz des erzeugten Objektes.
   * @throws Exception wenn das Injection fehlschlug.
   */
  public void inject(Object o) throws Exception;
}


