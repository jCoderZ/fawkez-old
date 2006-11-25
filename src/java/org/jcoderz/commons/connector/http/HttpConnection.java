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

import org.jcoderz.commons.connector.ConnectorException;
import org.jcoderz.commons.connector.http.transport.ConnectorContext;
import org.jcoderz.commons.connector.http.transport.HttpConnectorEventListener;
import org.jcoderz.commons.connector.http.transport.HttpRequestResponseHeader;


/**
 * This is the interface provided by the commons http connector.
 */
public interface HttpConnection
{
   /**
    * Send a http message using retries to the peer associated with the
    * connection in use and afterwards receive a response from it.
    *
    * @param message the request message that have to be send to the peer.
    * @return byte array containing the received http response from the peer.
    * @throws ResourceException indicating an error while sending a request
    *          and receiving a response detected by the application server
    * @throws ConnectorException in case of a failure on transport level
    *          whilst sending/receiving the request/response
    */
   byte[] sendAndReceive (byte[] message)
         throws ResourceException, ConnectorException;

   /**
    * Initiates close of the connection handle at the application level.
    * A connection client is required to call this method after the
    * connection is no more in use.
    * The physical connection will be still open afterwards.
    */
   void close ();

   /**
    * Sets the listener for http connector events like request send and
    * response received.
    *
    * @param listener the listener providing callback methods used by the
    *         connector
    * @param context used whilst callbacks
    * @throws ResourceException if appserver fails performing the call
    *          on the managed connection
    */
   void setEventListener (HttpConnectorEventListener listener,
         ConnectorContext context)
         throws ResourceException;

   /**
    * Sets http request header values used whilst sending the request
    * and http response header validated whilst receiveing the response.
    *
    * @param header contains request header to set and response header to
    *          validate
    * @throws ResourceException in case of appserver failure
    */
   void setRequestResponseHeader (HttpRequestResponseHeader header)
         throws ResourceException;
}
