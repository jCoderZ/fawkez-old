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
package org.jcoderz.commons.connector.http.transport;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Container for the http header parameter set for request
 * and validate for response.
 *
 */
public class HttpRequestResponseHeader
      implements Serializable
{
   static final long serialVersionUID = -8149478577085570778L;

   /** Map containing the request header key value pairs to be set
       for a connnector request. */
   private final Map mRequestHeader = new HashMap();
   /** Map containing the response header key value pairs to be
       validated with a received connector response. */
   private final Map mResponseHeader = new HashMap();

   /**
    * Adds a key value pair of an http header used for request.
    * If the key already exists the value will not be overwritten.
    *
    * @param key the name of the http header
    * @param value the value of the http header
    */
   public void addRequestHeader (String key, String value)
   {
      if (!mRequestHeader.containsKey(key))
      {
         mRequestHeader.put(key, value);
      }
   }

   /**
    * Adds a key value pair of an http header expected in response.
    * If the key already exists the value will not be overwritten.
    *
    * @param key the name of the http header
    * @param value the value of the http header
    */
   public void addResponseHeader (String key, String value)
   {
      if (!mResponseHeader.containsKey(key))
      {
         mResponseHeader.put(key, value);
      }
   }

   protected Map getRequestHeader ()
   {
      return Collections.unmodifiableMap(mRequestHeader);
   }

   protected Map getResponseHeader ()
   {
      return Collections.unmodifiableMap(mResponseHeader);
   }

   /** {@inheritDoc} */
   public String toString ()
   {
      final StringBuffer result = new StringBuffer();
      result.append("RequestHeader:\n");
      result.append(mRequestHeader.toString());
      result.append("\nResponseHeader:\n");
      result.append(mResponseHeader.toString());
      return result.toString();
   }
}
