/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Attic/SSLSocketMonitor.java,v $
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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLSocket;

public class SSLSocketMonitor extends Socket
{

  private SSLSocket socket;

  public SSLSocketMonitor(SSLSocket socket)
  {
    super();
    this.socket = socket;
  }

  /**
   * Connects this socket to the server.
   *
   * @param endpoint the <code>SocketAddress</code>
   * @throws  IOException if an error occurs during the connection
   * @throws  IllegalArgumentException if endpoint is null
   * @throws  java.nio.channels.IllegalBlockingModeException
   *          if this socket has an associated channel,
   *          and the channel is in non-blocking mode
   * @throws  IllegalArgumentException if endpoint is a
   *          SocketAddress subclass not supported by this socket
   * @since 1.4
   * @spec JSR-51
   */
  public void connect(SocketAddress endpoint) throws IOException
  {
    socket.connect(endpoint);
  }

  /**
   * Connects this socket to the server with a specified timeout value.
   * A timeout of zero is interpreted as an infinite timeout. The connection
   * will then block until established or an error occurs.
   *
   * @param endpoint the <code>SocketAddress</code>
   * @param timeout  the timeout value to be used in milliseconds.
   * @throws  IOException if an error occurs during the connection
   * @throws  SocketTimeoutException if timeout expires before connecting
   * @throws  IllegalArgumentException if endpoint is null
   * @throws  java.nio.channels.IllegalBlockingModeException
   *          if this socket has an associated channel,
   *          and the channel is in non-blocking mode
   * @throws  IllegalArgumentException if endpoint is a
   *          SocketAddress subclass not supported by this socket
   * @since 1.4
   * @spec JSR-51
   */
  public void connect(SocketAddress endpoint, int timeout) throws IOException
  {
    socket.connect(endpoint, timeout);
  }

  /**
   * Binds the socket to a local address.
   * <P>
   * If the address is <code>null</code>, then the system will pick up
   * an ephemeral port and a valid local address to bind the socket.
   *
   * @param bindpoint the <code>SocketAddress</code> to bind to
   * @throws  IOException if the bind operation fails, or if the socket
   *         is already bound.
   * @throws  IllegalArgumentException if bindpoint is a
   *          SocketAddress subclass not supported by this socket
   *
   * @since 1.4
   * @see #isBound
   */
  public void bind(SocketAddress bindpoint) throws IOException
  {
    socket.bind(bindpoint);
  }

  /**
   * Returns the address to which the socket is connected.
   *
   * @return  the remote IP address to which this socket is connected,
   *    or <code>null</code> if the socket is not connected.
   */
  public InetAddress getInetAddress()
  {
    return (socket.getInetAddress());
  }

  /**
   * Gets the local address to which the socket is bound.
   *
   * @return the local address to which the socket is bound or
   *         <code>InetAddress.anyLocalAddress()</code>
   *         if the socket is not bound yet.
   * @since   JDK1.1
   */
  public InetAddress getLocalAddress()
  {
    return (socket.getLocalAddress());
  }

  /**
   * Returns the remote port to which this socket is connected.
   *
   * @return  the remote port number to which this socket is connected, or
   *          0 if the socket is not connected yet.
   */
  public int getPort()
  {
    return (socket.getPort());
  }

  /**
   * Returns the local port to which this socket is bound.
   *
   * @return  the local port number to which this socket is bound or -1
   *          if the socket is not bound yet.
   */
  public int getLocalPort()
  {
    return (socket.getLocalPort());
  }

  /**
   * Returns the address of the endpoint this socket is connected to, or
   * <code>null</null> if it is unconnected.
   * @return a <code>SocketAddress</code> reprensenting the remote endpoint of this
   *         socket, or <code>null</code> if it is not connected yet.
   * @see #getInetAddress()
   * @see #getPort()
   * @see #connect(SocketAddress, int)
   * @see #connect(SocketAddress)
   * @since 1.4
   */
  public SocketAddress getRemoteSocketAddress()
  {
    return (socket.getRemoteSocketAddress());
  }

  /**
   * Returns the address of the endpoint this socket is bound to, or
   * <code>null</code> if it is not bound yet.
   *
   * @return a <code>SocketAddress</code> representing the local endpoint of this
   *         socket, or <code>null</code> if it is not bound yet.
   * @see #getLocalAddress()
   * @see #getLocalPort()
   * @see #bind(SocketAddress)
   * @since 1.4
   */
  public SocketAddress getLocalSocketAddress()
  {
    return (socket.getLocalSocketAddress());

  }

  /**
   * Returns the unique {@link java.nio.channels.SocketChannel SocketChannel}
   * object associated with this socket, if any.
   *
   * <p> A socket will have a channel if, and only if, the channel itself was
   * created via the {@link java.nio.channels.SocketChannel#open
   * SocketChannel.open } method.
   *
   * @return  the socket channel associated with this socket,
   *          or <tt>null</tt> if this socket was not created
   *          for a channel
   *
   * @since 1.4
   * @spec JSR-51
   */
  public SocketChannel getChannel()
  {
    return (socket.getChannel());
  }

  /**
   * Returns an input stream for this socket.
   *
   * <p> If this socket has an associated channel then the resulting input
   * stream delegates all of its operations to the channel.  If the channel
   * is in non-blocking mode then the input stream's <tt>read</tt> operations
   * will throw an {@link java.nio.channels.IllegalBlockingModeException}.
   *
   * @return     an input stream for reading bytes from this socket.
   * @exception  IOException  if an I/O error occurs when creating the
   *               input stream or if the socket is not connected.
   * @revised 1.4
   * @spec JSR-51
   */
  public InputStream getInputStream() throws IOException
  {
    InputStream istream = socket.getInputStream();

    return (new InputStreamMonitor(istream, socket));
  }

  /**
   * Returns an output stream for this socket.
   *
   * <p> If this socket has an associated channel then the resulting output
   * stream delegates all of its operations to the channel.  If the channel
   * is in non-blocking mode then the output stream's <tt>write</tt>
   * operations will throw an {@link
   * java.nio.channels.IllegalBlockingModeException}.
   *
   * @return     an output stream for writing bytes to this socket.
   * @exception  IOException  if an I/O error occurs when creating the
   *               output stream or if the socket is not connected.
   * @revised 1.4
   * @spec JSR-51
   */
  public OutputStream getOutputStream() throws IOException
  {
    return (socket.getOutputStream());
  }

  /**
   * Enable/disable TCP_NODELAY (disable/enable Nagle's algorithm).
   *
   * @param on <code>true</code> to enable TCP_NODELAY,
   * <code>false</code> to disable.
   *
   * @exception SocketException if there is an error
   * in the underlying protocol, such as a TCP error.
   *
   * @since   JDK1.1
   *
   * @see #getTcpNoDelay()
   */
  public void setTcpNoDelay(boolean on) throws SocketException
  {
    socket.setTcpNoDelay(on);
  }

  /**
   * Tests if TCP_NODELAY is enabled.
   *
   * @return a <code>boolean</code> indicating whether or not TCP_NODELAY is enabled.
   * @exception SocketException if there is an error
   * in the underlying protocol, such as a TCP error.
   * @since   JDK1.1
   * @see #setTcpNoDelay(boolean)
   */
  public boolean getTcpNoDelay() throws SocketException
  {
    return (socket.getTcpNoDelay());
  }

  /**
   * Enable/disable SO_LINGER with the specified linger time in seconds.
   * The maximum timeout value is platform specific.
   *
   * The setting only affects socket close.
   *
   * @param on     whether or not to linger on.
   * @param linger how long to linger for, if on is true.
   * @exception SocketException if there is an error
   * in the underlying protocol, such as a TCP error.
   * @exception IllegalArgumentException if the linger value is negative.
   * @since JDK1.1
   * @see #getSoLinger()
   */
  public void setSoLinger(boolean on, int linger) throws SocketException
  {
    socket.setSoLinger(on, linger);
  }

  /**
   * Returns setting for SO_LINGER. -1 returns implies that the
   * option is disabled.
   *
   * The setting only affects socket close.
   *
   * @return the setting for SO_LINGER.
   * @exception SocketException if there is an error
   * in the underlying protocol, such as a TCP error.
   * @since   JDK1.1
   * @see #setSoLinger(boolean, int)
   */
  public int getSoLinger() throws SocketException
  {
    return (socket.getSoLinger());
  }

  /**
   * Send one byte of urgent data on the socket. The byte to be sent is the lowest eight
   * bits of the data parameter. The urgent byte is
   * sent after any preceding writes to the socket OutputStream
   * and before any future writes to the OutputStream.
   * @param data The byte of data to send
   * @exception IOException if there is an error
   *  sending the data.
   * @since 1.4
   */
  public void sendUrgentData(int data) throws IOException
  {
    socket.sendUrgentData(data);
  }

  /**
   * Enable/disable OOBINLINE (receipt of TCP urgent data)
   *
   * By default, this option is disabled and TCP urgent data received on a
   * socket is silently discarded. If the user wishes to receive urgent data, then
   * this option must be enabled. When enabled, urgent data is received
   * inline with normal data.
   * <p>
   * Note, only limited support is provided for handling incoming urgent
   * data. In particular, no notification of incoming urgent data is provided
   * and there is no capability to distinguish between normal data and urgent
   * data unless provided by a higher level protocol.
   *
   * @param on <code>true</code> to enable OOBINLINE,
   * <code>false</code> to disable.
   *
   * @exception SocketException if there is an error
   * in the underlying protocol, such as a TCP error.
   *
   * @since   1.4
   *
   * @see #getOOBInline()
   */
  public void setOOBInline(boolean on) throws SocketException
  {
    socket.setOOBInline(on);
  }

  /**
   * Tests if OOBINLINE is enabled.
   *
   * @return a <code>boolean</code> indicating whether or not OOBINLINE is enabled.
   * @exception SocketException if there is an error
   * in the underlying protocol, such as a TCP error.
   * @since   1.4
   * @see #setOOBInline(boolean)
   */
  public boolean getOOBInline() throws SocketException
  {
    return (socket.getOOBInline());
  }

  /**
   *  Enable/disable SO_TIMEOUT with the specified timeout, in
   *  milliseconds.  With this option set to a non-zero timeout,
   *  a read() call on the InputStream associated with this Socket
   *  will block for only this amount of time.  If the timeout expires,
   *  a <B>java.net.SocketTimeoutException</B> is raised, though the
   *  Socket is still valid. The option <B>must</B> be enabled
   *  prior to entering the blocking operation to have effect. The
   *  timeout must be > 0.
   *  A timeout of zero is interpreted as an infinite timeout.
   * @param timeout the specified timeout, in milliseconds.
   * @exception SocketException if there is an error
   * in the underlying protocol, such as a TCP error.
   * @since   JDK 1.1
   * @see #getSoTimeout()
   */
  public void setSoTimeout(int timeout) throws SocketException
  {
    socket.setSoTimeout(timeout);
  }

  /**
   * Returns setting for SO_TIMEOUT.  0 returns implies that the
   * option is disabled (i.e., timeout of infinity).
   * @return the setting for SO_TIMEOUT
   * @exception SocketException if there is an error
   * in the underlying protocol, such as a TCP error.
   * @since   JDK1.1
   * @see #setSoTimeout(int)
   */
  public int getSoTimeout() throws SocketException
  {
    return (socket.getSoTimeout());
  }

  /**
   * Sets the SO_SNDBUF option to the specified value for this
   * <tt>Socket</tt>. The SO_SNDBUF option is used by the platform's
   * networking code as a hint for the size to set
   * the underlying network I/O buffers.
   *
   * <p>Because SO_SNDBUF is a hint, applications that want to
   * verify what size the buffers were set to should call
   * {@link #getSendBufferSize()}.
   *
   * @exception SocketException if there is an error
   * in the underlying protocol, such as a TCP error.
   *
   * @param size the size to which to set the send buffer
   * size. This value must be greater than 0.
   *
   * @exception IllegalArgumentException if the
   * value is 0 or is negative.
   *
   * @see #getSendBufferSize()
   * @since 1.2
   */
  public void setSendBufferSize(int size) throws SocketException
  {
    socket.setSendBufferSize(size);
  }

  /**
   * Get value of the SO_SNDBUF option for this <tt>Socket</tt>,
   * that is the buffer size used by the platform
   * for output on this <tt>Socket</tt>.
   * @return the value of the SO_SNDBUF option for this <tt>Socket</tt>.
   *
   * @exception SocketException if there is an error
   * in the underlying protocol, such as a TCP error.
   *
   * @see #setSendBufferSize(int)
   * @since 1.2
   */
  public int getSendBufferSize() throws SocketException
  {
    return (socket.getSendBufferSize());
  }

  /**
   * Sets the SO_RCVBUF option to the specified value for this
   * <tt>Socket</tt>. The SO_RCVBUF option is used by the platform's
   * networking code as a hint for the size to set
   * the underlying network I/O buffers.
   *
   * <p>Increasing the receive buffer size can increase the performance of
   * network I/O for high-volume connection, while decreasing it can
   * help reduce the backlog of incoming data.
   *
   * <p>Because SO_RCVBUF is a hint, applications that want to
   * verify what size the buffers were set to should call
   * {@link #getReceiveBufferSize()}.
   *
   * <p>The value of SO_RCVBUF is also used to set the TCP receive window
   * that is advertized to the remote peer. Generally, the window size
   * can be modified at any time when a socket is connected. However, if
   * a receive window larger than 64K is required then this must be requested
   * <B>before</B> the socket is connected to the remote peer. There are two
   * cases to be aware of:<p>
   * <ol>
   * <li>For sockets accepted from a ServerSocket, this must be done by calling
   * {@link ServerSocket#setReceiveBufferSize(int)} before the ServerSocket
   * is bound to a local address.<p></li>
   * <li>For client sockets, setReceiveBufferSize() must be called before
   * connecting the socket to its remote peer.<p></li></ol>
   * @param size the size to which to set the receive buffer
   * size. This value must be greater than 0.
   *
   * @exception IllegalArgumentException if the value is 0 or is
   * negative.
   *
   * @exception SocketException if there is an error
   * in the underlying protocol, such as a TCP error.
   *
   * @see #getReceiveBufferSize()
   * @see ServerSocket#setReceiveBufferSize(int)
   * @since 1.2
   */
  public void setReceiveBufferSize(int size) throws SocketException
  {
    socket.setReceiveBufferSize(size);
  }

  /**
   * Gets the value of the SO_RCVBUF option for this <tt>Socket</tt>,
   * that is the buffer size used by the platform for
   * input on this <tt>Socket</tt>.
   *
   * @return the value of the SO_RCVBUF option for this <tt>Socket</tt>.
   * @exception SocketException if there is an error
   * in the underlying protocol, such as a TCP error.
   * @see #setReceiveBufferSize(int)
   * @since 1.2
   */
  public int getReceiveBufferSize() throws SocketException
  {
    return (socket.getReceiveBufferSize());
  }

  /**
   * Enable/disable SO_KEEPALIVE.
   *
   * @param on     whether or not to have socket keep alive turned on.
   * @exception SocketException if there is an error
   * in the underlying protocol, such as a TCP error.
   * @since 1.3
   * @see #getKeepAlive()
   */
  public void setKeepAlive(boolean on) throws SocketException
  {
    socket.setKeepAlive(on);
  }

  /**
   * Tests if SO_KEEPALIVE is enabled.
   *
   * @return a <code>boolean</code> indicating whether or not SO_KEEPALIVE is enabled.
   * @exception SocketException if there is an error
   * in the underlying protocol, such as a TCP error.
   * @since   1.3
   * @see #setKeepAlive(boolean)
   */
  public boolean getKeepAlive() throws SocketException
  {
    return (socket.getKeepAlive());
  }

  /**
   * Sets traffic class or type-of-service octet in the IP
   * header for packets sent from this Socket.
   * As the underlying network implementation may ignore this
   * value applications should consider it a hint.
   *
   * <P> The tc <B>must</B> be in the range <code> 0 <= tc <=
   * 255</code> or an IllegalArgumentException will be thrown.
   * <p>Notes:
   * <p> for Internet Protocol v4 the value consists of an octet
   * with precedence and TOS fields as detailed in RFC 1349. The
   * TOS field is bitset created by bitwise-or'ing values such
   * the following :-
   * <p>
   * <UL>
   * <LI><CODE>IPTOS_LOWCOST (0x02)</CODE></LI>
   * <LI><CODE>IPTOS_RELIABILITY (0x04)</CODE></LI>
   * <LI><CODE>IPTOS_THROUGHPUT (0x08)</CODE></LI>
   * <LI><CODE>IPTOS_LOWDELAY (0x10)</CODE></LI>
   * </UL>
   * The last low order bit is always ignored as this
   * corresponds to the MBZ (must be zero) bit.
   * <p>
   * Setting bits in the precedence field may result in a
   * SocketException indicating that the operation is not
   * permitted.
   * <p>
   * for Internet Protocol v6 <code>tc</code> is the value that
   * would be placed into the sin6_flowinfo field of the IP header.
   *
   * @param tc        an <code>int</code> value for the bitset.
   * @throws SocketException if there is an error setting the
   * traffic class or type-of-service
   * @since 1.4
   * @see #getTrafficClass
   */
  public void setTrafficClass(int tc) throws SocketException
  {
    socket.setTrafficClass(tc);
  }

  /**
   * Gets traffic class or type-of-service in the IP header
   * for packets sent from this Socket
   * <p>
   * As the underlying network implementation may ignore the
   * traffic class or type-of-service set using {@link #setTrafficClass()}
   * this method may return a different value than was previously
   * set using the {@link #setTrafficClass()} method on this Socket.
   *
   * @return the traffic class or type-of-service already set
   * @throws SocketException if there is an error obtaining the
   * traffic class or type-of-service value.
   * @since 1.4
   * @see #setTrafficClass
   */
  public int getTrafficClass() throws SocketException
  {
    return (socket.getTrafficClass());
  }

  /**
   * Enable/disable SO_REUSEADDR.
   *
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
    socket.setReuseAddress(on);
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
    return (socket.getReuseAddress());
  }

  /**
   * Closes this socket.
   * <p>
   * Any thread currently blocked in an I/O operation upon this socket
   * will throw a {@link SocketException}.
   * <p>
   * Once a socket has been closed, it is not available for further networking
   * use (i.e. can't be reconnected or rebound). A new socket needs to be
   * created.
   *
   * <p> If this socket has an associated channel then the channel is closed
   * as well.
   *
   * @exception  IOException  if an I/O error occurs when closing this socket.
   * @revised 1.4
   * @spec JSR-51
   * @see #isClosed
   */
  public void close() throws IOException
  {
    socket.close();
  }

  /**
   * Places the input stream for this socket at "end of stream".
   * Any data sent to the input stream side of the socket is acknowledged
   * and then silently discarded.
   * <p>
   * If you read from a socket input stream after invoking
   * shutdownInput() on the socket, the stream will return EOF.
   *
   * @exception IOException if an I/O error occurs when shutting down this
   * socket.
   *
   * @since 1.3
   * @see java.net.Socket#shutdownOutput()
   * @see java.net.Socket#close()
   * @see java.net.Socket#setSoLinger(boolean, int)
   * @see #isInputShutdown
   */
  public void shutdownInput() throws IOException
  {
    socket.shutdownInput();
  }

  /**
   * Disables the output stream for this socket.
   * For a TCP socket, any previously written data will be sent
   * followed by TCP's normal connection termination sequence.
   *
   * If you write to a socket output stream after invoking
   * shutdownOutput() on the socket, the stream will throw
   * an IOException.
   *
   * @exception IOException if an I/O error occurs when shutting down this
   * socket.
   *
   * @since 1.3
   * @see java.net.Socket#shutdownInput()
   * @see java.net.Socket#close()
   * @see java.net.Socket#setSoLinger(boolean, int)
   * @see #isOutputShutdown
   */
  public void shutdownOutput() throws IOException
  {
    socket.shutdownOutput();
  }

  /**
   * Converts this socket to a <code>String</code>.
   *
   * @return  a string representation of this socket.
   */
  public String toString()
  {
    return (socket.toString());
  }

  /**
   * Returns the connection state of the socket.
   *
   * @return true if the socket successfuly connected to a server
   * @since 1.4
   */
  public boolean isConnected()
  {
    return (socket.isConnected());
  }

  /**
   * Returns the binding state of the socket.
   *
   * @return true if the socket successfuly bound to an address
   * @since 1.4
   * @see #bind
   */
  public boolean isBound()
  {
    return (socket.isBound());
  }

  /**
   * Returns the closed state of the socket.
   *
   * @return true if the socket has been closed
   * @since 1.4
   * @see #close
   */
  public boolean isClosed()
  {
    return (socket.isClosed());
  }

  /**
   * Returns wether the read-half of the socket connection is closed.
   *
   * @return true if the input of the socket has been shutdown
   * @since 1.4
   * @see #shutdownInput
   */
  public boolean isInputShutdown()
  {
    return (socket.isInputShutdown());
  }

  /**
   * Returns wether the write-half of the socket connection is closed.
   *
   * @return true if the output of the socket has been shutdown
   * @since 1.4
   * @see #shutdownOutput
   */
  public boolean isOutputShutdown()
  {
    return (socket.isOutputShutdown());
  }
}
