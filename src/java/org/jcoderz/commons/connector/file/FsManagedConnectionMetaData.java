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

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionMetaData;

/**
 * Provides a set of Meta Data describing this File System Connector.
 *
 */
public class FsManagedConnectionMetaData
      implements ManagedConnectionMetaData
{
   /** The product name. */
   public static final String EIS_PRODUCT_NAME
         = "Commons File System Connector";

   /** The product version. */
   public static final String EIS_PRODUCT_VERSION = "1.0";

   /**
    * Maximum limit on number of active concurrent connections that the File
    * System Connector can support across client processes.
    * 0 means that the connector does not have any limit.
    */
   public static final int MAX_CONNECTIONS = 0;


   private final String mUserName;

   /**
    * Contructor.
    * @param userName to be set, can be null.
    */
   public FsManagedConnectionMetaData (String userName)
   {
      mUserName = userName;
   }

   /**
    * @see javax.resource.spi.ManagedConnectionMetaData#getEISProductName()
    */
   public String getEISProductName ()
         throws ResourceException
   {
      return EIS_PRODUCT_NAME;
   }

   /**
    * @see javax.resource.spi.ManagedConnectionMetaData#getEISProductVersion()
    */
   public String getEISProductVersion ()
         throws ResourceException
   {
      return EIS_PRODUCT_VERSION;
   }

   /**
    * Returns 0. 0 means that this connector does not have any limit.
    * @return 0
    * @throws ResourceException never thrown.
    * @see javax.resource.spi.ManagedConnectionMetaData#getMaxConnections()
    */
   public int getMaxConnections ()
         throws ResourceException
   {
      return MAX_CONNECTIONS;
   }

   /**
    * @see javax.resource.spi.ManagedConnectionMetaData#getUserName()
    */
   public String getUserName ()
         throws ResourceException
   {
      return mUserName;
   }
}
