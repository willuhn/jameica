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

import java.net.IDN;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Parser and compatibility bridge for the explicitly configured legacy
 * archive server. No discovery data is used here.
 */
public final class ArchiveServerEndpoint
{
  /** Canonical lookup name used by existing Hibiscus releases. */
  public static final String LOOKUP_NAME = "tcp:de.willuhn.jameica.messaging.Plugin.connector.tcp";

  private ArchiveServerEndpoint()
  {
  }

  /**
   * Checks whether a lookup name requests any plaintext TCP service.
   * Whitespace and case variants are classified as TCP so they fail closed.
   * @param name lookup name.
   * @return true for a TCP lookup name.
   */
  public static boolean isTcpLookup(String name)
  {
    if (name == null)
      return false;

    int offset = 0;
    char[] scheme = new char[]{'t','c','p'};
    for (char expected:scheme)
    {
      while (offset < name.length() && isSpacingOrControl(name.charAt(offset)))
        ++offset;
      if (offset >= name.length() || Character.toLowerCase(name.charAt(offset)) != expected)
        return false;
      ++offset;
    }
    while (offset < name.length() && isSpacingOrControl(name.charAt(offset)))
      ++offset;
    return offset < name.length() && name.charAt(offset) == ':';
  }

  /**
   * Resolves the one legacy compatibility lookup without multicast.
   * @param name lookup name; it must exactly match {@link #LOOKUP_NAME}.
   * @param configured explicitly configured endpoint.
   * @return normalized host and port or {@code null} when disabled.
   */
  public static String resolveLookup(String name, String configured)
  {
    if (!LOOKUP_NAME.equals(name))
      return null;

    InetSocketAddress endpoint = parse(configured);
    return endpoint != null ? format(endpoint) : null;
  }

  /**
   * Parses an explicit endpoint without resolving hostnames.
   * IPv6 literals must use brackets.
   * @param value endpoint in hostname:port or [IPv6]:port form.
   * @return unresolved endpoint or {@code null} for an empty value.
   */
  public static InetSocketAddress parse(String value)
  {
    if (value == null || value.trim().length() == 0)
      return null;

    String endpoint = value.trim();
    String host;
    String portText;
    if (endpoint.startsWith("["))
    {
      int closing = endpoint.indexOf(']');
      if (closing <= 1 || endpoint.indexOf('[',1) >= 0 || endpoint.indexOf(']',closing + 1) >= 0)
        throw invalid();

      host = endpoint.substring(1,closing);
      String suffix = endpoint.substring(closing + 1).trim();
      if (!suffix.startsWith(":") || suffix.indexOf(':',1) >= 0)
        throw invalid();
      portText = suffix.substring(1).trim();
      validateIpv6(host);
    }
    else
    {
      int colon = endpoint.indexOf(':');
      if (colon <= 0 || colon != endpoint.lastIndexOf(':'))
        throw invalid();
      host = endpoint.substring(0,colon).trim();
      portText = endpoint.substring(colon + 1).trim();
      host = validateHostname(host);
    }

    if (!portText.matches("[0-9]{1,5}"))
      throw invalid();
    int port = Integer.parseInt(portText);
    if (port < 1 || port > 65535)
      throw invalid();

    return InetSocketAddress.createUnresolved(host,port);
  }

  /**
   * Formats an endpoint without losing IPv6 brackets.
   * @param endpoint parsed endpoint.
   * @return normalized endpoint string.
   */
  public static String format(InetSocketAddress endpoint)
  {
    String host = endpoint.getHostString();
    return (host.indexOf(':') >= 0 ? "[" + host + "]" : host) + ":" + endpoint.getPort();
  }

  private static String validateHostname(String host)
  {
    if (host.length() == 0 || host.length() > 253 || containsWhitespaceOrControl(host) || host.indexOf('/') >= 0 || host.indexOf('\\') >= 0 || host.indexOf('@') >= 0 || host.indexOf('[') >= 0 || host.indexOf(']') >= 0)
      throw invalid();

    if (host.matches("[0-9.]+"))
    {
      String[] parts = host.split("\\.",-1);
      if (parts.length != 4)
        throw invalid();
      for (String part:parts)
      {
        if (!part.matches("[0-9]{1,3}") || Integer.parseInt(part) > 255)
          throw invalid();
      }
      return host;
    }

    final String ascii;
    try
    {
      ascii = IDN.toASCII(host,IDN.USE_STD3_ASCII_RULES);
    }
    catch (IllegalArgumentException e)
    {
      throw invalid();
    }
    String checked = ascii.endsWith(".") ? ascii.substring(0,ascii.length() - 1) : ascii;
    if (checked.length() == 0 || checked.length() > 253)
      throw invalid();
    for (String label:checked.split("\\.",-1))
    {
      if (label.length() == 0 || label.length() > 63 || !label.matches("[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?"))
        throw invalid();
    }
    return ascii;
  }

  private static void validateIpv6(String host)
  {
    if (host.indexOf(':') < 0 || containsWhitespaceOrControl(host) || host.indexOf('%') >= 0)
      throw invalid();
    try
    {
      InetAddress.getByName(host);
    }
    catch (Exception e)
    {
      throw invalid();
    }
  }

  private static boolean containsWhitespaceOrControl(String value)
  {
    for (int i=0;i<value.length();++i)
    {
      char c = value.charAt(i);
      if (Character.isWhitespace(c) || Character.isISOControl(c))
        return true;
    }
    return false;
  }

  private static boolean isSpacingOrControl(char value)
  {
    return Character.isWhitespace(value) || Character.isSpaceChar(value) || Character.isISOControl(value) || Character.getType(value) == Character.FORMAT;
  }

  private static IllegalArgumentException invalid()
  {
    return new IllegalArgumentException("archive server must use a valid hostname:port or [IPv6]:port endpoint");
  }
}
