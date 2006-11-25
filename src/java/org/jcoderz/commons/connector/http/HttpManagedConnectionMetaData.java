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

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionMetaData;


/**
 * This class specifies meta data for a managed http connection.
 */
public class HttpManagedConnectionMetaData
      implements ManagedConnectionMetaData
{
   /** The product version number of the system the connector connects to
       - used by the container. */
   private static final String EIS_PRODUCT_VERSION = "1.0";
      /** The product name. */
   private static final String EIS_PRODUCT_NAME = "Http Connector";
   /** The associated managed connection. */
   private final HttpManagedConnectionImpl mManagedConnection;


   /**
    * Constructor.
    *
    * @param mc the managed connection in use
    */
   HttpManagedConnectionMetaData (HttpManagedConnectionImpl mc)
   {
      mManagedConnection = mc;
   }

   /**
    * Gets the product name of the connector.
    *
    * @return String - the EIS name
    * @throws ResourceException
    *         - as expected by the application server
    */
   public String getEISProductName ()
         throws ResourceException
   {
      return EIS_PRODUCT_NAME;
   }

   /**
    * Gets the product version of the connector.
    *
    * @return String - the version of the EIS
    * @throws ResourceException
    *         - as expected by the application server
    */
   public String getEISProductVersion ()
         throws ResourceException
   {
      return EIS_PRODUCT_VERSION;
   }

   /**
    * Gets the name of the current user.
    * Not usefull.
    *
    * @return String - "N/A"
    * @throws ResourceException
    *         - as expected by the application server
    */
   public String getUserName ()
         throws ResourceException
   {
      return "N/A";
   }

   /**
    * Gets the maximum number of physical connections to the backend.
    *
    * @return int - the maximum number of physical connections
    * @throws ResourceException
    *         - as expected by the application server
    */
   public int getMaxConnections ()
         throws ResourceException
   {
      return mManagedConnection.getManagedConnectionFactory().
         getMaxConnections().intValue();
   }
}


