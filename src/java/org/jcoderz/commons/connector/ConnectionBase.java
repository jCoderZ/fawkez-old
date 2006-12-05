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

import javax.resource.ResourceException;


/**
 * This abstract class provides a set of common methods that used by all
 * derived connection's implementations.
 *
 */
public abstract class ConnectionBase
      implements ConnectionHandle
{
   /** Indicates whether this connection has already been closed.*/
   private boolean mIsClosed = false;
   /** Indicates whether this connection has already been cleaned up.*/
   private boolean mIsCleanedUp = false;
   /** The listener to be used. */
   private ConnectionNotificationListener mCnl;

   /**
    * Constructor.
    *
    * @param cnl The ConnectionNotificationListener to be used.
    */
   protected ConnectionBase (final ConnectionNotificationListener cnl)
   {
      mCnl = cnl;
   }

   /**
    * Throws the {@link ResourceException} if this connection has already
    * been closed.
    *
    * @throws javax.resource.spi.IllegalStateException if this connection has
    *    already been closed.
    */
   protected void assertNotClosed ()
         throws javax.resource.spi.IllegalStateException
   {
      if (mIsClosed)
      {
         throw new javax.resource.spi.IllegalStateException(
               "Connection has already been closed.");
      }
   }

   /**
    * Throws the {@link ResourceException} if this connection has already been
    * cleaned up.
    *
    * @throws javax.resource.spi.IllegalStateException if this connection has
    *    already been  cleaned up.
    */
   protected void assertNotCleanedUp ()
         throws javax.resource.spi.IllegalStateException
   {
      if (mIsCleanedUp)
      {
         throw new javax.resource.spi.IllegalStateException(
               "Connection has already been cleaned up.");
      }
   }

   /**
    * Throws the {@link ResourceException} if this connection has already been
    * closed or cleaned up.
    *
    * @throws ResourceException if this connection has already been closed or
    * cleaned up.
    */
   protected void assertValid ()
         throws ResourceException
   {
      assertNotClosed();
      assertNotCleanedUp();
   }

   /**
    * Sets this connection to the state closed. All futher calls on the public
    * methods of this connection will throw a ResourceException.
    *
    * @throws ResourceException thrown if this connection has already been
    * closed.
    */
   protected void setClosed ()
         throws ResourceException
   {
      assertValid();

      mIsClosed = true;
      mCnl.notifyConnectionClosed(this);
   }

   /**
    * Sets this connection to the state cleaned up. All futher calls on the
    * public methods of this connection will throw a ResourceException.
    */
   public void cleanUp ()
   {
      mIsCleanedUp = true;
   }

   /** {@inheritDoc} */
   public void changeAssociation (final ConnectionNotificationListener newCnl)
   {
      mCnl.notifyConnectionDissociated(this);
      mCnl = newCnl;
   }
}
