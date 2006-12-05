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
package org.jcoderz.commons.connector;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;


/**
 * This class implements the {@link javax.resource.spi.ManagedConnectionFactory}
 * interface and provides a set of abstract methods which have to be overrided
 * by a derived class to provide the connector specific features.
 *
 * @see javax.resource.spi.ManagedConnection
 *
 */
public abstract class ManagedConnectionFactoryBase
      implements ManagedConnectionFactory
{
   /** Used in logger exntering/exiting calls. */
   private static final transient String CREATECONNECTIONFACTORY
         = "createConnectionFactory";

   /** The full qualified name of this class. */
   private static final transient String CLASSNAME
         = ManagedConnectionFactoryBase.class.getName();

   /** The logger to use. */
   private static final transient Logger logger = Logger.getLogger(CLASSNAME);

   /** Default <code>serialVersionUID</code>. */
   private static final long serialVersionUID = 1L;

   /** The default ConnectionManager. */
   private final DefaultConnectionManager mDCM = new DefaultConnectionManager();

   /** The PrintWriter instance to use. */
   private PrintWriter mPrintWriter;

   /** The semaphore object used for mPrintWriter's access. */
   private final Object mPwSemaphore = new Object();

   /** User name. */
   private String mUserName;
   /** Password. */
   private String mPassword;

   /**
    * Tests whether the given managed connection <code>mc</code> matchs for
    * handling the connection allocation request.
    *
    * @param mc The managed connection to be tested.
    * @param up The caller's security information.
    * @param cri A resource adapter specific connection request information.
    *
    * @return True if the given managed connection <code>mc</code> matchs for
    *    handling the connection allocation request. Otherwise false.
    *
    * @throws ResourceException thrown in error cases.
    */
   protected abstract boolean isMatchingManagedConnection (ManagedConnection mc,
      UserPassword up, ConnectionRequestInfo cri)
         throws ResourceException;

   /**
    * Creates a new ConnectionFactory. A derived class may provide a CCI
    * connection factory interface as recomended in the JCA Specification or
    * a non-CCI interface that provides a resource adapter specific interface.
    *
    * @param cm The ConnectorManager to use.
    * @return A new instance of connection factory (CCI or non-CCI).
    * @throws ResourceException thrown in error cases.
    */
   protected abstract Object createConnectionFactoryImpl (ConnectionManager cm)
         throws ResourceException;

   protected abstract ManagedConnection createManagedConnectionImpl (
         UserPassword up, ConnectionRequestInfo cri);

   /** {@inheritDoc} */
   public ManagedConnection createManagedConnection (Subject subject,
         ConnectionRequestInfo cri)
         throws ResourceException
   {
      logger.entering(CLASSNAME, "createManagedConnection",
            new Object [] {subject, cri});

      UserPassword up = SecurityUtil.getUserPassword(subject, this, cri);
      final UserPassword propPw = getUserPassword();

      if (UserPassword.isEmpty(up) && !UserPassword.isEmpty(propPw))
      {
         up = propPw;
      }

      final ManagedConnection result = createManagedConnectionImpl(up, cri);

      logger.exiting(CLASSNAME, "createManagedConnection", result);

      return result;
   }

   /** {@inheritDoc} */
   public Object createConnectionFactory (ConnectionManager cm)
         throws ResourceException
   {
      logger.entering(CLASSNAME, CREATECONNECTIONFACTORY, cm);

      final Object result = createConnectionFactoryImpl(cm);

      logger.exiting(CLASSNAME,  CREATECONNECTIONFACTORY, result);

      return result;
   }

   /** {@inheritDoc} */
   public Object createConnectionFactory ()
         throws ResourceException
   {
      logger.entering(CLASSNAME, CREATECONNECTIONFACTORY);

      final Object result = createConnectionFactory(mDCM);

      logger.exiting(CLASSNAME, CREATECONNECTIONFACTORY, result);

      return result;
   }


   /** {@inheritDoc} */
   public ManagedConnection matchManagedConnections (Set set, Subject subject,
         ConnectionRequestInfo cri)
         throws ResourceException
   {
      final boolean finer = logger.isLoggable(Level.FINER);

      if (finer)
      {
         logger.entering(CLASSNAME, "matchManagedConnections", new Object [] {
               set, subject, cri});
      }

      final UserPassword up = SecurityUtil.getUserPassword(subject, this, cri);
      ManagedConnection result = null;
      if (set != null)
      {
         final Iterator itr = set.iterator();
         while (itr.hasNext())
         {
            final Object obj = itr.next();
            if (obj instanceof ManagedConnection)
            {
               final ManagedConnection mc = (ManagedConnection) obj;
               if (isMatchingManagedConnection(mc, up, cri))
               {
                  result = mc;
                  break;
               }
            }
         }
      }

      if (finer)
      {
         logger.exiting(CLASSNAME, "matchManagedConnections", result);
      }

      return result;
   }

   /** {@inheritDoc} */
   public void setLogWriter (PrintWriter pw)
         throws ResourceException
   {
      synchronized (mPwSemaphore)
      {
         mPrintWriter = pw;
      }
   }

   /** {@inheritDoc} */
   public PrintWriter getLogWriter ()
         throws ResourceException
   {
      synchronized (mPwSemaphore)
      {
         return mPrintWriter;
      }
   }

   /**
    * Sets the user name for this factory. This method should be called by
    *    deployment code.
    *
    * @param userName the user name to be set.
    */
   public void setUserName (String userName)
   {
      logger.entering(CLASSNAME, "setUserName", userName);
      mUserName = userName;
      logger.exiting(CLASSNAME, "setUserName");
   }

   /**
    * Sets the password for this factory. This method should be called by
    *    deployment code.
    *
    * @param password the passwor to be set.
    */
   public void setPassword (String password)
   {
      logger.entering(CLASSNAME, "setPassword", "xxx");
      mPassword = password;
      logger.exiting(CLASSNAME, "setPassword");
   }

   /**
    * Returns the UserPassword instance, containing the user name and password,
    *    set be deployment code.
    *
    * @return the UserPassword instance.
    */
   public UserPassword getUserPassword ()
   {
      return UserPassword.fromUserPassword(mUserName, mPassword);
   }

   /** {@inheritDoc} */
   public int hashCode ()
   {
      // FIXME Implement this method
      return super.hashCode();
   }

   /**
    * Returns <code>true</code> if this
    * <code>ManagedConnectionFactoryImpl</code> is the same as the o argument.
    * @param o Object to be compared with this.
    * @return <code>true</code> if this
    * <code>ManagedConnectionFactoryImpl</code> is the same as the o argument.
    */
   public boolean equals (Object o)
   {
      if (this == o)
      {
         return true;
      }
      if (o == null)
      {
         return false;
      }
      if (o.getClass() != getClass())
      {
         return false;
      }
      if (! (o instanceof ManagedConnectionFactoryBase))
      {
         return false;
      }

      return true;
   }
}
