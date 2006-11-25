/*
 * $Id$
 *
 * Copyright 2006, The jCoderZ.org Project. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials
 *      provided with the distribution.
 *    * Neither the name of the jCoderZ.org Project nor the names of
 *      its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jcoderz.commons.connector.http;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.jcoderz.commons.connector.ConnectorConfiguration;
import org.jcoderz.commons.connector.http.transport.ConnectorContext;
import org.jcoderz.commons.connector.http.transport.HttpClientConnection;
import org.jcoderz.commons.connector.http.transport.HttpClientConnectionImpl;
import org.jcoderz.commons.connector.http.transport.HttpConnectionException;
import org.jcoderz.commons.connector.http.transport.HttpConnectorEventListener;
import org.jcoderz.commons.connector.http.transport.HttpRequestResponseHeader;
import org.jcoderz.commons.types.Url;
import org.jcoderz.commons.util.Constants;




/**
 * This class defines the managed connection maintained by the application
 * server and triggered via the non-managed connection.
 *
 */
public class HttpManagedConnectionImpl
      implements ManagedConnection
{
   /** Class name for use in logging. */
   private static final String CLASSNAME
         = HttpManagedConnectionImpl.class.getName();
   /** Logger in use. */
   private static final Logger logger
         = Logger.getLogger(CLASSNAME);
   /** Indicated if the managed connection is established. */
   private boolean mConnectionAssociated = false;
   /** Indicates if the managed connection is opened. */
   private boolean mConnectionOpened = false;
   /** The logger used for the implementation of the interface
       ManagedConnection.*/
   private  PrintWriter mPrintWriter;
   /** Object containing all app server references that must be notified
       used for the implementation of the interface ManagedConnection. */
   private final Set mEventListeners = new HashSet();
   /** Connection handle associated with this ManagedConnection. */
   private HttpConnectionImpl mConnectionHandle;
   /** The factory which have created this object. */
   private final HttpManagedConnectionFactoryImpl mManagedConnectionFactory;
   /** The connection specification used to trigger the creation of
       this physical connection. */
   private HttpConnectionRequestInfo mConnectionRequestInfo;
   /** Physical Connection */
   private HttpClientConnection mConnection;
   /** Flag indicates that the physical connection must be closed. */
   private boolean mPhysicalCloseRequired = false;
   /** URL the physical Connection will connect to. */
   private Url mUrl;
   private final ConnectorConfiguration mConfig;

   private int mConnectTimeout;
   private int mReadTimeout;
   private String mKeyAlias;
   private String mKeyAliasPassword;
   private KeyStore mKeyStore = null;
   private KeyStore mTrustStore = null;

   private HttpConnectorEventListener mHttpEventListener;
   private ConnectorContext mConnectorContext;
   private HttpRequestResponseHeader mRequestResponseHeader;


   /**
    * Constructor.
    * @param mcf the HttpManagedConnectionFactoryImpl associated with
    * @param cri the ConnectionRequestInfo specifying the requested connection
    */
   public HttpManagedConnectionImpl (
      HttpManagedConnectionFactoryImpl mcf,
      ConnectionRequestInfo cri)
   {
      final String methodName = "Constructor";
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, methodName, cri);
      }
      mConfig = ConfigurationFactory.getConfiguration();
      // set the ManagedConnectionFactory creating this object
      mManagedConnectionFactory = mcf;
       // set the obtained ConnectionRequestInfo
      mConnectionRequestInfo = (HttpConnectionRequestInfo) cri;
      mConnectionAssociated = false;
      mConnectionOpened = false;
      mConnectionHandle = null;
      if (mConnectionRequestInfo != null)
      {
         configure(mConnectionRequestInfo);
      }
      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, methodName);
      }
   }

   /*
    * Methods defined in interface
    *     javax.resource.spi.ManagedConnection
    *
    *     void                            addConnectionEventListener
    *                                        (ConnectionEventListener listener)
    *     void                            associateConnection
    *                                        (java.lang.Object connection)
    *     void                            cleanup ()
    *     void                            destroy ()
    *     java.lang.Object                getConnection (Subject subject,
    *                                         ConnectionRequestInfo info)
    *     LocalTransaction                getLocalTransaction ()
    *     java.io.PrintWriter             getLogWriter ()
    *     void                            setLogWriter (java.io.PrintWriter out)
    *     ManagedConnectionMetaData       getMetaData ()
    *     javax.transaction.xa.XAResource getXAResource ()
    *     void                            removeConnectionEventListener
    *                                        (ConnectionEventListener listener
    */

   /**
    * @see javax.resource.spi.ManagedConnection#addConnectionEventListener(javax.resource.spi.ConnectionEventListener)
    */
   public void addConnectionEventListener (ConnectionEventListener listener)
   {
      mEventListeners.add(listener);
   }

   /**
    * @see javax.resource.spi.ManagedConnection#associateConnection(java.lang.Object)
    */
   public void associateConnection (Object connection)
   {
      final String methodName = "associateConnection";
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, methodName);
      }

      // the managed connection is already configured and the
      // physical connection created anew if necessary
      // - the check is obsolete
      //checkIfDestroyed();
      if (mConnectionHandle != null)
      {
         if (logger.isLoggable(Level.WARNING))
         {
            final String messageText = "connection still have a handle";
            logger.warning(messageText);
         }
         mConnectionHandle.disassociateManagedConnection(true);
      }
      final HttpConnectionImpl usercon = (HttpConnectionImpl) connection;
      usercon.associateManagedConnection(this);
      mConnectionHandle = usercon;
      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, methodName);
      }
   }

   /**
    * @see javax.resource.spi.ManagedConnection#cleanup()
    */
   public void cleanup ()
   {
      final String methodName = "cleanup";
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, methodName);
      }

      // if makes no difference if the physical connection is closed
      // or not cause reusing the managed connection creates a new
      // physical connection inside "configure" if necessary
      // - the check is obsolete
      //checkIfDestroyed();
      // Invalidate the connection handle and
      // release the reference.
      if (mConnectionAssociated)
      {
         mConnectionHandle.disassociateManagedConnection(true);
         mConnectionAssociated = false;
         mConnectionHandle = null;
      }
       if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, methodName);
      }
   }

   /**
    * @see javax.resource.spi.ManagedConnection#destroy()
    */
   public void destroy ()
   {
      final String methodName = "destroy";
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, methodName);
      }

      // the container is not the only one allowed to close the physical
      // connection - the managed connection close it itself in
      // case of an "Connection : Close" attribute received in the
      // response message
      // - the check is obsolete
      //checkIfDestroyed();

      // trace message
      if (logger.isLoggable(Level.FINE))
      {
         final String messageText
            = "destroy PHYSICAL CONNECTION (" + this + ") to "
                  + mConnectionRequestInfo;
         logger.log(Level.FINE, messageText);
      }
      //
      disconnect();

      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, methodName);
      }
   }

   /**
    * @see javax.resource.spi.ManagedConnection#getConnection(javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
    */
   public Object getConnection (Subject subject, ConnectionRequestInfo cri)
   {
      final String methodName = "getConnection";
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, methodName);
      }
      mPhysicalCloseRequired = false;
      mConnectionRequestInfo = (HttpConnectionRequestInfo) cri;

      // There must not an association to a connection handle when this
      // method is called.
      if (mConnectionAssociated)
      {
         final String errorText
            = "managed connection already associated to a connection handle";
         throw new IllegalStateException(errorText);
      }

      // if not connected then configure and connect
      if (!mConnectionOpened)
      {
         // trace message
         if (logger.isLoggable(Level.FINE))
         {
            final String messageText
               = "establishing NEW PHYSICAL CONNECTION (" + this + ") to "
                     + mConnectionRequestInfo;
            logger.fine(messageText);
         }
         //
         if (mConnectionRequestInfo != null)
         {
            configure(mConnectionRequestInfo);
         }
         // we connect while sending....
      }
      else
      {
         // trace message
         if (logger.isLoggable(Level.FINE))
         {
            final String messageText
               = "reusing PHYSICAL CONNECTION (" + this + ") to "
                     + mConnectionRequestInfo;
            logger.fine(messageText);
         }
         //
      }
      mConnectionAssociated = true;
      mConnectionHandle = new HttpConnectionImpl(this);
      return mConnectionHandle;
   }

   /**
    * @see javax.resource.spi.ManagedConnection#getLocalTransaction()
    */
   public LocalTransaction getLocalTransaction ()
         throws ResourceException
   {
      final String messageText = "Local transaction not supported";
      throw new NotSupportedException(messageText);
   }

   /**
    * @see javax.resource.spi.ManagedConnection#getLogWriter()
    */
   public PrintWriter getLogWriter ()
   {
      return mPrintWriter;
   }

   /**
    * @see javax.resource.spi.ManagedConnection#setLogWriter(java.io.PrintWriter)
    */
   public void setLogWriter (PrintWriter out)
   {
      mPrintWriter = out;
   }

   /**
    * @see javax.resource.spi.ManagedConnection#getMetaData()
    */
   public ManagedConnectionMetaData getMetaData ()
   {
      // it is not specified that the physical connection have to be
      // open whilst that call
      final HttpManagedConnectionMetaData managedConnectionMetaData
         = new HttpManagedConnectionMetaData(this);
      return managedConnectionMetaData;
   }

   /**
    * @see javax.resource.spi.ManagedConnection#getXAResource()
    */
   public XAResource getXAResource ()
         throws ResourceException
   {
      final String messageText = "XA transaction not supported";
      throw new NotSupportedException(messageText);
   }

   /**
    * @see javax.resource.spi.ManagedConnection#removeConnectionEventListener(javax.resource.spi.ConnectionEventListener)
    */
   public void removeConnectionEventListener (ConnectionEventListener listener)
   {
      mEventListeners.remove(listener);
   }


   /*
    * Implementation of interface
    *     javax.resource.spi.HttpManagedConnection
    *
    * finished.
    */


   /**
    * Performs a physical connection close.
    */
   private void disconnect ()
   {
      final String methodName = "disconnect";
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, methodName);
      }

      // if "disconnect" is triggered by container side
      // and the response contained a "Connection:Close"
      // HTTP header the connection has been already
      // closed by the Managed Connection
      if (!mConnectionOpened && mPhysicalCloseRequired)
      {
         if (logger.isLoggable(Level.FINE))
         {
            final String messageText = "already disconnected (" + this + ")"
                  + " due to \'Connection:Close\' in response";
            logger.fine(messageText);
         }
      }
      else
      {
         // close the physical socket connection
         mConnection.closeConnection();
         mConnectionOpened = false;
      }

      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, methodName);
      }
   }

  /**
   * Configures the physical connection.
   * Uses configuration parameter from the
   * {@link org.jcoderz.commons.connector.http.HttpConnectionSpec
   * HttpConnectionSpec}.
   * This method is called inside the
   * {@link #HttpManagedConnection(HttpManagedConnectionFactoryImpl,
   * ConnectionRequestInfo) constructor}
   * and
   * {@link #getConnection(Subject, ConnectionRequestInfo ) getConnection(..)}
   * method.
   *
   * @param cri    the Connection Request Info
   */
   private final void configure (HttpConnectionRequestInfo cri)
   {
      final String methodName = "configure";
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, methodName);
      }

      String messageText;
      String errorText;

      if (mConnectionOpened)
      {
         messageText = "Unable to configure an already opened connection";
         // trace message
         if (logger.isLoggable(Level.WARNING))
         {
            logger.warning(messageText);
         }
         //
      }
      else
      {
         if (cri.getConnectionSpec() != null)
         {
            // set configuration parameters obtained in the cri
            // for the connection
            mUrl = cri.getConnectionSpec().getUrl();

            mConnectTimeout = mConfig.getConnectTimeoutInMilliSeconds();
            mReadTimeout = mConfig.getReadTimeoutInMilliSeconds();

            mKeyAlias = mConfig.getSslKeyAlias();
            mKeyAliasPassword = mConfig.getSslKeyAliasPassword();
            //FIXME: mKeyStore = KeyStoreLocator.getKeyStore();
            //FIXME: mTrustStore = KeyStoreLocator.getTrustStore();

            if (mConnection == null)
            {
               // trace message
               if (logger.isLoggable(Level.FINER))
               {
                  messageText = "creating new HTTPConnection";
                  logger.log(Level.FINER, messageText);
               }
               //
               mConnection = new HttpClientConnectionImpl();
            }

            mConnection.initSsl(
                  mKeyStore, mTrustStore, mKeyAlias, mKeyAliasPassword);
            mConnection.establishConnection(
                  mUrl.toString(), mConnectTimeout, mReadTimeout);
         }
         else
         {
            errorText = "connection specification is not set";
            throw new IllegalStateException(errorText);
         }
      }
      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, methodName);
      }
   }

   /**
    * This method is used by the
    * {@link org.jcoderz.commons.connector.http.HttpConnectionImpl
    * HttpConnection Implementation} class to send and receive an HTTP
    * request/response using the commons-httpclient library.
    *
    * @param message the message body to send via HTTP.
    * @return the response received.
    * @throws HttpConnectionException for all types of connection failures
    */
   public byte[] sendAndReceive (byte[] message)
         throws HttpConnectionException
   {
      final String methodName = "sendAndReceive(byte[])";
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, methodName);
      }

      // set the event listener for "requestSend" / "responseReceived"
      mConnection.setEventListener(mHttpEventListener, mConnectorContext);

      // add Content-Length and Connection to the request header
      //     will not be overwritten if already set
      getRequestResponseHeader().addRequestHeader("Content-Length",
            String.valueOf(message.length));
      getRequestResponseHeader().addRequestHeader("Connection",
            "Keep-Alive");

      // set all given request header pairs
      mConnection.setRequestResponseHeader(getRequestResponseHeader());

      // set the given message body
      mConnection.setRequestBody(new ByteArrayInputStream(message));

      mConnectionOpened = true;

      //execute the httpclient connection
      mConnection.execute();

      // obtain the response
      final byte[] responseBytes = mConnection.getResponseBody();

      if (isCloseConnectionRequired())
      {
         mPhysicalCloseRequired = true;
      }

      mConnection.releaseConnection();
      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, methodName);
      }
      return responseBytes;
   }

   /**
    * Sets the header set for sending and validate whilst receiving.
    *
    * @param header header to set and to validate
    */
   public void setRequestResponseHeader (HttpRequestResponseHeader header)
   {
      mRequestResponseHeader = header;
   }

   /**
    * Sets the event listener used for SLA logs.
    * This listener will be set at the
    *
    * @param listener the listener to set
    * @param context the context to set
    */
   public void setEventListener (HttpConnectorEventListener listener,
         ConnectorContext context)
   {
      mHttpEventListener = listener;
      mConnectorContext = context;
   }

   /**
    * This method is used by the
    * {@link HttpConnectionHelper HttpConnectionHelper}
    * class to disassociate the connection handle
    * from this HttpManagedConnection instance.
    *
    * @param conn the connection handle to be disassociated
    * @throws javax.resource.spi.IllegalStateException if the connection handle
    *          to disassociate is not associated
    */
   void disassociateConnection (HttpConnectionImpl conn)
         throws IllegalStateException
   {
      final String methodName = "disassociateConnection";
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, methodName);
      }

      if (conn != mConnectionHandle)
      {
         final String errorText = "Connection handle was not created by "
                               + "THIS HttpManagedConnection " + conn;
         final IllegalStateException ise = new IllegalStateException(errorText);
          // trace message
         if (logger.isLoggable(Level.FINE))
         {
            logger.log(Level.FINE, errorText);
         }
         if (logger.isLoggable(Level.FINER))
         {
            logger.throwing(CLASSNAME, methodName, ise);
         }
         //
         throw ise;
      }
      mConnectionAssociated = false;
      mConnectionHandle = null;
      if (mPhysicalCloseRequired)
      {
         disconnect();
      }
      else
      {
         mConnection.releaseConnection();
      }

      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, methodName);
      }
   }

   /**
    * Gets the connection specification used to trigger the creation
    * of this physical connection.
    *
    * @return HttpConnectionRequestInfo - the requested object specifying
    *         the requested connection
    */
   ConnectionRequestInfo getConnectionRequestInfo ()
   {
      return mConnectionRequestInfo;
   }

   /**
    * Notify the registered set of listeners when an application component
    * closes a connection handle. The application server uses this connection
    * close event to make a decision on whether or not to put this
    * HttpManagedConnection instance back into the connection pool.
    *
    * @param connectionHandle the connection handle performing a close
    */
   void notifyAboutConnectionClosed (
      HttpConnectionImpl connectionHandle)
   {
      final String methodName = "notifyAboutConnectionClosed";
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, methodName);
      }

      if (mConnectionAssociated)
      {
         if (logger.isLoggable(Level.WARNING))
         {
            final String messageText
               = "HttpManagedConnection SHOULD have disassociated before";
            logger.warning(messageText);
         }

         mConnectionAssociated = false;
         mConnectionHandle = null;
      }
      final ConnectionEvent ce = new ConnectionEvent(
            this, ConnectionEvent.CONNECTION_CLOSED);
      ce.setConnectionHandle(connectionHandle);
      final Iterator it = mEventListeners.iterator();
      while (it.hasNext())
      {
         final ConnectionEventListener listener
            = (ConnectionEventListener) it.next();
         listener.connectionClosed(ce);
      }
       if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, methodName);
      }
   }

  /**
   * Notify the registered listeners of the occurrence of a physical
   * connection-related error. The event notification happens just before
   * a resource adapter throws an exception to the application component
   * using the connection handle.
   * The connectionErrorOccurred method indicates that the associated
   * HttpManagedConnection instance is now invalid and unusable.
   * The application server handles the connection error event
   * notification by initiating application server-specific cleanup
   * (for example, removing HttpManagedConnection instance from the
   * connection pool) and then calling HttpManagedConnection.destroy
   * method to destroy the physical connection.
   *
   * @param e                the exception indication a connection error
   * @param connectionHandle the connection handle notifying the
   *                         connection error
   */
   void notifyAboutConnectionErrorOccurred (
      Exception e,
      HttpConnectionImpl connectionHandle)
   {
      final String methodName = "notifyAboutConnectionErrorOccurred";
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, methodName);
      }

      if (mConnectionAssociated)
      {
         if (logger.isLoggable(Level.WARNING))
         {
            final String messageText
               = "HttpManagedConnection SHOULD have disassociated before";
            logger.warning(messageText);
         }
      }
      final ConnectionEvent ce = new ConnectionEvent(
            this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, e);
      if (connectionHandle != null)
      {
         ce.setConnectionHandle(connectionHandle);
      }

      final Iterator it = mEventListeners.iterator();
      while (it.hasNext())
      {
         final ConnectionEventListener listener
             = (ConnectionEventListener) it.next();
         listener.connectionErrorOccurred(ce);
      }
      if (logger.isLoggable(Level.FINER))
      {
          logger.exiting(CLASSNAME, methodName);
      }
   }

   /**
    *  Returns true if the underlying physical connection is currently open.
    *
    *  @return boolean - true if connection is open, else false
    */
   boolean isOpen ()
   {
      return mConnectionOpened;
   }

   /**
    *  Checks the response header if a physical connection close is required.
    */
   private boolean isCloseConnectionRequired ()
   {
      boolean result = false;
      final String connectionValue
         = mConnection.getResponseHeader("Connection");
       if (connectionValue != null
            && connectionValue.toLowerCase(
                  Constants.SYSTEM_LOCALE).equals("close"))
      {
         logger.fine("\'Connection\' attribute in response header is \'"
            + connectionValue
            + "\' physical connection will be closed");
         result = true;
      }
      return result;
   }

   protected Url getUrl ()
   {
      return mUrl;
   }

   /**
    * Gets the managed connection factory of this managed connection.
    *
    * @return ManagedPaymentProtocolConnectionFactory the factory in use
    */
   public HttpManagedConnectionFactoryImpl getManagedConnectionFactory ()
   {
      return mManagedConnectionFactory;
   }

   private HttpRequestResponseHeader getRequestResponseHeader ()
   {
      if (mRequestResponseHeader == null)
      {
         mRequestResponseHeader = new HttpRequestResponseHeader();
      }
      return mRequestResponseHeader;
   }

   /**
    * Gets the string representation of the HttpManagedConnection.
    *
    * @return the string representation
    */
   public String toString ()
   {
      final String result
         = "HttpManagedConnectionImpl hashCode()=" + hashCode();
      return result;
   }
}
