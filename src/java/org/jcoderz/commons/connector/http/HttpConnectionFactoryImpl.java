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

import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;


/**
 * Factory for the creation of a connection handle used by the client.
 *
 */
public class HttpConnectionFactoryImpl
      implements HttpConnectionFactory
{
   /** The managed connection factory in use. */
   private final HttpManagedConnectionFactoryImpl mManagedConnectionFactory;
   /** The connection manager in use. */
   private final ConnectionManager mConnectionManager;
   /** The reference used for JNDI. */
   private Reference mReference;
   /** Human-readable description. */
   private String mDescription;

   /**
    * Constructor.
    *
    * @param mcf    the used ManagedConnectionFactory
    * @param cm     the used ConnectionManager
    */
   public HttpConnectionFactoryImpl (HttpManagedConnectionFactoryImpl mcf,
      ConnectionManager cm)
   {
      mManagedConnectionFactory = mcf;
      mConnectionManager = cm;
   }

   /** {@inheritDoc} */
   public HttpConnection getConnection (HttpConnectionSpec connectionSpec)
         throws ResourceException
   {
      return new HttpConnectionHelper(this, connectionSpec);
   }

   /**
    * Gets a HttpConnection implementation
    * (here: HttpConnectionImpl - see deployment descriptor) as
    * counterpart of the HttpManagedConnectionImpl.
    * This connection does not support multiple retries and is used
    * within the HttpConnectionHelper to provide retries.
    *
    * @param connectionSpec specifies the target system
    * @return HttpConnection an implementation of the HttpConnection
    *          interface
    * @throws ResourceException
    */
   protected HttpConnection getConnectionHandle (
         HttpConnectionSpec connectionSpec)
         throws ResourceException
   {
      final HttpConnectionRequestInfo cri = new HttpConnectionRequestInfo(
            mManagedConnectionFactory, connectionSpec);
      final HttpConnection connectionHandle
         = (HttpConnection) mConnectionManager
            .allocateConnection(mManagedConnectionFactory, cri);
      return connectionHandle;
   }

   /** {@inheritDoc} */
   public void setReference (Reference reference)
   {
      mReference = reference;
   }

   /** {@inheritDoc} */
   public Reference getReference ()
   {
      return mReference;
   }

   /**
    * Get the human-readable description.
    *
    * @return String - the description of the connector as part of the
    *                  deployment descriptor
    */
   public String getDescription ()
   {
      return mDescription;
   }

   /**
    * Set the human-readable description.
    *
    * @param desc the description of the connector as part of the deployment
    *             descriptor
    */
   public void setDescription (String desc)
   {
      mDescription = desc;
   }
}
