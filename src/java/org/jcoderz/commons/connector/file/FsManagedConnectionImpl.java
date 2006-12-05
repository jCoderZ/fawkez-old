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
package org.jcoderz.commons.connector.file;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
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
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.jcoderz.commons.connector.ConnectionHandle;
import org.jcoderz.commons.connector.ConnectionNotificationListener;
import org.jcoderz.commons.connector.SecurityUtil;
import org.jcoderz.commons.connector.UserPassword;


/**
 * This Managed Connection is the factory for a File System Connction.
 *
 * The File System Connector is not transactional, so the methods
 * {@linkplain #getLocalTransaction()} and {@linkplain #getXAResource()} always
 * throw the {@link javax.resource.NotSupportedException}.
 *
 */
public class FsManagedConnectionImpl
      implements ManagedConnection, ConnectionNotificationListener
{
   /** The full qualified name of this class. */
   private static final transient String CLASSNAME
         = FsManagedConnectionImpl.class.getName();

   /** The logger to use. */
   private static final transient Logger logger = Logger.getLogger(CLASSNAME);
   private static final Random RANDOM = new Random();

   private final FsManagedConnectionMetaData mMetaData;

   /** Registered ConnectionEventListeners. */
   private final Set mConnectionEventListeners = new HashSet();

   /** All active connections, created by this Factory. */
   private final Set mConnections = new HashSet();

   /** The PrintWriter instance to use. */
   private PrintWriter mPrintWriter;

   private final UserPassword mUp;

   private final ConnectionRequestInfo mCri;
   private final ManagedConnectionFactory mMcf;

   private final int mIndex;
   private final String mStringified;

   /**
    * Constructor.
    * @param mcf The underlying ManagedConnectionFactory
    * @param up The UserPassword to be used.
    * @param cri Connection Request Info
    */
   public FsManagedConnectionImpl (ManagedConnectionFactory mcf,
         UserPassword up, ConnectionRequestInfo cri)
   {
      synchronized (RANDOM)
      {
         mIndex = RANDOM.nextInt(Integer.MAX_VALUE);
      }
      mStringified = "FsManagedConnectionImpl[" + mIndex + "]";
      mMcf = mcf;
      mUp = UserPassword.fromUserPassword(up);
      mCri = cri;
      mMetaData = new FsManagedConnectionMetaData(mUp.getUserName());

      logger.fine("Created FsManagedConnectionImpl[" + mIndex + "]");
   }

   /** {@inheritDoc} */
   public Object getConnection (Subject subject, ConnectionRequestInfo cri)
         throws ResourceException
   {
      final String method = "getConnection";
      final boolean finer = logger.isLoggable(Level.FINER);
      if (finer)
      {
         logger.entering(CLASSNAME, method, new Object [] {subject, cri});
      }

      final UserPassword up = SecurityUtil.getUserPassword(subject, mMcf, cri);

      if (!mUp.equals(up))
      {
         // JCA 1.0 8.2.7
         // If a resource adapter does not support re-authentication, the
         // getConnection method should throw
         // javax.resource.spi. SecurityException if the passed Subject in the
         // getConnection method is different from the security context
         // associated with the ManagedConnection instance.
         final javax.resource.spi.SecurityException rse
               = new javax.resource.spi.SecurityException(
                     "Re-authentication is not supported.");
         if (finer)
         {
            logger.throwing(CLASSNAME, method, rse);
         }
         throw rse;
      }

      final Properties props;
      if (cri instanceof FsConnectionRequestInfo)
      {
         props = ((FsConnectionRequestInfo) cri).getProperties();
      }
      else
      {
         props = null;
      }
      final Object result = new FsConnectionImpl(this, props);
      registerHandle(result, finer);

      if (finer)
      {
         logger.exiting(CLASSNAME, method, result);
      }

      return result;
   }

   /** {@inheritDoc} */
   public void destroy ()
         throws ResourceException
   {
      final boolean finer = logger.isLoggable(Level.FINER);

      if (finer)
      {
         logger.entering(CLASSNAME, "destroy");
      }

      cleanupConnection();

      if (finer)
      {
         logger.exiting(CLASSNAME, "destroy");
      }
   }

   /** {@inheritDoc} */
   public void cleanup ()
         throws ResourceException
   {
      final boolean finer = logger.isLoggable(Level.FINER);
      if (finer)
      {
         logger.entering(CLASSNAME, "cleanup");
      }

      cleanupConnection();

      if (finer)
      {
         logger.exiting(CLASSNAME, "cleanup");
      }
   }

   /** {@inheritDoc} */
   public void associateConnection (Object connection)
         throws ResourceException
   {
      final boolean finer = logger.isLoggable(Level.FINER);

      if (finer)
      {
         logger.entering(CLASSNAME, "associateConnection", connection);
      }

      if (!(connection instanceof ConnectionHandle))
      {
         final ResourceException re = new ResourceException("Can not associate "
               + "the new connection handle: '" + connection + "'.");
         logger.throwing(CLASSNAME, "associateConnection", re);
         throw re;
      }

      final ConnectionHandle cb = (ConnectionHandle) connection;
      cb.changeAssociation(this);

      registerHandle(cb, finer);

      if (finer)
      {
         logger.exiting(CLASSNAME, "associateConnection");
      }
   }

   /** {@inheritDoc} */
   public void addConnectionEventListener (ConnectionEventListener cel)
   {
      logger.entering(CLASSNAME, "addConnectionEventListener", cel);

      synchronized (mConnectionEventListeners)
      {
         mConnectionEventListeners.add(cel);
      }

      logger.exiting(CLASSNAME, "addConnectionEventListener");
   }

   /** {@inheritDoc} */
   public void removeConnectionEventListener (ConnectionEventListener cel)
   {
      logger.entering(CLASSNAME, "removeConnectionEventListener", cel);

      synchronized (mConnectionEventListeners)
      {
         mConnectionEventListeners.remove(cel);
      }

      logger.exiting(CLASSNAME, "removeConnectionEventListener", cel);
   }

   /**
    * Always throws the {@link NotSupportedException}.
    * The File System Connector does not support any XA Transaction.
    *
    * @return never return.
    *
    * @throws ResourceException Always thrown.
    *
    * @see javax.resource.spi.ManagedConnection#getXAResource()
    */
   public XAResource getXAResource ()
         throws ResourceException
   {
      throw new NotSupportedException("Resource Adapter does not support "
            + "XA Transaction.");
   }

   /**
    * Always throws the {@link NotSupportedException}.
    * The File System Connector does not support any Local Transaction.
    *
    * @return never return.
    *
    * @throws ResourceException Always thrown.
    *
    * @see javax.resource.spi.ManagedConnection#getLocalTransaction()
    */
   public LocalTransaction getLocalTransaction ()
         throws ResourceException
   {
      throw new NotSupportedException("Resource Adapter does not support "
            + "Local Transaction.");
   }

   /** {@inheritDoc} */
   public ManagedConnectionMetaData getMetaData ()
         throws ResourceException
   {
      return mMetaData;
   }

   /** {@inheritDoc} */
   public void setLogWriter (PrintWriter pw)
         throws ResourceException
   {
      logger.entering(CLASSNAME, "setLogWriter", pw);
      mPrintWriter = pw;
      logger.exiting(CLASSNAME, "setLogWriter");
   }

   /** {@inheritDoc} */
   public PrintWriter getLogWriter ()
         throws ResourceException
   {
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, "getLogWriter");
         logger.exiting(CLASSNAME, "getLogWriter", mPrintWriter);
      }

      return mPrintWriter;
   }

   /** {@inheritDoc} */
   public void notifyConnectionClosed (Object source)
   {
      final boolean finer = logger.isLoggable(Level.FINER);

      if (finer)
      {
         logger.entering(CLASSNAME, "notifyConnectionClosed", source);
      }

      // This connection has been closed
      deregisterHandle(source, finer);

      // Connection Closed Event.
      final ConnectionEvent closedEvent = new ConnectionEvent(this,
            ConnectionEvent.CONNECTION_CLOSED);

      // Set connection handle
      closedEvent.setConnectionHandle(source);

      // Notify all connection listeners
      notifyEvent(closedEvent);

      if (finer)
      {
         logger.exiting(CLASSNAME, "notifyConnectionClosed");
      }
   }

   /** {@inheritDoc} */
   public void notifyConnectionErrorOccurred (Object source, Exception e)
   {
      logger.entering(CLASSNAME, "notifyConnectionErrorOccurred",
            new Object [] {source, e});

      // Connection Closed Event.
      final ConnectionEvent closedEvent = new ConnectionEvent(this,
            ConnectionEvent.CONNECTION_ERROR_OCCURRED, e);

      // Set connection handle
      closedEvent.setConnectionHandle(source);

      // Notify all connection listeners
      notifyEvent(closedEvent);

      logger.exiting(CLASSNAME, "notifyConnectionErrorOccurred");
   }

   /** {@inheritDoc} */
   public void notifyConnectionDissociated (Object source)
   {
      final boolean finer = logger.isLoggable(Level.FINER);
      if (finer)
      {
         logger.entering(CLASSNAME, "notifyConnectionDissociated", source);
      }

      deregisterHandle(source, finer);

      if (finer)
      {
         logger.exiting(CLASSNAME, "notifyConnectionDissociated");
      }
   }


   /**
    * @return UserPassword set for this managed connection.
    */
   UserPassword getUserPassword ()
   {
      return mUp;
   }

   /** {@inheritDoc} */
   public String toString ()
   {
      return mStringified;
   }

   /** {@inheritDoc} */
   public int hashCode ()
   {
      return mIndex;
   }

   /**
    * Indicates whether some other object is "equal to" this one.
    *
    * @param obj the object to compare to.
    * @return true if this object is the same as the obj argument; false
    *         otherwise.
    */
   public boolean equals (Object obj)
   {
      return (obj instanceof FsManagedConnectionImpl
            && ((FsManagedConnectionImpl) obj).mIndex == this.mIndex);
   }

   private void registerHandle (final Object result, boolean loggable)
   {
      mConnections.add(result);

      if (loggable)
      {
         logger.finer(toString() + " added " + result
               + ", new connection count " + mConnections.size());
      }
   }

   private void deregisterHandle (Object source, boolean loggable)
   {
      mConnections.remove(source);

      if (loggable)
      {
         logger.finer(toString() + " removed " + source
               + ", new connection count " + mConnections.size());
      }
   }

   /**
    * Cleans up all handles created by this factory and frees all allocaded
    * resources. Removes all registered notification listeners.
    */
   private void cleanupConnection ()
   {
      final boolean finer = logger.isLoggable(Level.FINER);

      if (finer)
      {
         logger.entering(CLASSNAME, "cleanupConnection");
      }

      final Iterator itr = mConnections.iterator();

      ConnectionHandle current = null;
      while (itr.hasNext())
      {
         current = (ConnectionHandle) itr.next();
         try
         {
            current.cleanUp();
         }
         catch (ResourceException re)
         {
            logger.log(Level.WARNING, "Could not clean up the connection '"
                  + current + "'.", re);
         }
      }
      mConnections.clear();
      if (finer)
      {
         logger.exiting(CLASSNAME, "cleanupConnection");
      }
   }

   private void notifyEvent (final ConnectionEvent event)
   {
      final Object [] a;
      synchronized (mConnectionEventListeners)
      {
         // to avoid locking on the mConnectionEventListeners object
         a = mConnectionEventListeners.toArray();
      }

      // Notify all listeners
      for (int i = 0; i < a.length; i++)
      {
         ((ConnectionEventListener) a[i]).connectionClosed(event);
      }
   }
}
