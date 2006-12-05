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

import java.io.Serializable;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.ConnectionManager;


/**
 * This Factory implements the {@link javax.resource.cci.ConnectionFactory}
 * interface and provides methods for getting connections to the File System.
 *
 */
class ConnectionFactoryBase
      implements ConnectionFactory, Serializable, Referenceable
{
   public static final long serialVersionUID = 1L;

   /** Reference to this ConnectionFactory. */
   private Reference mReference;

   /**
    * The ConnectionManager to use.
    * In case of <b>two tier scenario</b> the  ConnectionManager is an instance
    * of the DefaultConnectionManager.
    * In case of <b>three tier scenario</b> this Manager is provided by
    * the Application Server.
    */
   private final ConnectionManager mConnectionManager;

   /**
    * Constructs a ConnectionFactory.
    *
    * @param cm The ConnectionManager to use.
    */
   public ConnectionFactoryBase (final ConnectionManager cm)
   {
      mConnectionManager = cm;
   }

   /** {@inheritDoc} */
   public Connection getConnection ()
         throws ResourceException
   {
      return null;
   }

   /** {@inheritDoc} */
   public Connection getConnection (ConnectionSpec cs)
         throws ResourceException
   {
      return null;
   }

   /** {@inheritDoc} */
   public RecordFactory getRecordFactory ()
         throws ResourceException
   {
      return null;
   }

   /** {@inheritDoc} */
   public ResourceAdapterMetaData getMetaData ()
         throws ResourceException
   {
      return null;
   }

   /**
    * Sets the reference for this ConnectionFactory.
    * This method is called by deployment code.
    *
    * @param reference The reference for this ConnectionFactory.
    *
    * @see javax.resource.Referenceable#setReference(javax.naming.Reference)
    */
   public void setReference (Reference reference)
   {
      mReference = reference;
   }

   /**
    * Returns the non-null Reference of this for this ConnectionFactory.
    *
    * @return The non-null Reference of this for this ConnectionFactory.
    *
    * @exception NamingException If a naming exception was encountered
    *     while retrieving the reference.
    *
    * @see javax.naming.Referenceable#getReference()
    *
    */
   public Reference getReference ()
         throws NamingException
   {
      return mReference;
   }

}
