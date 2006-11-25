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

import java.rmi.RemoteException;
import javax.resource.ResourceException;
import org.jcoderz.commons.connector.ConnectorException;
import org.jcoderz.commons.connector.http.transport.ConnectorContext;
import org.jcoderz.commons.connector.http.transport.HttpConnectorEventListener;
import org.jcoderz.commons.connector.http.transport.HttpRequestResponseHeader;
import org.jcoderz.commons.types.Url;



/**
 * Session bean using the http connector.
 *
 */
public class HttpConnectorSessionImpl
      implements HttpConnectorSessionInterface,
                 HttpConnectorEventListener
{
   private long mSendTime = 0;
   private long mReceivedTime = 0;
   private int mTryCounter = 1;

   /** {@inheritDoc} */
   public ConnectorResponse sendAndReceive (
         byte[] request, Url target,
         HttpRequestResponseHeader header)
         throws RemoteException, ResourceException, ConnectorException
   {
      reset();
      final HttpConnectionSpec spec = new HttpConnectionSpec(this, target);
      final HttpConnection connection
            = HttpConnectionUtil.getHttpConnection(spec);
      connection.setEventListener(
            ((HttpConnectorEventListener) this), new ConnectorContext() { });
      connection.setRequestResponseHeader(header);
      final byte[] response;
      try
      {
         response = connection.sendAndReceive(
            "this is a message".getBytes());
      }
      catch (ConnectorException cex)
      {
         cex.log();
         throw cex;
      }
      connection.close();
      return new ConnectorResponse(
            calculatedRequestTime(), mTryCounter, response);
   }

   /** {@inheritDoc} */
   public void requestSend (ConnectorContext context)
   {
      if (mSendTime > 0)
      {
         mTryCounter++;
      }
      mSendTime = System.currentTimeMillis();
   }

   /** {@inheritDoc} */
   public void responseReceived (
         int resultCode, byte[] response, ConnectorContext context)
   {
      mReceivedTime = System.currentTimeMillis();
   }

   private long calculatedRequestTime ()
   {
      long result;
      if (mReceivedTime == 0)
      {
         result = -1;
      }
      else if (mReceivedTime <= mSendTime)
      {
         result = 0;
      }
      else
      {
         result = mReceivedTime - mSendTime;
      }
      return result;
   }

   private void reset ()
   {
      mSendTime = 0;
      mReceivedTime = 0;
      mTryCounter = 1;
   }

   /** {@inheritDoc} */
   public void requestSendWithRetry (ConnectorContext context)
   {
      // nop
   }

   /** {@inheritDoc} */
   public void responseReceivedAfterRetry (int numberOfRetries, 
           byte[] responseData, ConnectorContext context)
   {
      // nop
   }
}
