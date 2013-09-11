/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
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


