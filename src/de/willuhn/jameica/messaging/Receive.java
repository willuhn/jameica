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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Damit koennen Methoden in Klassen annotiert werden, die
 * via Messaging aufgerufen werden koennen.
 * 
 * Beans, die diese Annotation verwenden, sollten zusaetzlich
 * die Lifecycle-Annotation mit dem Type "CONTEXT" benutzen,
 * um sicherzustellen, dass immer die selbe Instanz der Bean
 * verwendet wird, um die Nachrichten zu erhalten.
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


