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

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.jcoderz.commons.ArgumentMalformedException;
import org.jcoderz.commons.util.Assert;
import org.jcoderz.commons.util.Constants;
import org.jcoderz.commons.util.HexUtil;
import org.jcoderz.commons.util.StringUtil;



/**
 * This class implements the HttpConnectionInterface using the Jakarta
 * commons-httpclient library.
 *
 */
public class HttpClientConnectionImpl
      implements HttpClientConnection
{
   /** The class name used for logging */
   private static final String CLASSNAME
         = HttpClientConnectionImpl.class.getName();
   /** The logger in use */
   private static final Logger logger
         = Logger.getLogger(CLASSNAME);
   /** The default port used for SSL connections */
   private static final int DEFAULT_SSL_PORT = 443;
   /** Constant for line feed. */
   private static final String LINE_FEED = "\n";
   /** Constant for the HTTP result code 200 */
   private static final int HTTP_RESULT_OK = 200;
   /** Contant for the HTTP result code 300 */
   private static final int HTTP_REDIRECT = 300;
   /** Constant for the HTTP result code 400 */
   private static final int HTTP_BAD_REQUEST = 400;
   /** Constant for the HTTP result code 500 */
   private static final int HTTP_INTERNAL_SERVER_ERROR = 500;
   /** The HttpClient defined in the 3rd party library. */
   private HttpClient mHttpClient;
   /** The HostConfiguration defined in the 3rd party library. */
   private HostConfiguration mHostConfiguration;
   /** The PostMethod defined in the 3rd party library. */
   private PostMethod mPostMethod;
   /** The state identifying the current step of the HTTP send/receive
      process. */
   /** Input stream for the request message used for the httpclient */
   private InputStream mRequestBodyInputStream = null;
   /** Content length of the request message */
   private int mRequestContentLength = 0;
   /** Flag indicating if the content length has been set via interface method.
       If false the content length will be calculated by the httpclient. */
   private boolean mIsRequestContentLengthSet = false;
   /** Content type of the request message */
   private String mRequestContentType = null;
   /** Current state of the httpclient. Used to check the correct usage of the
       interface methods defined within HttpClientConnection interface. */
   private HttpConnectionState mState
         = HttpConnectionState.CONNECTION_NOT_ESTABLISHED;
   /** Keystore filename used for SSL connections. Used to load the keystore
       from the filesystem if not given within interface method. */
   private String mKeyStoreFilename = null;
   /** Keystore password used for SSL connection. */
   private String mKeyStorePassword = null;
   /** Truststore filename used for SSL connections. Used to load the truststore
       from the filesystem if not given within interface method. */
   private String mTrustStoreFilename = null;
   /** Truststore password used for SSL connection. */
   private String mTrustStorePassword = null;
   /** Keystore used for SSL connections obtained via interface method. */
   private KeyStore mKeyStore = null;
   private KeyStore mTrustStore = null;
   /** Key alias of the sending system used for SSL connections. */
   private String mKeyAlias = null;
   /** Key password of the used key. */
   private String mKeyAliasPassword = null;
   /** Path used to load the keystore from file system if not given within
       interface method calls. */
   private String mPath = null;

   private HttpConnectorEventListener mHttpEventListener = null;
   private ConnectorContext mConnectorContext = null;
   private HttpRequestResponseHeader mRequestResponseHeader = null;

   /**
    * @see HttpClientConnection#establishConnection(String, int, int)
    */
   public void establishConnection (
      String uriAsString,
      int connectTimeout,
      int readTimeout)
   {
      Assert.notNull(uriAsString, "uriAsString");
      mHostConfiguration = new HostConfiguration();
      mPostMethod = new PostMethod();

      URI uri;
      try
      {
         uri = new URI(uriAsString, false);
         mPath = uri.getPathQuery();
         mPostMethod.setPath(mPath);

         final String scheme = uri.getScheme();
         if (scheme != null && scheme.toLowerCase(Constants.SYSTEM_LOCALE).
               equals("https"))
         {
            SslSocketFactory sslFactory = null;
            if (mKeyStore != null && mTrustStore != null)
            {
               // using the keystore given from signature service
               sslFactory = new SslSocketFactory(
                     mKeyStore, mTrustStore, mKeyAlias, mKeyAliasPassword);
            }
            else
            {
               // using a keystore loading from file system
               sslFactory = new SslSocketFactory(
                     mKeyStoreFilename, mKeyStorePassword,
                     mTrustStoreFilename, mTrustStorePassword,
                     mKeyAlias, mKeyAliasPassword);
            }
            final Protocol https
                  = new Protocol(
                        "https",
                        (ProtocolSocketFactory) sslFactory,
                        DEFAULT_SSL_PORT);
            int port = uri.getPort();
            if (port == -1)
            {
               port = DEFAULT_SSL_PORT;
            }
            final String host = uri.getHost();
            mHostConfiguration.setHost(host, port, https);
         }
         else
         {
            mHostConfiguration.setHost(uri);
         }
      }
      catch (URIException ue)
      {
         final ArgumentMalformedException ame = new ArgumentMalformedException(
            "uriAsString", uriAsString, ue.getMessage());
         throw ame;
      }
      catch (IllegalArgumentException iae)
      {
         final ArgumentMalformedException ame = new ArgumentMalformedException(
            "uriAsString", uriAsString, iae.getMessage());
         throw ame;
      }
      mHttpClient = new HttpClient();

      final SimpleHttpConnectionManager connectionManager
            = new SimpleHttpConnectionManager();
      final HttpConnectionManagerParams httpParams
            = new HttpConnectionManagerParams();
      httpParams.setConnectionTimeout(connectTimeout);
      httpParams.setSoTimeout(readTimeout);
      connectionManager.setParams(httpParams);
      mHttpClient.setHttpConnectionManager(connectionManager);
      mState = HttpConnectionState.CONNECTION_ESTABLISHED;
   }

   /**
    * @see HttpClientConnection#releaseConnection()
    */
   public void releaseConnection ()
   {
      if (mState == HttpConnectionState.CONNECTION_RELEASED)
      {
         return;
      }
      if (mState != HttpConnectionState.CONNECTION_ESTABLISHED
            && mState != HttpConnectionState.CONNECTION_EXECUTED)
      {
         final IllegalStateException ile = new IllegalStateException(
               "Connection must be established before");
         throw ile;
      }
      mPostMethod.releaseConnection();

      // depcrecated and will be removed in future releases
      mPostMethod.recycle();
      mState = HttpConnectionState.CONNECTION_RELEASED;
   }

   /**
    * @see HttpClientConnection#closeConnection()
    */
   public void closeConnection ()
   {
      if (mState != HttpConnectionState.CONNECTION_ESTABLISHED
            && mState != HttpConnectionState.CONNECTION_EXECUTED
            && mState != HttpConnectionState.CONNECTION_RELEASED)
      {
         final IllegalStateException ile = new IllegalStateException(
               "Connection must be established before");
         throw ile;
      }
      final HttpConnectionManager connManager
         = mHttpClient.getHttpConnectionManager();
      final HttpConnection connection
         = connManager.getConnection(mHostConfiguration);
      connection.close();
      mState = HttpConnectionState.CONNECTION_CLOSED;
   }

   /**
    * @see HttpClientConnection#execute()
    */
   public void execute ()
         throws HttpConnectionException
   {
      assertStateForExecute();
      setRequestHeader();
      mPostMethod.setRequestEntity(getRequestEntity());

      // reassign the path to the previously released post method
      if (mState == HttpConnectionState.CONNECTION_RELEASED)
      {
         mPostMethod.setPath(mPath);
      }
      //  dump request
      if (logger.isLoggable(Level.FINEST))
      {
         dumpRequestHeader();
      }
      int resultCode = 0;
      try
      {
         doCallbackRequestSend();
         resultCode = mHttpClient.executeMethod(
               mHostConfiguration, mPostMethod);
         doCallbackResponseReceived(resultCode, getResponseBody());
      }
      catch (HttpException he)
      {
         final HttpConnectionException hce = new HttpConnectionException(
               he.getMessage(), he);
         throw hce;
      }
      catch (ConnectException ce)
      {
         final HttpConnectConnectionException hce
               = new HttpConnectConnectionException(ce.getMessage(), ce);
         throw hce;
      }
      catch (IOException ioe)
      {
         handleIOExceptionWhilstExecute(ioe);
      }
      // dump response
      if (logger.isLoggable(Level.FINEST))
      {
         dumpResponse();
      }

      assertResultCode(resultCode);
      assertResponseHeader();
      mState = HttpConnectionState.CONNECTION_EXECUTED;
   }

   private void assertStateForExecute ()
   {
      if (mState != HttpConnectionState.CONNECTION_ESTABLISHED
            && mState != HttpConnectionState.CONNECTION_RELEASED)
      {
         final IllegalStateException ile = new IllegalStateException(
               "Connection must be established before or released");
         throw ile;
      }
   }

   private void handleIOExceptionWhilstExecute (
         IOException ioe)
         throws HttpConnectionException
   {
      final String message = ioe.getMessage().toLowerCase(
            Constants.SYSTEM_LOCALE);
      HttpConnectionException hce;
      if (message.equals("read timed out"))
      {
         hce = new HttpTimeoutConnectionException(ioe.getMessage(), ioe);
      }
      else
      {
         hce = new HttpConnectionException(ioe.getMessage(), ioe);
      }
      throw hce;
   }

   /**
    * @see HttpClientConnection#setRequestBody(java.io.InputStream)
    */
   public void setRequestBody (InputStream in)
   {
      mRequestBodyInputStream = in;
   }

   /**
    *
    * @see HttpClientConnection#getResponseBody()
    */
   public byte[] getResponseBody ()
         throws HttpEmptyResponseException
   {
      final byte[] result;
      try
      {
         result = mPostMethod.getResponseBody();
      }
      catch (IOException ioe)
      {
         final HttpEmptyResponseException ere
               = new HttpEmptyResponseException(
                     "IOException while obtaining response body", ioe);
         throw ere;
      }

      if (result == null || result.length == 0)
      {
         final HttpEmptyResponseException ere
            = new HttpEmptyResponseException(
               "Received empty http body in response");
         throw ere;
      }
      return result;
   }

   /**
    * @see HttpClientConnection#getResponseHeader(java.lang.String)
    */
   public String getResponseHeader (String key)
   {
      String result = null;
      final Header header = mPostMethod.getResponseHeader(key);

      if (header != null)
      {
         result = (header.getElements())[0].getName();
      }
      return result;
   }

   /**
    * @see HttpClientConnection#initSsl(java.lang.String, java.lang.String)
    */
   public void initSsl (
         String keyAlias,
         String keyAliasPassword)
   {
      Assert.notNull(keyAlias, "keyAlias");
      Assert.notNull(keyAliasPassword, "keyAliasPassword");
      mKeyStoreFilename = System.getProperty(
            "javax.net.ssl.keyStore");
      mKeyStorePassword = System.getProperty(
            "javax.net.ssl.keyStorePassword");
      mTrustStoreFilename = System.getProperty(
            "javax.net.ssl.trustStore");
      mTrustStorePassword = System.getProperty(
            "javax.net.ssl.trustStorePassword");
      mKeyStore = null;
      mTrustStore = null;
      mKeyAlias = keyAlias;
      mKeyAliasPassword = keyAliasPassword;
   }

   /**
    * @see HttpClientConnection#initSsl(KeyStore, KeyStore, String, String)
    */
   public void initSsl (
         KeyStore keyStore,
         KeyStore trustStore,
         String keyAlias,
         String keyAliasPassword)
   {
      Assert.notNull(keyStore, "keyStore");
      Assert.notNull(trustStore, "trustStore");
      Assert.notNull(keyAlias, "keyAlias");
      Assert.notNull(keyAliasPassword, "keyAliasPassword");
      mKeyStore = keyStore;
      mTrustStore = trustStore;
      mKeyAlias = keyAlias;
      mKeyAliasPassword = keyAliasPassword;
   }

   /**
    * @see org.jcoderz.commons.connector.http.transport.HttpClientConnection#setEventListener(org.jcoderz.commons.connector.http.transport.HttpConnectorEventListener, org.jcoderz.commons.connector.http.transport.ConnectorContext)
    */
   public void setEventListener (HttpConnectorEventListener listener,
         ConnectorContext context)
   {
      mHttpEventListener = listener;
      mConnectorContext = context;
   }

   /**
    * @see org.jcoderz.commons.connector.http.transport.HttpClientConnection#setRequestResponseHeader(org.jcoderz.commons.connector.http.transport.HttpRequestResponseHeader)
    */
   public void setRequestResponseHeader (HttpRequestResponseHeader header)
   {
      mRequestResponseHeader = header;
   }

   /**
    * Prepares the InputStream including the http request for the
    * httpclient.
    *
    * @return InputStreamRequestEntity the InputStream used by httpclient
    */
   private InputStreamRequestEntity getRequestEntity ()
   {
      InputStreamRequestEntity entity = null;
      if (mIsRequestContentLengthSet && mRequestContentType != null)
      {
         entity = new InputStreamRequestEntity(mRequestBodyInputStream,
            mRequestContentLength, mRequestContentType);
      }
      else if (mIsRequestContentLengthSet)
      {
         entity = new InputStreamRequestEntity(mRequestBodyInputStream,
               mRequestContentLength);
      }
      else if (mRequestContentType != null)
      {
         entity = new InputStreamRequestEntity(mRequestBodyInputStream,
               mRequestContentType);
      }
      else
      {
         entity = new InputStreamRequestEntity(mRequestBodyInputStream);
      }
      return entity;
   }

   /**
    * Sets the request header included in the HttpRequestResponseHeader object.
    */
   private void setRequestHeader ()
   {
      if (mRequestResponseHeader != null)
      {
         final Map header
               = new HashMap(mRequestResponseHeader.getRequestHeader());
         if (!header.containsKey("Content-Type"))
         {
            // default Content-Type
            header.put("Content-Type", "text/xml; charset=ISO-8859-1");
         }
         for (final Iterator it = header.keySet().iterator(); it.hasNext();)
         {
            final String key = (String) it.next();
            final String value = (String) header.get(key);
            mPostMethod.setRequestHeader(key, value);

            if (key.equals("Content-Length"))
            {
               setRequestContentLength(Integer.parseInt(value));
            }
            else if (key.equals("Content-Type"))
            {
               mRequestContentType = value;
            }
         }
      }
   }

   /**
    * Sets the content lenght of the request.
    * @param contentLength the length to set
    */
   private void setRequestContentLength (int contentLength)
   {
      mRequestContentLength = contentLength;
      mIsRequestContentLengthSet = true;
   }

   /**
    * Checks the http status code in the response message.
    * Throws appropriate HttpConnectionException if result code is
    * not 200 (OK).
    *
    * @param resultCode the http result code received in response
    * @throws HttpConnectionException to indicate failure on http level
    */
   private void assertResultCode (int resultCode)
         throws HttpConnectionException
   {
      if (resultCode != HTTP_RESULT_OK)
      {
         HttpConnectionException hce;
         if (resultCode >= HTTP_BAD_REQUEST
               && resultCode < HTTP_INTERNAL_SERVER_ERROR)
         {  // HTTP 4xx
            hce = new HttpClientConnectionException(
                     "HTTP Result Code of range 4xx in response",
                     null);
         }
         else if (resultCode >= HTTP_INTERNAL_SERVER_ERROR)
         {  // HTTP 5xx
            hce = new HttpServerConnectionException(
                     "HTTP Result Code of range 5xx in response",
                     null);
         }
         else if (resultCode >= HTTP_REDIRECT
               && resultCode < HTTP_BAD_REQUEST)
         {  // HTTP 3xx
            hce = new HttpServerConnectionException(
                     "HTTP redirect not supported",
                     null);
         }
         else
         {
            hce = new HttpServerConnectionException(
                     "Unsupported HTTP Result Code in response",
                     null);
         }
         hce.setStatusCode(resultCode);
         hce.setHttpMessage(StringUtil.asciiToString(getResponseBody()));

         throw hce;
      }
   }

   /**
    * Checks if the received http header in response match the
    * response header defined in the given HttpRequestResponseHeader object.
    *
    * @throws HttpInvalidResponseHeaderException if one or more header
    *          values are not like expected
    */
   private void assertResponseHeader ()
         throws HttpInvalidResponseHeaderException
   {
      if (mRequestResponseHeader != null)
      {
         logger.finest(mRequestResponseHeader.toString());
         final Map expectedResponseHeader
               = mRequestResponseHeader.getResponseHeader();
         final StringBuffer message = new StringBuffer();
         final Map invalidHeaders = new HashMap();
         for (final Iterator it = expectedResponseHeader.keySet().iterator();
               it.hasNext();)
         {
            final String key = (String) it.next();
            final String value = (String) expectedResponseHeader.get(key);
            final String responseValue = getResponseHeader(key);
            if (responseValue == null)
            {
               if (message.length() == 0)
               {
                  message.append("One or more invalid header");
                  message.append(" values detected in response:\n");
               }
               message.append("Expected response header <");
               message.append(key);
               message.append("> not received\n");
               invalidHeaders.put(key, null);
            }
            else if (!responseValue.equalsIgnoreCase(value))
            {
               if (message.length() == 0)
               {
                  message.append("One or more invalid header");
                  message.append(" values detected in response:\n");
               }
               message.append("Value for <");
               message.append(key);
               message.append("> is '");
               message.append(responseValue);
               message.append("' expected was '");
               message.append(value);
               message.append("'\n");
               invalidHeaders.put(key, responseValue);
            }
         } // end for

         if (message.length() != 0)
         {
            final HttpInvalidResponseHeaderException ihe
                  = new HttpInvalidResponseHeaderException(
                        message.toString(), invalidHeaders);

            try
            {
               ihe.setHttpMessage(HexUtil.dump(mPostMethod.getResponseBody()));
            }
            catch (IOException ignore)
            {
               // ignore
            }
            throw ihe;
         }
      }
      else
      {
         logger.finest("No response header for validating set");
      }
   }

   private void doCallbackRequestSend ()
   {
      if (mHttpEventListener != null)
      {
         mHttpEventListener.requestSend(mConnectorContext);
      }
   }

   private void doCallbackResponseReceived (int resultCode, byte[] responseData)
   {
      if (mHttpEventListener != null)
      {
         mHttpEventListener.responseReceived(
               resultCode, responseData, mConnectorContext);
      }
   }

   /**
    * Dumps the request message.
    */
   private void dumpRequestHeader ()
   {
      final StringBuffer dumpBuffer = new StringBuffer();
      dumpBuffer.append("\n-------------------------DUMP REQUEST HEADER---\n");
      dumpBuffer.append("Host: ");
      dumpBuffer.append(mHostConfiguration.getHost());
      dumpBuffer.append(LINE_FEED);
      dumpBuffer.append("Port: ");
      dumpBuffer.append(mHostConfiguration.getPort());
      dumpBuffer.append(LINE_FEED);
      dumpBuffer.append("Path: ");
      dumpBuffer.append(mPostMethod.getPath());
      dumpBuffer.append(LINE_FEED);
      dumpBuffer.append(LINE_FEED);
      dumpBuffer.append("Header:\n");
      final Header[] headers = mPostMethod.getRequestHeaders();
      for (int i = 0; i < headers.length; i++)
      {
         final String header = headers[i].toString();
         dumpBuffer.append(header);
      }
      dumpBuffer.append(LINE_FEED);
      dumpBuffer.append("-----------------------------------------");
      logger.finest(dumpBuffer.toString());
   }

   /**
    * Dumps the response message.
    */
   private void dumpResponse ()
   {
      final StringBuffer dumpBuffer = new StringBuffer();
      dumpBuffer.append("\n-------------------------DUMP RESPONSE---\n");
      dumpBuffer.append("StatusLine: ");
      dumpBuffer.append(mPostMethod.getStatusLine().toString());
      dumpBuffer.append(LINE_FEED);
      dumpBuffer.append(LINE_FEED);
      dumpBuffer.append("Header:\n");
      final Header[] headers = mPostMethod.getResponseHeaders();
      for (int i = 0; i < headers.length; i++)
      {
         final String header = headers[i].toString();
         dumpBuffer.append(header);
      }
      dumpBuffer.append(LINE_FEED);
      dumpBuffer.append("Body: \n");
      byte[] response = null;
      try
      {
         response = mPostMethod.getResponseBody();
      }
      catch (IOException ignore)
      {
         // ignore
      }
      if (response != null)
      {
         dumpBuffer.append(HexUtil.dump(response));
      }
      else
      {
         dumpBuffer.append("no response body available");
      }
      dumpBuffer.append(LINE_FEED);
      dumpBuffer.append("-----------------------------------------");
      logger.finest(dumpBuffer.toString());
   }
}
