/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Damit koennen Methoden in Klassen annotiert werden, die
 * via Messaging aufgerufen werden koennen.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Receive
{
  /**
   * Optionaler Name der Queue, auf die die Methode "lauschen" soll.
   * @return Name der Queue.
   */
  String queue() default "";
}


