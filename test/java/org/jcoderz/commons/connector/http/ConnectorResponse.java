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

import java.io.Serializable;

/**
 * Value object for the Connector Bean Test.
 * Includes the time needed for sending the request and
 * receiving the response and the amount of tries and the
 * response message.
 *
 */
public class ConnectorResponse
      implements Serializable
{
   static final long serialVersionUID = -5201563036377336101L;

   private final long mRequestTime;
   private final int mTries;
   private final byte[] mResponse;

   /**
    * Constructor of the value object.
    *
    * @param requestTime - time in milli seconds need for sending the
    *         request and receiving the response
    * @param tries - the amout of tries for performing the request
    * @param response - the received response
    */
   public ConnectorResponse (long requestTime, int tries, byte[] response)
   {
      mRequestTime = requestTime;
      mTries = tries;
      mResponse = response;
   }

   /**
    * Get the request time in millis.
    *
    * @return long - the request time in millis, -1 if request failed
    */
   public long getRequestTime ()
   {
      return mRequestTime;
   }

   /**
    * Get the amount of tries.
    *
    * @return int - the amount of tries, 1 for no retry
    */
   public int getTries ()
   {
      return mTries;
   }

   /**
    * Get the response message.
    *
    * @return byte[] - the response message
    */
   public byte[] getResponse ()
   {
      final int length = mResponse.length;
      final byte[] result = new byte[length];
      System.arraycopy(mResponse, 0 , result, 0, length);
      return result;
   }

   /**
    * Dumps the value object.
    *
    * @see java.lang.Object#toString()
    */
   public String toString ()
   {
      final StringBuffer result = new StringBuffer();
      result.append("----Connector Response----");
      result.append("\nRequest time in millis: ");
      result.append(mRequestTime);
      result.append("\nAmount of tries: ");
      result.append(mTries);
      result.append("\nMessage:\n");
      result.append(new String(mResponse));
      result.append("\n--------------------------");
      return result.toString();
   }

}
