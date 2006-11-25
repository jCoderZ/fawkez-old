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

import org.jcoderz.commons.types.Url;
import org.jcoderz.commons.util.Assert;




/**
 * The connection spec for a commons http connection.
 *
 */
public final class HttpConnectionSpec
{
   /** Class of the object that created this ConnectionSpec.
       Needed to find the right ClassLoader in ManagedHttpConnection class. */
   private final Class mInvokingObjectClass;
   /** The target connection point */
   private final Url mUrl;
   /** The canonic string of this connection specification */
   private transient String mCanonicKey = null;


   /**
    * Constructor. Create a new instance of HttpConnectionSpec.
    *
    * @param invokingObject Object that creates this ConnectionSpec.
    * @param url the URL connected to
    */
   public HttpConnectionSpec (
         Object invokingObject,
         Url url)
   {
      Assert.notNull(invokingObject, "invokingObject");
      Assert.notNull(url, "url");
      mInvokingObjectClass = invokingObject.getClass();
      mUrl = url;
   }

   /**
    * Gets the invoking object.
    *
    * @return the object invoke
    */
   public Class getInvokingObjectClass ()
   {
      return mInvokingObjectClass;
   }

   /**
    * The property "URL" defines the target connection point.
    * Use this method to retrieve the current value of this property.
    *
    * @return URL - current value of property "URL".
    */
   public Url getUrl ()
   {
      return mUrl;
   }


   /**
    * Create canonic key string.
    * Return a canonic string which contains *all* relevant information of the
    * Connection Request info.
    * Example: "{http://target.host.com}"
    * NOTE: hashCode() and equals() depend on the proper implementation
    * of this !
    *
    * @return String - the canonic key
    */
   public String canonicKey ()
   {
      if (mCanonicKey == null)
      {
         mCanonicKey =
            "{" + getUrl() + "}";
      }
      return mCanonicKey;
   }
}
