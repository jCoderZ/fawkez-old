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

import org.jcoderz.commons.connector.ConnectionRequestInfoBase;

 /**
  * This class contains the request information for a requested
  * connection. It implements the java connector architecture
  * interface <code>HttpConnectionRequestInfo</code>.
  *
  */
public class HttpConnectionRequestInfo
      extends ConnectionRequestInfoBase
 {
   /** the connection specification including the connection parameter */
   private final HttpConnectionSpec mConnectionSpec;

   /**
    * Constructor.
    * Called by a managed connection factory.
    *
    * @param mcf the calling managed connection factory
    */
   public HttpConnectionRequestInfo (HttpManagedConnectionFactoryImpl mcf)
   {
      mConnectionSpec = null;
   }

   /**
    * Constructor.
    * Create a HttpConnectionRequestInfo from a ConnectionSpec.
    *
    * @param mcf the calling managed connectin factory
    * @param cs the used connection specification
    */
   public HttpConnectionRequestInfo (
      HttpManagedConnectionFactoryImpl mcf,
      HttpConnectionSpec cs)
   {
      mConnectionSpec = cs;
   }

   /*
    * Methods defined in interface
    *     ConnectionRequestInfo
    *
    *     boolean equals (java.lang.Object other)
    *     int hashCode ()
    */

   /**
    * Compare two instances based on the relevant properties.<p>
    * Depends on
    * {link #canonicKey() <code>canonicKey</code>}
    *
    * @param obj other object instance
    * @return boolean - true if given object is equal to this request
    *                   information object, false else.
    */
   public boolean equals (Object obj)
   {
      return obj instanceof HttpConnectionRequestInfo
            && canonicKey().equals(
               ((HttpConnectionRequestInfo) obj).canonicKey());
   }

   /**
    * Calculate hashCode based on all relevant properties.<p>
    * Depends on
    * {link #canonicKey() <code>canonicKey</code>}
    *
    * @return int - the calculated hashCode
    */
   public int hashCode ()
   {
      return canonicKey().hashCode();
   }

   /*
    * Implementation of Interface
    *     javax.resource.spi.ConnectionRequestInfo
    *
    * finished.
    */


   /**
    * Create canonic human-readable string.
    * A canonic string contains *all* relevant information of the Connection
    * Request info.
    *
    * @return String - the result of <code>canonicKey</code>
    */
   public String toString ()
   {
      return canonicKey();
   }

   /**
    * Create canonic key string.
    * Return a canonic string which contains *all* relevant information of the
    * Connection Request info.
    * Example: "{user=Hans password=asecret domain=Purchasing}"
    *
    * NOTE: hashCode() and equals() depend on the proper implementation
    * of this !
    *
    * @return String - the canonical string
    */
   public String canonicKey ()
   {
      String result;
      if (mConnectionSpec != null)
      {
         result = mConnectionSpec.canonicKey();
      }
      else
      {
         // No special request-specific properties available
         result = "{dummy}";
      }
      return result;
   }

   /**
    * Gets the connection specification object of this request information.
    *
    * @return PaymentProtocolConnectionSpec - the specification object
    */
   public HttpConnectionSpec getConnectionSpec ()
   {
      return mConnectionSpec;
   }
}
