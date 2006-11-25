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
 * The listener interface used by the http connector to perform
 * callback functionality on the client of the resource adapter.
 *
 */
public interface HttpConnectorEventListener
{
   /**
    * Callback method indicating that the request is being sent to the
    * peer, possibly retrying if sending fails at first.
    *
    * @param context the context set for the connector handle
    */
   void requestSendWithRetry (ConnectorContext context);

   /**
    * Callback method indicating that the request is being sent to
    * the peer.
    * This can be called multiple times indicating retries sending
    * the request.
    *
    * @param context the context set for the connector handle
    */
   void requestSend (ConnectorContext context);

   /**
    * Callback method indicating that the response has been received
    * from the peer in use.
    *
    * @param statusCode the HTTP status code
    * @param responseData the HTTP response
    * @param context the context set for the connector handle
    */
   void responseReceived (
         int statusCode, byte[] responseData, ConnectorContext context);

   /**
    * Callback indicating that a response was received after the given
    * number of retries.
    *
    * @param numberOfRetries the number of retries
    * @param responseData the HTTP response data
    * @param context the context set for the connector handle
    */
   void responseReceivedAfterRetry (
         int numberOfRetries, byte[] responseData, ConnectorContext context);
}
