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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;

/**
 * The default Connection Manager. This Manager is intended to be used in a
 * <b>two tier scenario</b> (Non-managed Environment, see Connection
 * Architecture 1.0 Chapter 5.9).
 *
 */
public class DefaultConnectionManager
      implements ConnectionManager
{
   /** The full qualified name of this class. */
   private static final transient String CLASSNAME
         = DefaultConnectionManager.class.getName();

   /** The logger to use. */
   private static final transient Logger logger = Logger.getLogger(CLASSNAME);

   /**
    * The Default <code>serialVersionUID</code>
    */
   private static final long serialVersionUID = 1L;

   private final Set mMc = new HashSet();

   /**
    * Creates a new physical connection to the underlying EIS instance.
    *
    * @see javax.resource.spi.ConnectionManager#allocateConnection(javax.resource.spi.ManagedConnectionFactory, javax.resource.spi.ConnectionRequestInfo)
    */
   public Object allocateConnection (ManagedConnectionFactory mcf,
         ConnectionRequestInfo cri)
         throws ResourceException
   {
      logger.entering(CLASSNAME, "allocateConnection",
            new Object [] {mcf, cri});

      ManagedConnection mc = mcf.matchManagedConnections(mMc, null, cri);

      if (mc == null)
      {
         mc = mcf.createManagedConnection(null, cri);
         synchronized (mMc)
         {
            mMc.add(mc);
         }
      }

      final Object result = mc.getConnection(null, cri);

      logger.exiting(CLASSNAME, "allocateConnection", result);

      return result;
   }

   /**
    * Cleans up all managed connections.
    */
   public void cleanUp ()
   {
      synchronized (mMc)
      {
         final Iterator itr = mMc.iterator();
         while (itr.hasNext())
         {
            try
            {
               ((ManagedConnection) itr.next()).cleanup();
            }
            catch (ResourceException re)
            {
               // nothing to do
            }
         }
      }
   }

   /**
    * Destroys all managed connections.
    */
   public void destroy ()
   {
      synchronized (mMc)
      {
         final Iterator itr = mMc.iterator();
         while (itr.hasNext())
         {
            try
            {
               ((ManagedConnection) itr.next()).destroy();
            }
            catch (ResourceException re)
            {
               // nothing to do
            }
         }
      }
      mMc.clear();
   }
}
