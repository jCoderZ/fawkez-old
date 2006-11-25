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

import java.io.InputStream;
import java.security.KeyStore;


/**
 * Interface used by the commons http connector to use the jakarta
 * commons-httpclient.
 *
 */
public interface HttpClientConnection
{
   /**
    * Establishes the connection to the target host using the given parameter
    * values. At this step only the underlaying objects will be created and
    * initializied. The physical connection will be open later on.
    *
    * @param url the URL to connect to
    * @param connectTimeout the connect timeout used for connecting the host
    * @param readTimeout the read timeout used whilst reading the response
    *                      from the target host
    */
   void establishConnection (
      String url, int connectTimeout, int readTimeout);

   /**
    * Releases the connection for reuse.
    * The physical connection is afterwards still open.
    */
   void releaseConnection ();

   /**
    * Closes the physical connection.
    */
   void closeConnection ();

   /**
    * Executes sending the request and receiving the response.
    *
    * @throws HttpConnectionException in case of a connection failure
    */
   void execute ()
         throws HttpConnectionException;

   /**
    * Sets the request body to send.
    *
    * @param body the message body to send
    */
   void setRequestBody (InputStream body);

   /**
    * Gets the response body received from the target host.
    *
    * @return byte[] - the body of the response message
    * @throws HttpEmptyResponseException if response body is empty
    */
   byte[] getResponseBody ()
         throws HttpEmptyResponseException;

   /**
    * Gets the response header parameter value for a given key.
    *
    * @param key the parameter key to obtain the value for
    * @return String - the parameter value for the given key
    */
   String getResponseHeader (String key);

   /**
    * Sets parameter necessary for SSL connection.
    *
    * @param keyStore
    *          the keystore given from Signature Service
    * @param trustStore
    *          the truststore given from Signature Service
    * @param keyAlias
    *          the alias of the key used from keystore
    * @param keyAliasPassword
    *          the password of the key in use
    */
   void initSsl (
         KeyStore keyStore,
         KeyStore trustStore,
         String keyAlias,
         String keyAliasPassword);

   /**
    * Sets parameter necessary for SSL connection.
    * Here: the keystores are loaded from the file system.
    *
    * @param keyAlias
    *          the alias of the key used from keystore
    * @param keyAliasPassword
    *          the password of the key in use
    */
   void initSsl (
         String keyAlias,
         String keyAliasPassword);

   /**
    * Sets the event listener used for SLA logging.
    *
    * @param listener the listener to set
    * @param context the context to set
    */
   void setEventListener (HttpConnectorEventListener listener,
         ConnectorContext context);

   /**
    * Sets request header to send and response header to validate.
    * @param header the header object
    */
   void setRequestResponseHeader (HttpRequestResponseHeader header);
}
