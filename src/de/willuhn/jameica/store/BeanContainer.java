/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * Generischer Container, in dem Beans abgelegt und mittels BeanStore
 * gespeichert werden koennen.
 * @param <T> der konkrete Typ der Beans.
 */
@XmlRootElement
public class BeanContainer<T> implements Serializable
{
  // Niemals aendern, damit das auch serialisierbar bleibt, wenn Properties geaendert werden.
  static final long serialVersionUID = -3158681266553988208L;

  private List<T> beans = new ArrayList<T>();
  
  @XmlAttribute(name="type",required=true)
  Class<T> type;
  
  @XmlAttribute(name="encrypted",required=true)
  boolean encrypted = false;
  
  /**
   * ct.
   * Fuer die Bean-Spec, damit JAXB die Klasse instanziieren kann.
   */
  @SuppressWarnings("unused")
  private BeanContainer()
  {
  }
  
  /**
   * ct.
   * @param type der konkrete Typ der Beans.
   * @param encrypted true, wenn der Beanstore verschluesselt speichern soll.
   */
  BeanContainer(Class<T> type, boolean encrypted)
  {
    this.type      = type;
    this.encrypted = encrypted;
  }
  
  /**
   * Liefert die Beans.
   * @return die Liste der Beans.
   */
  @XmlElementWrapper(name="beans")
  @XmlElement(name="bean")
  public List<T> getBeans()
  {
    return this.beans;
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "[bean-container: " + this.type.getSimpleName() + ", encrypted: " + this.encrypted + "]";
  }
}


