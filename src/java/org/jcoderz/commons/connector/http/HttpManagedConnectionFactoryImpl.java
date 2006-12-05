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

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;


/**
 * Managed Connection Factory used by the Application Server to create
 * a non-managed factory and connection for the application.
 *
 */
public final class HttpManagedConnectionFactoryImpl
      implements ManagedConnectionFactory
{
   /** The serial UID generated for revision 1.4 */
   static final long serialVersionUID = 5601979166228177337L;

   /** Class name for use in logging. */
   private static final transient
      String CLASSNAME = HttpManagedConnectionFactoryImpl.class.getName();

   /** The logger in use. */
   private static final transient
      Logger logger = Logger.getLogger(CLASSNAME);

   /** The system specific line separator */
   private static final String
      LINE_FEED = System.getProperty("line.separator");
   private transient HttpManagedConnectionImpl mManagedConnection = null;
   private transient int mAmountOfMatchingConnections = 0;
   private transient int mAmountOfOpenMatchingConnections = 0;


  /** The URL to connect to. */
   private String mUrl;

   /** The maximum amount of open connections. */
   private Integer mMaxConnections;


   /** {@inheritDoc} */
   public Object createConnectionFactory (ConnectionManager cm)
   {
      return new HttpConnectionFactoryImpl(this, cm);
   }

   /** {@inheritDoc} */
   public Object createConnectionFactory ()
   {
      return new HttpConnectionFactoryImpl(this, null);
   }

   /** {@inheritDoc} */
   public ManagedConnection createManagedConnection (
         Subject subject, ConnectionRequestInfo cri)
   {
      return new HttpManagedConnectionImpl(this, cri);
   }

   /** {@inheritDoc} */
   public ManagedConnection matchManagedConnections (
         Set connectionSet,
         Subject subject,
         ConnectionRequestInfo cri)
   {
      final String methodName = "matchManagedConnections";

      // trace message
      if (logger.isLoggable(Level.FINEST))
      {
         logger.finest("Connections in pool=" + connectionSet.size());
         logger.finest("ConnectionRequestInfo: " + cri);
      }
      //
      mManagedConnection = null;
      mAmountOfOpenMatchingConnections = 0;
      mAmountOfMatchingConnections = 0;

      final Iterator it = connectionSet.iterator();
      while (it.hasNext())
      {
         final Object obj = it.next();
         if (obj instanceof ManagedConnection)
         {
            final HttpManagedConnectionImpl mc
                  = (HttpManagedConnectionImpl) obj;
            // If there are no special requirements about the
            // ManagedConnection, the first ManagedConnection in
            // the pool is good enough
            if (cri == null)
            {
               logger.finest("No ConnectionRequestInfo specified.");
               logger.finest("Found connection in pool: " + mc);

               mManagedConnection = mc;
               break;       // found managed connection - without cri
            }
             // otherwise try to find a managed connection with an appropriate
            // CtsConnectionRequestInfo
            findMatchingConnection(mc, cri);
         }
      }
       if (mManagedConnection == null)
      {
         logger.finest("No matching connection found in pool.");
      }
      else
      {
         if (logger.isLoggable(Level.FINEST))
         {
            logger.log(Level.FINEST, LINE_FEED
               + "---------- Connection POOL -----------"      + LINE_FEED
               + " Connection to: " + cri.toString()           + LINE_FEED
               + " Total: " + mAmountOfMatchingConnections     + LINE_FEED
               + " Open:  " + mAmountOfOpenMatchingConnections + LINE_FEED
               + "--------------------------------------"      + LINE_FEED
               + LINE_FEED);
         }
         logger.exiting(CLASSNAME, methodName, mManagedConnection);
      }
      return mManagedConnection;
   }

   /**
    * Tries to match the given connection with the used connection request
    * info and calculates the pool amounts.
    *
    * @param mc
    *          the managed connection to match
    * @param cri
    *          the connection request info to match with
    */
   private void findMatchingConnection (
         HttpManagedConnectionImpl mc, ConnectionRequestInfo cri)
   {
      // otherwise try to find a managed connection with an appropriate
      // CtsConnectionRequestInfo
      if (mc.getConnectionRequestInfo().equals(cri))
      {
         if (mManagedConnection == null)
         {
            logger.finest("Found matching connection in pool: " + mc);
            mManagedConnection = mc; // found managed connection - with cri
         }
         mAmountOfMatchingConnections++;
         if (mc.isOpen())
         {
            mAmountOfOpenMatchingConnections++;
         }
      }
   }

   /** {@inheritDoc} */
   public void setLogWriter (PrintWriter arg0)
   {
      // not used
   }

   /** {@inheritDoc} */
   public PrintWriter getLogWriter ()
   {
      // not used
      return null;
   }

   /**
    * Sets the configuration parameter "MaxConnections"
    * defined in the deployment descriptor.
    *
    * @param connections max count of connections
    */
   public void setMaxConnections (Integer connections)
   {
      final String methodName = "setMaxConnection";
      if (logger.isLoggable(Level.FINER))
      {
         final Object[] args = {connections};
         logger.entering(CLASSNAME, methodName, args);
      }
      mMaxConnections = connections;
      logger.exiting(CLASSNAME, methodName);
   }

   /**
    * Gets the configuration parameter "MaxConnections".
    *
    * @return Long - the maximum number of connections
    */
   public Integer getMaxConnections ()
   {
      return mMaxConnections;
   }

   /**
    * Set configuration parameter "Url" as defined in deployment descriptor.
    *
    * @param url the url connecting to
    */
   public void setUrl (String url)
   {
      final String methodName = "setUrl";
      if (logger.isLoggable(Level.FINER))
      {
         final Object[] args = {url};
         logger.entering(CLASSNAME, methodName, args);
      }

      mUrl = url;
      logger.exiting(CLASSNAME, methodName);
   }

   /**
    * Gets the configuration parameter "Url".
    *
    * @return String - the url connection to
    */
   public String getUrl ()
   {
      return mUrl;
   }
}
