/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/

package de.willuhn.jameica.util;

import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.NonValidator;
import net.n3.nanoxml.StdXMLBuilder;
import net.n3.nanoxml.StdXMLParser;
import net.n3.nanoxml.XMLParseException;

/**
 * NanoXML parser that rejects document type declarations before NanoXML can
 * resolve external DTDs or entities.
 */
public final class SafeXMLParser extends StdXMLParser
{
  /**
   * Creates a parser with NanoXML's standard tree builder and validator.
   */
  public SafeXMLParser()
  {
    super();
    setBuilder(new StdXMLBuilder());
    setValidator(new NonValidator());
  }

  /**
   * Rejects the complete DTD feature.
   * @throws Exception always.
   */
  @Override
  protected void processDocType() throws Exception
  {
    IXMLReader reader = getReader();
    String systemID = reader == null ? null : reader.getSystemID();
    int line = reader == null ? 0 : reader.getLineNr();
    throw new XMLParseException(systemID,line,"DOCTYPE declarations are not allowed");
  }
}
