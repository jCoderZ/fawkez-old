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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.ResourceException;

import org.jcoderz.commons.InternalErrorException;
import org.jcoderz.commons.connector.ConnectionTimeoutErrorException;
import org.jcoderz.commons.connector.ConnectorConfiguration;
import org.jcoderz.commons.connector.ConnectorException;
import org.jcoderz.commons.connector.CreatingConnectorFailedException;
import org.jcoderz.commons.connector.http.transport.ConnectorContext;
import org.jcoderz.commons.connector.http.transport.HttpConnectorEventListener;
import org.jcoderz.commons.connector.http.transport.HttpRequestResponseHeader;
import org.jcoderz.commons.util.Assert;


/**
 * Implementation of the HttpConnection interface.
 * This class will be used by the client as connection handle
 * (supports retries).
 *
 */
public final class HttpConnectionHelper
      implements HttpConnection
{
   /** Class name used for logging. */
   private static final String CLASSNAME
         = HttpConnectionHelper.class.getName();
   /** Logger in use. */
   private static final Logger logger
         = Logger.getLogger(CLASSNAME);
   /** ConnectionFactory to create HttpConnectionImpl instance that
       is associated with a managed connecion. */
   private final HttpConnectionFactoryImpl mConnectionFactoryImpl;
   /** Connection Spec for the requested connection. */
   private final HttpConnectionSpec mConnectionSpec;
   /** Connection handle to the managed connection. */
   private HttpConnectionExtended mConnection;
   /** Delay for retries in milli seconds. */
   private int mRequiredDelay;
   /** Flag indicating that a delay for retries is necessary. */
   private boolean mIsRetryRequired = false;
   private final ConnectorConfiguration mConfig;
   /** Number of retries to perform. */
   private final int mAmountOfTries;
   private HttpRequestResponseHeader mRequestResponseHeader = null;

   private HttpConnectorEventListener mEventListener;
   private ConnectorContext mListenerContext;

   /**
    * Constructor.
    * Gets the connection spec and the connection factory to establish new
    * connections for retries if necessary.
    *
    * @param cf the connection factory for establishing connections
    * @param cs the connection spec identifying the connection target
    */
   public HttpConnectionHelper (
         HttpConnectionFactory cf, HttpConnectionSpec cs)
   {
      Assert.notNull(cs, "cs");
      mConnectionFactoryImpl = (HttpConnectionFactoryImpl) cf;
      mConnectionSpec = cs;

      mConfig = ConfigurationFactory.getConfiguration();
      mAmountOfTries = mConfig.getAmountOfTriesForwardingRequest();
   }

   /** {@inheritDoc} */
   public byte[] sendAndReceive (byte[] message)
         throws ResourceException, ConnectorException
   {
      final String methodName = "sendAndReceive";
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, methodName);
      }
      ConnectorException caughtException = null;

      mIsRetryRequired = false;
      byte[] response = null;
      int tryNumber = 0;
      List collectedExceptions = null;

      fireBeforeSend();

      do
      {
         try
         {
            tryNumber++;
            response = process(message);
         }
         catch (ConnectorException ce)
         {
            if (collectedExceptions == null)
            {
               collectedExceptions = new ArrayList();
            }
            caughtException = ce;
            if (mIsRetryRequired && tryNumber < mAmountOfTries)
            {
               collectedExceptions.add(caughtException);
               logForRetry(tryNumber, caughtException);
               caughtException = null;
               sleepForDelay();
            }
         }
      } // ..until we have received a response or the amount of tries
        // has been exceeded or the last caught exception does not lead
        // to an retry
      while (response == null
            && tryNumber < mAmountOfTries
            && caughtException == null);

      fireAfterReceive(tryNumber, response);

      assertRetries(tryNumber, caughtException, collectedExceptions);
      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, methodName);
      }
      return response;
   }

   private void assertRetries (
         int tryNumber,
         ConnectorException caughtException,
         List collectedExceptions)
         throws ConnectorException
   {
      if (tryNumber >= mAmountOfTries)
      {  // throw TimeoutException if amout or tries are exceeded
         collectedExceptions.add(caughtException);
         final ConnectorException ex = createFinalTimeoutException(
               collectedExceptions);
         throw ex;
      }
      else if (caughtException != null)
      {
         // ..otherwise throw last exception
         throw caughtException;
      }
   }

   /**
    * Performs the send and receive on the real connection handle
    * (HttpConnectionImpl).
    *
    * @param message the message to send
    * @return byte[] the response in return
    * @throws ResourceException in case of an resource adapter failure
    *          within the application server
    * @throws ConnectorException in case of a connection specific failure
    */
   private byte[] process (byte[] message)
         throws ResourceException, ConnectorException
   {
      final String methodName = "process";
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, methodName);
      }
      byte[] response;
      try
      {
         response = getConnection().sendAndReceive(message);
      }
      catch (ConnectorException ce)
      {
         mIsRetryRequired = mConnection.isRetryRequired();
         mRequiredDelay = mConnection.getRequiredDelayForRetries();
         mConnection = null;
         if (logger.isLoggable(Level.FINER))
         {
            logger.throwing(CLASSNAME, methodName, ce);
         }
         throw ce;
      }
      catch (ResourceException re)
      {
         if (mConnection == null)
         {
            mIsRetryRequired = true;
            mRequiredDelay
                  = mConfig.getConnectionErrorRetryDelayInMilliSeconds();
            final CreatingConnectorFailedException rqe
                  = new CreatingConnectorFailedException(
                     mConnectionSpec.getUrl(), re);
            if (logger.isLoggable(Level.FINER))
            {
               logger.throwing(CLASSNAME, methodName, rqe);
            }
            throw rqe;
         }
         if (logger.isLoggable(Level.FINER))
         {
            logger.throwing(CLASSNAME, methodName, re);
         }
         throw re;
      }
      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, methodName);
      }
      return response;
   }

   /** {@inheritDoc} */
   public void setEventListener (HttpConnectorEventListener listener,
         ConnectorContext context)
         throws ResourceException
   {
      mEventListener = listener;
      mListenerContext = context;
      getConnection().setEventListener(listener, context);
   }

   /** {@inheritDoc} */
   public void setRequestResponseHeader (HttpRequestResponseHeader header)
         throws ResourceException
   {
      mRequestResponseHeader = header;
      if (mConnection != null)
      {
         mConnection.setRequestResponseHeader(mRequestResponseHeader);
      }
   }

   /** {@inheritDoc} */
   public void close ()
   {
      if (mConnection != null)
      {
         mConnection.close();
      }
   }

   private void fireBeforeSend ()
   {
      if (mEventListener != null)
      {
         mEventListener.requestSendWithRetry(mListenerContext);
      }
   }

   private void fireAfterReceive (int retries, byte[] response)
   {
      if (mEventListener != null)
      {
         mEventListener.responseReceivedAfterRetry(
               retries, response, mListenerContext);
      }
   }

   /**
    * Gets the HttpConnectionImpl object implementing the
    * HttpConnection interface.
    * @return HttpConnection
    * @throws ResourceException in case of an error whilst obtaining the
    *          MpiConnection object
    */
   private HttpConnection getConnection ()
         throws ResourceException
   {
      final String methodName = "getConnection";
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, methodName);
      }
      if (mConnection == null)
      {
         mConnection = (HttpConnectionExtended) mConnectionFactoryImpl.
               getConnectionHandle(mConnectionSpec);
         if (mRequestResponseHeader != null)
         {
            mConnection.setRequestResponseHeader(mRequestResponseHeader);
         }
      }
      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, methodName, mConnection);
      }
      return mConnection;
   }

   /**
    * Logs additional information on LEVEL.FINE for a retry sending request.
    * @param tryNumber the number of try
    * @param ce the caught exception
    */
   private void logForRetry (int tryNumber, ConnectorException ce)
   {
      if (logger.isLoggable(Level.FINE))
      {
         final String messageText = "Will retry to send request "
               + " after sleeping " + mRequiredDelay + " millis."
               + "Resend caused by an '" + ce
               + "'. This is try number " + tryNumber + ".";
         logger.fine(messageText);
      }
   }

   /**
    * Sleeping for the time in milli seconds set for delay.
    * @param tries
    *          the number of current try
    */
   private void sleepForDelay ()
   {
      try
      {
         Thread.sleep(mRequiredDelay);
      }
      catch (InterruptedException e)
      {
         throw new InternalErrorException(
               "Interrupt while sleeping for delay between connection retries",
               e);
      }
   }

   /**
    * Adds the previously occurred exceptions as parameters to the
    * last exception,.
    */
   private ConnectorException createFinalTimeoutException (List exceptions)
   {
      // T-T-UC8.E2 - Three Failed Attempts to Send Request
      // T-T-UC8.E2.1
      // All failures are mapped to a timeout exception!
      ConnectionTimeoutErrorException result;
      if (exceptions != null)
      {
         final Iterator i = exceptions.iterator();
         int pos = 0;
         final StringBuffer failures = new StringBuffer();
         while (i.hasNext())
         {
            pos++;
            final Exception e = (Exception) i.next();
            failures.append("TRY_");
            failures.append(pos);
            failures.append("_EXCEPTION_WAS ");
            failures.append(String.valueOf(e));
            failures.append('\n');
            failures.append("stacktrace:");
            failures.append(getStackTrace(e, null));
            failures.append('\n');
         }
         result = new ConnectionTimeoutErrorException(
            mConnectionSpec.getUrl(), failures.toString());
      }
      else
      {
         result = new ConnectionTimeoutErrorException(
               mConnectionSpec.getUrl(), null);
      }
      return result;
   }

   private String getStackTrace (Throwable ex, StringBuffer result)
   {
      final StackTraceElement[] stack = ex.getStackTrace();
      final StringBuffer buffer;
      if (result == null)
      {
         buffer = new StringBuffer();
      }
      else
      {
         buffer = result;
         buffer.append('\n');
         buffer.append("Caused by:");
         buffer.append(ex.toString());
      }
      buffer.append('\n');
      int ix = 0;
      while (ix < stack.length)
      {
         final StackTraceElement stackElement = stack[ix];
         buffer.append(stackElement.toString());
         buffer.append('\n');
         ix++;
      }
      final Throwable cause = ex.getCause();
      if (cause != null)
      {
         getStackTrace(cause, buffer);
      }
      return buffer.toString();
   }

}
