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

 /**
  * HttpConnectionException indicates a connection failure on HTTP level.
  *
  */
public class HttpConnectionException
      extends Exception
{
   static final long serialVersionUID = -530559381852475115L;

   private String mHttpMessage = null;
   private String mStatusReason = null;
   private int mStatusCode = -1;


   /**
    * Constructor.
    *
    * @param cause the root cause of problem
    */
   public HttpConnectionException (Throwable cause)
   {
      super(cause);
   }

   /**
    * Constructor.
    *
    * @param message describing the HTTP error
    * @param cause the root cause of problem
    */
   public HttpConnectionException (String message, Throwable cause)
   {
      super(message, cause);
   }

   /**
    * @return Returns the message.
    */
   public String getHttpMessage ()
   {
      return mHttpMessage;
   }

   /**
    * @return Returns the statusCode.
    */
   public int getStatusCode ()
   {
      return mStatusCode;
   }

   /**
    * @return Returns the statusReason.
    */
   public String getStatusReason ()
   {
      return mStatusReason;
   }

   /**
    * @param statusReason The statusReason to set.
    */
   void setStatusReason (String statusReason)
   {
      mStatusReason = statusReason;
   }

   /**
    * @param httpMessage The message to set.
    */
   void setHttpMessage (String httpMessage)
   {
      mHttpMessage = httpMessage;
   }

   /**
    * @param statusCode The statusCode to set.
    */
   void setStatusCode (int statusCode)
   {
      mStatusCode = statusCode;
   }
}
