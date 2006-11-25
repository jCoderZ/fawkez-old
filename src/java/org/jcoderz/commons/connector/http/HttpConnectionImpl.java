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
 *      its contributors may be used to endorse or promote productsb
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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.ResourceException;

import org.jcoderz.commons.connector.ConnectionRequestFailedException;
import org.jcoderz.commons.connector.ConnectionResponseFailedException;
import org.jcoderz.commons.connector.ConnectionTimeoutErrorException;
import org.jcoderz.commons.connector.ConnectorConfiguration;
import org.jcoderz.commons.connector.ConnectorException;
import org.jcoderz.commons.connector.http.transport.ConnectorContext;
import org.jcoderz.commons.connector.http.transport.HttpClientConnectionException;
import org.jcoderz.commons.connector.http.transport.HttpConnectConnectionException;
import org.jcoderz.commons.connector.http.transport.HttpConnectionException;
import org.jcoderz.commons.connector.http.transport.HttpConnectorEventListener;
import org.jcoderz.commons.connector.http.transport.HttpEmptyResponseException;
import org.jcoderz.commons.connector.http.transport.HttpInvalidResponseHeaderException;
import org.jcoderz.commons.connector.http.transport.HttpRequestResponseHeader;
import org.jcoderz.commons.connector.http.transport.HttpServerConnectionException;
import org.jcoderz.commons.connector.http.transport.HttpTimeoutConnectionException;
import org.jcoderz.commons.types.Url;
import org.jcoderz.commons.util.Assert;




/**
 * Implementation of the HttpConnectionExtended interface.
 * The implemented methods call the respective methods on
 * the ManagedHttpConnection class.
 *
 */
public final class HttpConnectionImpl
      implements HttpConnectionExtended
{
   /** Class name used for logging. */
   private static final String CLASSNAME
         = HttpConnectionImpl.class.getName();
   /** Logger in use. */
   private static final Logger logger
         = Logger.getLogger(CLASSNAME);
   /** Handle to the physical connection. Null means connection is invalid. */
   private HttpManagedConnectionImpl mManagedConnection;
   /** Flag indicating if the physical connection is closed by the managed
       connection - if true the connection is disassociated by the application
       server. */
   private boolean mClosedByManagedConnection = false;
   /** Delay for retries in milli seconds. */
   private int mRequiredDelay;
   /** Flag indicating that a delay for retries is necessary. */
   private boolean mIsRetryRequired = false;
   private final ConnectorConfiguration mConfig;


   /**
    * Constructor.
    * Sets the calling ManagedPaymentProtocolConnection object.
    *
    * @param mc the managed connection creating this object
    */
   public HttpConnectionImpl (HttpManagedConnectionImpl mc)
   {
      mManagedConnection = mc;
      mConfig = ConfigurationFactory.getConfiguration();
   }

   /**
    * @see org.jcoderz.commons.connector.http.HttpConnection#sendAndReceive(byte[])
    */
   public byte[] sendAndReceive (byte[] message)
         throws ResourceException, ConnectorException
   {
      final String methodName = "sendAndReceive";
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, methodName);
      }
      Assert.notNull(message, "message");

      mIsRetryRequired = false;
      byte[] result = null;
      try
      {
         result = getManagedConnection().sendAndReceive(message);
      }
      catch (HttpClientConnectionException hce)
      {
         handleClientConnectionException(hce);
      }
      catch (HttpServerConnectionException hse)
      {
         handleServerConnectionException(hse);
      }
      catch (HttpConnectConnectionException hce)
      {
         handleConnectConnectionException(hce);
      }
      catch (HttpTimeoutConnectionException hte)
      {
         handleTimeoutConnectionException(hte);
      }
      catch (HttpInvalidResponseHeaderException hie)
      {
         handleInvalidResponseHeaderException(hie);
      }
      catch (HttpEmptyResponseException ere)
      {
         handleEmptyResponseException(ere);
      }
      catch (HttpConnectionException he)
      {
         handleConnectionException(he);
      }

      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, methodName, result);
      }
      return result;
   }

   /**
    * @see HttpConnection#setRequestResponseHeader(HttpRequestResponseHeader)
    */
   public void setRequestResponseHeader (HttpRequestResponseHeader header)
         throws ResourceException
   {
      getManagedConnection().setRequestResponseHeader(header);
   }

   /**
    * @see HttpConnection#setEventListener(HttpConnectorEventListener, ConnectorContext)
    */
   public void setEventListener (HttpConnectorEventListener listener,
         ConnectorContext context)
   {
      getManagedConnection().setEventListener(listener, context);
   }

   private void handleClientConnectionException (HttpConnectionException ex)
         throws ConnectionResponseFailedException
   {
      logger.finer("Handling 'ClientConnectionException' " + ex.getMessage());
      if (mConfig.getHttpClientErrorResendFlag())
      {
         mIsRetryRequired = true;
         mRequiredDelay = mConfig.getHttpClientErrorResendDelayInMilliSeconds();
      }
      final ConnectionResponseFailedException cre
            = new ConnectionResponseFailedException(getUrl(), ex);
      invalidateManagedConnection(cre);
      throw cre;
   }

   private void handleServerConnectionException (HttpConnectionException ex)
         throws ConnectionResponseFailedException
   {
      logger.finer("Handling 'ServerConnectionException' " + ex.getMessage());
      if (mConfig.getHttpServerErrorResendFlag())
      {
         mIsRetryRequired = true;
         mRequiredDelay
               = mConfig.getHttpServerErrorResendDelayInMilliSeconds();
      }
      final ConnectionResponseFailedException cre
            = new ConnectionResponseFailedException(getUrl(), ex);
      invalidateManagedConnection(cre);
      throw cre;
   }

   private void handleConnectConnectionException (HttpConnectionException ex)
         throws ConnectionRequestFailedException
   {
      logger.finer("Handling 'ConnectConnectionException' " + ex.getMessage());
      final ConnectionRequestFailedException cre
            = new ConnectionRequestFailedException(getUrl(), ex);
      invalidateManagedConnection(cre);
      throw cre;
   }

   private void handleTimeoutConnectionException (HttpConnectionException ex)
         throws ConnectionTimeoutErrorException
   {
      logger.finer("Handling 'TimeoutConnectionException' " + ex.getMessage());
      if (mConfig.getHttpReadTimeoutErrorResendFlag())
      {
         mIsRetryRequired = true;
         mRequiredDelay
               = mConfig.getHttpReadTimeoutErrorResendDelayInMilliSeconds();
      }
      mIsRetryRequired = true;
      final StringBuffer previousFailure = new StringBuffer();
      previousFailure.append(ex.toString());
      final Throwable cause = ex.getCause();
      if (cause != null)
      {
         previousFailure.append(" caused by ");
         previousFailure.append(cause.toString());
      }
      final ConnectionTimeoutErrorException cte
            = new ConnectionTimeoutErrorException(
                  getUrl(), previousFailure.toString(), ex);
      invalidateManagedConnection(cte);
      throw cte;
   }

   private void handleInvalidResponseHeaderException (
         HttpConnectionException ex)
         throws ConnectionResponseFailedException
   {
      logger.finer("Handling 'InvalidResponseHeaderException' "
            + ex.getMessage());
      mIsRetryRequired = false;
      final ConnectionResponseFailedException cre
            = new ConnectionResponseFailedException(getUrl(), ex);
      invalidateManagedConnection(cre);
      throw cre;
   }

   private void handleEmptyResponseException (
         HttpConnectionException ex)
         throws ConnectionResponseFailedException
   {
      logger.finer("Handling 'HttpEmptyResponseException' "
            + ex.getMessage());
      mIsRetryRequired = false;
      final ConnectionResponseFailedException cre
            = new ConnectionResponseFailedException(getUrl(), ex);
      invalidateManagedConnection(cre);
      throw cre;
   }

   private void handleConnectionException (HttpConnectionException ex)
         throws ConnectionRequestFailedException
   {
      // TODO: retry policy for the "other" errors
      //    like a server closing whilst sending the request..
      logger.finer("Handling 'ConnectionException' " + ex.getMessage());
      mIsRetryRequired = false;
      final ConnectionRequestFailedException cre
            = new ConnectionRequestFailedException(getUrl(), ex);
      invalidateManagedConnection(cre);
      throw cre;
   }

   /**
    * @see HttpConnection#close()
    */
   public void close ()
   {
      final String methodName = "close";
      logger.entering(CLASSNAME, methodName);

      // In case that the Connector detects an error on the
      // HttpManagedConnection, it has the duty to notify the
      // application server by calling the
      // notifyAboutConnectionErrorOccurred() callback. In this case, the
      // application server will call cleanup() on the HttpManagedConnection to
      // disassociate its connection handle. We can recognize that, as
      // mClosedByManagedConnection will be set.
      if (mManagedConnection == null && mClosedByManagedConnection)
      {
         // Connection handle already released by application server;
         // Doesn't need an explicit close().
         // trace message
         logger.finer("connection already closed by application server");
         //
      }
      else if (mManagedConnection == null && !mClosedByManagedConnection)
      {
         // trace message
         logger.finer("connection already closed");
         //
      }
      else
      {
         // Notify the HttpManagedConnection that this connection handle no
         // longer belongs to the HttpManagedConnection.
         getManagedConnection().disassociateConnection(this);
          // Notify all registered EventListeners (Application Server) that
         // this connection handle was closed.
         getManagedConnection().notifyAboutConnectionClosed(this);
          // Disassociate this connection handle by forgetting to which
         // HttpManagedConnection we belong. This connection handle is
         // now invalid.
         disassociateManagedConnection(false);
      }
      logger.exiting(CLASSNAME, methodName);
   }

   /**
    * Gets the associated ManagedConnection.
    *
    * @return the HttpManagedConnectionImpl
    * @throws IllegalStateException - if the managed connection handle
    *          is not set
    */
   protected HttpManagedConnectionImpl getManagedConnection ()
         throws IllegalStateException
   {
      if (mManagedConnection == null)
      {
         final String errorText = "Connection handle is invalid.";
         final IllegalStateException ise = new IllegalStateException(errorText);
         throw ise;
      }
      return mManagedConnection;
   }

   /**
    * <p>Associate a different physical connection to this user-level
    * connection.</p>
    * <p>Called by the AbstractManagedConnection if transaction boundaries
    * are crossed.</p>
    *
    * @param mc new physical connection instance
    * @throws ResourceException - like defined in the interface
    */
   protected void associateManagedConnection (HttpManagedConnectionImpl mc)
   {
      mManagedConnection = mc;
   }

   /**
    * Disassociate this connection handle by forgetting to which
    * ManagedConnection we belong. This connection handle is
    * now invalid.
    *
    * @param closedByManagedConnection flag indicates if this managed
    *          connnection is closed by itself
    */
   public void disassociateManagedConnection (boolean closedByManagedConnection)
   {
      if (closedByManagedConnection)
      {
         logger.finest("disassociated by ManagedConnection");
      }
      else
      {
         logger.finest("disassociated by Connection");
      }

      mClosedByManagedConnection = closedByManagedConnection;
      mManagedConnection = null;
   }

   /**
    * Disassociates the connection and marks it as broken.
    */
   private void invalidateManagedConnection (final Exception cause)
   {
      getManagedConnection().disassociateConnection(this);
      getManagedConnection().notifyAboutConnectionErrorOccurred(
            cause, this);
      disassociateManagedConnection(false);
   }

   private Url getUrl ()
   {
      return getManagedConnection().getUrl();
   }

   /**
    * Gets the time in milli seconds required if another retry
    * will be performed.
    *
    * @return int
    *          the delay to use in HttpConnecionImpl
    */
   public int getRequiredDelayForRetries ()
   {
      return mRequiredDelay;
   }

   /**
    * Gets the flag indicating that a retry is required if
    * max number of retries is not exceeded.
    * @return boolean
    *          if true perform retry - else not
    */
   public boolean isRetryRequired ()
   {
      return mIsRetryRequired;
   }
}
