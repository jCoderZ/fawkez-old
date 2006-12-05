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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.SecurityException;
import javax.resource.spi.security.GenericCredential;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

/**
 * Utility provides method to extract user name and password from a set of
 * security relevant objects such as
 * {@link javax.resource.spi.security.PasswordCredential},
 * {@link javax.resource.spi.ConnectionRequestInfo} and so on.
 *
 */
public final class SecurityUtil
{
   /** The full qualified name of this class. */
   private static final String CLASSNAME = SecurityUtil.class.getName();

   /** The logger to use. */
   private static final Logger logger = Logger.getLogger(CLASSNAME);

   private SecurityUtil ()
   {

   }

   /**
    * Extracts the user name and password from the given set of objects as
    * specified in the JCA 1.0 Chapter 8.2.6.
    *
    * @param subject Current Security Subject passed by the application server.
    * @param mcf The managed connection factory
    * @param cri Connection reques info
    *
    * @return UserPassword instance. If no user name / password can be
    * extracted, this method returns {@link UserPassword#EMPTY_USER_PASSWORD}.
    *
    * @throws SecurityException Thown if no applicable credentials found in
    * the Subject.
    */
   public static UserPassword getUserPassword (Subject subject,
      ManagedConnectionFactory mcf, ConnectionRequestInfo cri)
         throws SecurityException
   {
      final String method = "getUserPassword";
      final boolean finer = logger.isLoggable(Level.FINER);
      if (finer)
      {
         logger.entering(CLASSNAME, method, new Object [] {subject, mcf, cri});
      }

      UserPassword result = UserPassword.EMPTY_USER_PASSWORD;

      if (subject != null)
      {
         // JCA 1.0 8.2.6 Contract for RA (Option A)
         final PasswordCredential pc = PrivilegedExecutor.getPasswordCredential(
               subject, mcf);
         if (pc != null)
         {
            result = UserPassword.fromUserPassword(pc.getUserName(), new String(
                  pc.getPassword()));
         }
         else
         {
            final SecurityException rse = new SecurityException(
                  "No applicable credentials found in the Subject passed by the"
                        + " application server.");

            logger.throwing(CLASSNAME, method, rse);
            throw rse;

            // TODO Implements this path if required. (required ?)

            // JCA 1.0 8.2.6 Contract for RA (Option B)
            /*
            final GenericCredential gc
                  = PrivilegedExecutor.getGenericCredential(subject, mcf);

            if (gc == null)
            {
               throw new SecurityException("No applicable credentials found in"
                     + " the Subject passed by the application server.");
            }
            */
         }

      }
      else if (cri != null)
      {
         // JCA 1.0 8.2.6 Contract for RA (Option C)
         if (!(cri instanceof ConnectionRequestInfoBase))
         {
            final SecurityException rse = new SecurityException("No applicable "
                  + "ConnectionRequestInfo passed by the application server.");
            logger.throwing(CLASSNAME, method, rse);
            throw rse;
         }

         result = UserPassword.fromUserPassword(
               ((ConnectionRequestInfoBase) cri).getUserPassword());
      }

      if (finer)
      {
         logger.exiting(CLASSNAME, method, result);
      }

      return result;
   }

   /**
    * This class is intended to execute a privileged action.
    * The security contract assumes that a resource adapter has the necessary
    * security permissions to extract a private credential set from a Subject
    * instance.
    */
   private static class PrivilegedExecutor
         implements PrivilegedAction
   {
      private final Subject mSubject;
      private final ManagedConnectionFactory mMcf;
      private final boolean mPcAction;

      PrivilegedExecutor (Subject subject, ManagedConnectionFactory mcf,
            boolean pcAction)
      {
         mSubject = subject;
         mMcf = mcf;
         mPcAction = pcAction;
      }

      /** {@inheritDoc} */
      public Object run ()
      {
         final Object result;
         if (mPcAction)
         {
            result = getPwdCredential();
         }
         else
         {
            result = getGenCredential();
         }
         return result;
      }

      private Object getPwdCredential ()
      {
         // This method implements the behavior of a Resource Adapter specified
         // in the JCA 1.0 8.2.6 Contract for RA (Option A)

         // The resource adapter explicitly checks whether the passed Subject
         // instance carries a PasswordCredential instance using the
         // Subject.getPrivateCredentials method.
         final Set creds = mSubject.getPrivateCredentials(
               PasswordCredential.class);
         PasswordCredential pc = null;
         // Since a Subject instance can carry multiple PasswordCredential
         // instances, a Managed- ConnectionFactory should only use a
         // PasswordCredential instance that has been specifically passed to
         // it through the security contract.
         final Iterator credentials = creds.iterator();
         while (credentials.hasNext())
         {
            final PasswordCredential pcCurrent
                  = (PasswordCredential) credentials.next();

            // The ManagedConnectionFactory implementation uses the equals
            // method to compare itself with the passed instance.
            if (pcCurrent.getManagedConnectionFactory().equals(mMcf))
            {
               pc = pcCurrent;
               break;
            }
         }
         return pc;
      }

      private Object getGenCredential ()
      {
         // This method implements the behavior of a Resource Adapter specified
         // in the JCA 1.0 8.2.6 Contract for RA (Option B)

         // The resource adapter explicitly checks whether passed Subject
         // instance carries a GenericCredential instance using the methods
         // getPrivateCredentials and getPublicCredentials defined on the
         // Subject interface.
         final Set creds = mSubject.getPrivateCredentials(
               GenericCredential.class);

         Object result = null;

         final Iterator credentials = creds.iterator();
         while (credentials.hasNext())
         {
            final GenericCredential c = (GenericCredential) credentials.next();
            result = c;
            break;
         }
         return result;
      }

      /**
       * Tries to extract a PasswordCredential from the Subject
       * <code>subject</code> applicable to the managed connection factory
       * <code>mcf</code>.
       * Executes a privileged action.
       *
       *
       * @param subject Current Security Subject passed by the
       *    application server.
       * @param mcf The managed connection factory
       *
       * @return a PasswordCredential or null.
       */
      static PasswordCredential getPasswordCredential (Subject subject,
            ManagedConnectionFactory mcf)
      {
         return (PasswordCredential) AccessController.doPrivileged(
               new PrivilegedExecutor(subject, mcf, true));
      }

      /**
       * Tries to extract a GenericCredential from the Subject
       * <code>subject</code> applicable to the managed connection factory
       * <code>mcf</code>.
       * Executes a privileged action.
       *
       * @param subject Current Security Subject passed by the application
       *    server.
       *
       * @param mcf The managed connection factory
       *
       * @return a GenericCredential or null.
       */
      static GenericCredential getGenericCredential (Subject subject,
            ManagedConnectionFactory mcf)
      {
         return (GenericCredential) AccessController.doPrivileged(
               new PrivilegedExecutor(subject, mcf, false));
      }
   }
}
