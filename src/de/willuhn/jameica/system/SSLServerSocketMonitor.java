/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Attic/SSLServerSocketMonitor.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/01/14 00:48:56 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.system;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

/**
 */
public class SSLServerSocketMonitor extends ServerSocket
{
  private SSLServerSocket server;

  /**
   * Construct a new SSLServerSocket wrapper over the real SSLServerSocket
   * instance.
   * <p>
   * The ServerSocket object did not provide a public no-arg constructor
   * prior to JDK 1.4. As a result, this constructor will invoke the
   * <code>super(0)</code> to create a server socket and will then
   * close it right away. This may interfere with a security policy
   * restricting <code>listen</code> permission to certain ports.
   * <p>
   * This class may be recompiled without the first two lines if it
   * is only going to be used in JDK &gt; 1.4
   */
  public SSLServerSocketMonitor(SSLServerSocket server) throws IOException
  {
    super();

    this.server = server;
  }

  /**
   *
   * Binds the <code>ServerSocket</code> to a specific address
   * (IP address and port number).
   * <p>
   * If the address is <code>null</code>, then the system will pick up
   * an ephemeral port and a valid local address to bind the socket.
   * <p>
   * @param	endpoint	The IP address & port number to bind to.
   * @throws	IOException if the bind operation fails, or if the socket
   *			   is already bound.
   * @throws	SecurityException	if a <code>SecurityManager</code> is present and
   * its <code>checkListen</code> method doesn't allow the operation.
   * @throws  IllegalArgumentException if endpoint is a
   *          SocketAddress subclass not supported by this socket
   * @since 1.4
   */
  public void bind(SocketAddress endpoint) throws IOException
  {
    server.bind(endpoint);
  }

  /**
   *
   * Binds the <code>ServerSocket</code> to a specific address
   * (IP address and port number).
   * <p>
   * If the address is <code>null</code>, then the system will pick up
   * an ephemeral port and a valid local address to bind the socket.
   * <P>
   * The <code>backlog</code> argument must be a positive
   * value greater than 0. If the value passed if equal or less
   * than 0, then the default value will be assumed.
   * @param	endpoint	The IP address & port number to bind to.
   * @param	backlog		The listen backlog length.
   * @throws	IOException if the bind operation fails, or if the socket
   *			   is already bound.
   * @throws	SecurityException	if a <code>SecurityManager</code> is present and
   * its <code>checkListen</code> method doesn't allow the operation.
   * @throws  IllegalArgumentException if endpoint is a
   *          SocketAddress subclass not supported by this socket
   * @since 1.4
   */
  public void bind(SocketAddress endpoint, int backlog) throws IOException
  {
    server.bind(endpoint, backlog);
  }

  /**
   * Returns the local address of this server socket.
   *
   * @return  the address to which this socket is connected,
   *          or <code>null</code> if the socket is not yet connected.
   */
  public InetAddress getInetAddress()
  {
    return (server.getInetAddress());
  }

  /**
   * Returns the port on which this socket is listening.
   *
   * @return  the port number to which this socket is listening.
   */
  public int getLocalPort()
  {
    return (server.getLocalPort());
  }

  /**
   * Returns the address of the endpoint this socket is bound to, or
   * <code>null</code> if it is not bound yet.
   *
   * @return a <code>SocketAddress</code> representing the local endpoint of this
   *	       socket, or <code>null</code> if it is not bound yet.
   * @see #getInetAddress()
   * @see #getLocalPort()
   * @see #bind(SocketAddress)
   * @since 1.4
   */

  public SocketAddress getLocalSocketAddress()
  {
    return (server.getLocalSocketAddress());
  }

  /**
   * Listens for a connection to be made to this socket and accepts 
   * it. The method blocks until a connection is made. 
   *
   * <p>A new Socket <code>s</code> is created and, if there 
   * is a security manager, 
   * the security manager's <code>checkAccept</code> method is called
   * with <code>s.getInetAddress().getHostAddress()</code> and
   * <code>s.getPort()</code>
   * as its arguments to ensure the operation is allowed. 
   * This could result in a SecurityException.
   * 
   * @exception  IOException  if an I/O error occurs when waiting for a
   *               connection.
   * @exception  SecurityException  if a security manager exists and its  
   *             <code>checkListen</code> method doesn't allow the operation.
   * @return the new Socket
   * @see SecurityManager#checkAccept
   */
  public Socket accept() throws IOException
  {
    SSLSocket socket = (SSLSocket) server.accept();
    return (new SSLSocketMonitor(socket));
  }

  /**
   * Closes this socket. 
   *
   * @exception  IOException  if an I/O error occurs when closing the socket.
   */
  public void close() throws IOException
  {
    server.close();
  }

  /**
   * Returns the unique {@link java.nio.channels.ServerSocketChannel} object
   * associated with this socket, if any.
   *
   * <p> A server socket will have a channel if, and only if, the channel
   * itself was created via the {@link
   * java.nio.channels.ServerSocketChannel#open ServerSocketChannel.open}
   * method.
   *
   * @return  the server-socket channel associated with this socket,
   *          or <tt>null</tt> if this socket was not created
   *          for a channel
   *
   * @since 1.4
   * @spec JSR-51
   */
  public ServerSocketChannel getChannel()
  {
    return (server.getChannel());
  }

  /**
   * Returns the binding state of the ServerSocket.
   *
   * @return true if the ServerSocket succesfuly bound to an address
   * @since 1.4
   */
  public boolean isBound()
  {
    return (server.isBound());
  }

  /**
   * Returns the closed state of the ServerSocket.
   *
   * @return true if the socket has been closed
   * @since 1.4
   */
  public boolean isClosed()
  {
    return (server.isClosed());
  }

  /**
   * Enable/disable SO_TIMEOUT with the specified timeout, in
   * milliseconds.  With this option set to a non-zero timeout,
   * a call to accept() for this ServerSocket
   * will block for only this amount of time.  If the timeout expires,
   * a <B>java.io.InterruptedIOException</B> is raised, though the
   * ServerSocket is still valid.  The option <B>must</B> be enabled
   * prior to entering the blocking operation to have effect.  The 
   * timeout must be > 0.
   * A timeout of zero is interpreted as an infinite timeout.  
   * @param timeout the specified timeout, in milliseconds
   * @exception SocketException if there is an error in 
   * the underlying protocol, such as a TCP error. 
   * @since   JDK1.1
   * @see #getSoTimeout()
   */
  public void setSoTimeout(int timeout) throws SocketException
  {
    server.setSoTimeout(timeout);
  }

  /** 
   * Retrive setting for SO_TIMEOUT.  0 returns implies that the
   * option is disabled (i.e., timeout of infinity).
   * @return the SO_TIMEOUT value
   * @exception IOException if an I/O error occurs
   * @since   JDK1.1
   * @see #setSoTimeout(int)
   */
  public int getSoTimeout() throws IOException
  {
    return (server.getSoTimeout());
  }

  /**
   * Enable/disable SO_REUSEADDR.
   * <P>
   * The SO_REUSEADDR socket option  affects the <code>bind()</code>
   * operation and serves 2 main different purposes:
   * <UL>
   * <LI>Allows a listening server to start and <code>bind</code> its port
   * even if previously established connections exists that use this port
   * as their local port. This typically happens when a ServerSocket is
   * started and listen on that port, accepts an incoming connection and
   * spawn a child process to handle the socket. Then the listening server
   * terminates but the child continues to service the client. At that, to
   * be able to restart the listening ServerSocket, the SO_REUSEADDR option
   * needs to be turned on.</LI>
   * <LI>Allows multiple instances of the same server to be started on
   * the same port, as long as each instance binds a different local
   * IP address.</LI>
   * </UL>
   *
   * @param on     whether or not to have socket ReuseAddr turned on.
   * @exception SocketException if there is an error
   * in the underlying protocol, such as a TCP error. 
   * @since 1.4
   * @see #getReuseAddress()
   */
  public void setReuseAddress(boolean on) throws SocketException
  {
    server.setReuseAddress(on);
  }

  /**
   * Tests if SO_REUSEADDR is enabled.
   *
   * @return a <code>boolean</code> indicating whether or not SO_REUSEADDR is enabled.
   * @exception SocketException if there is an error
   * in the underlying protocol, such as a TCP error. 
   * @since   1.4
   * @see #setReuseAddress(boolean)
   */
  public boolean getReuseAddress() throws SocketException
  {
    return (server.getReuseAddress());
  }

  /**
   * Returns the implementation address and implementation port of 
   * this socket as a <code>String</code>.
   *
   * @return  a string representation of this socket.
   */
  public String toString()
  {
    return (server.toString());
  }

  /**
   * Sets a default proposed value for the SO_RCVBUF option for sockets 
   * accepted from this <tt>ServerSocket</tt>. The value actually set 
   * in the accepted socket must be determined by calling 
   * {@link Socket#getReceiveBufferSize()} after the socket 
   * is returned by {@link #accept()}. 
   * <p>
   * The value of SO_RCVBUF is used both to set the size of the internal
   * socket receive buffer, and to set the size of the TCP receive window
   * that is advertized to the remote peer.
   * <p>
   * It is possible to change the value subsequently, by calling 
   * {@link Socket#setReceiveBufferSize(int)}. However, if the application 
   * wishes to allow a receive window larger than 64K bytes, as defined by RFC1323
   * then the proposed value must be set in the ServerSocket <B>before</B> 
   * it is bound to a local address. This implies, that the ServerSocket must be 
   * created with the no-argument constructor, then setReceiveBufferSize() must 
   * be called and lastly the ServerSocket is bound to an address by calling bind(). 
   * <p>
   * Failure to do this will not cause an error, and the buffer size may be set to the
   * requested value but the TCP receive window in sockets accepted from 
   * this ServerSocket will be no larger than 64K bytes.
   *
   * @exception SocketException if there is an error
   * in the underlying protocol, such as a TCP error. 
   *
   * @param size the size to which to set the receive buffer
   * size. This value must be greater than 0.
   *
   * @exception IllegalArgumentException if the 
   * value is 0 or is negative.
   *
   * @since 1.4
   * @see #getReceiveBufferSize
   */
  public synchronized void setReceiveBufferSize(int size)
    throws SocketException
  {
    server.setReceiveBufferSize(size);
  }

  /**
   * Gets the value of the SO_RCVBUF option for this <tt>ServerSocket</tt>, 
   * that is the proposed buffer size that will be used for Sockets accepted
   * from this <tt>ServerSocket</tt>.
   * 
   * <p>Note, the value actually set in the accepted socket is determined by
   * calling {@link Socket#getReceiveBufferSize()}.
   * @return the value of the SO_RCVBUF option for this <tt>Socket</tt>.
   * @exception SocketException if there is an error
   * in the underlying protocol, such as a TCP error. 
   * @see #setReceiveBufferSize(int)
   * @since 1.4
   */
  public synchronized int getReceiveBufferSize() throws SocketException
  {
    return (server.getReceiveBufferSize());
  }

}

/**********************************************************************
 * $Log: SSLServerSocketMonitor.java,v $
 * Revision 1.1  2005/01/14 00:48:56  willuhn
 * *** empty log message ***
 *
 **********************************************************************/