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

import java.io.ByteArrayInputStream;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jcoderz.commons.ArgumentMalformedException;
import org.jcoderz.commons.TestCase;
import org.jcoderz.commons.util.Constants;
import org.jcoderz.commons.util.StringUtil;



/**
 * This class test the implementation of the HttpConnectionInterface
 * provided via the Jakarta commons-httpclient project.
 * @author anonymous
 */
public class HttpClientConnectionTest
      extends TestCase
{
   private static final int CONNECT_TIMEOUT = 5000;
   private static final int READ_TIMEOUT = 5000;
   private static final String FILE_SEPARATOR
         = System.getProperty("file.separator");
   private static final String
         DEFAULT_URL = "http://localhost:" 
             + HttpClientConnectionTestSetup.DEFAULT_SERVER_PORT;
   private static final byte[]
         SIMPLE_BODY = "This is a simple POST request".getBytes();
   private static final String
         ECHO_SIMPLE_BODY = "Echo:This is a simple POST request";
   private static final String
         CONTENT_TYPE_PARAMETER = "Content-Type";
   private static final String
         CONTENT_TYPE_PARAMETER_VALUE = "text/xml; charset=ISO-8859-1";
   private static final String
         CONNECTION_PARAMETER = "Connection";
   private static final String
         CONNECTION_PARAMETER_VALUE_CLOSE = "Close";
   private static final String
         CONNECTION_PARAMETER_VALUE_KEEPALIVE = "Keep-Alive";
   private static final String
         HTTPCONNECTIONEXCEPTION = "HttpConnectionException: ";
   private static final String
         ILLEGALSTATEEXCEPTION = "IllegalStateException: ";
   private static final String
         NO_MESSAGE_BODY_IN_RESPONSE = "No message body in response";
   private static final String
         CONNECTION_CLOSE_EXPECTED = "Connection close expected";
   private static final String
         RESPONSE_BODY_NOT_LIKE_EXPECTED
            = "Response body not like expected";
   private static final String
         ATTRIBUTE_CONNECTION_MISSING
            = "No attribute \'Connection\'";
   private static final String
         CONNECTION_MUST_BE_ESTABLISHED_OR_RELEASED
            = "Connection must be established before or released";
   private static final String
         ILLEGAL_STATE_MESSAGE_NOT_LIKE_EXPECTED
            = "Illegal State message not like expected";
   private static final String
         ILLEGALSTATEEXCEPTION_EXPECTED
            = "IllegalStateException expected";

   /** The interface implementation in use. */
   private HttpClientConnection mHttpConnection;


   /**
    * Main.
    *
    * @param args main arguments
    */
   public static void main (
         String[] args)
   {
      junit.textui.TestRunner.run(suite());
   }

   /**
    * Creates the testsuite.
    * @return Test
    *          the testsuite
    */
   public static Test suite ()
   {
      TestSuite suite = null;
      if (hasTestCases())
      {
         suite = getSuite(HttpClientConnectionTest.class);
      }
      else
      {
         suite = new TestSuite(HttpClientConnectionTest.class);
      }

      final Test setup = new HttpClientConnectionTestSetup(suite);
      return setup;
   }

   /**
    * Setup creates a new interface implementation object.
    * @see TestCase#setUp()
    */
   protected void setUp ()
         throws Exception
   {
      super.setUp();
      mHttpConnection = new HttpClientConnectionImpl();
   }

   /**
    * TearDown of the TestCase.
    * @see TestCase#tearDown()
    */
   protected void tearDown ()
         throws Exception
   {
      super.tearDown();
   }

   /**
    * Performs establishing a connection, creating a request, sending that
    * request, receiving the response and closing the connection.
    * @throws Exception for any unexpected error
    */
   public void testSendSimplePostRequest ()
         throws Exception
   {
      // connect
      mHttpConnection.establishConnection(
            DEFAULT_URL,
            CONNECT_TIMEOUT, READ_TIMEOUT);
      mHttpConnection.setRequestBody(
            new ByteArrayInputStream(SIMPLE_BODY));
      final HttpRequestResponseHeader header = new HttpRequestResponseHeader();
      header.addRequestHeader(
            CONTENT_TYPE_PARAMETER, CONTENT_TYPE_PARAMETER_VALUE);
      header.addRequestHeader(
            CONNECTION_PARAMETER, CONNECTION_PARAMETER_VALUE_CLOSE);
      mHttpConnection.setRequestResponseHeader(header);

      // execute
      mHttpConnection.execute();
      final String response = getResponseBodyAsString();
      final String connectionValue
            = mHttpConnection.getResponseHeader(CONNECTION_PARAMETER);

      // close
      mHttpConnection.closeConnection();

      assertNotNull(NO_MESSAGE_BODY_IN_RESPONSE, response);
      assertEquals(RESPONSE_BODY_NOT_LIKE_EXPECTED, ECHO_SIMPLE_BODY, response);
      assertNotNull(ATTRIBUTE_CONNECTION_MISSING, connectionValue);
      assertEquals(CONNECTION_CLOSE_EXPECTED,
            "close",
            connectionValue.toLowerCase(Constants.SYSTEM_LOCALE));
   }

   /**
    * Sends post request without a waiting server.
    * @throws Exception if the test case fails with an exception.
    */
   public void testSendSimplePostRequestToNotExistingTarget ()
       throws Exception
   {
      //    connect
      mHttpConnection.establishConnection(
            "http://localhost:23",
            CONNECT_TIMEOUT, READ_TIMEOUT);
      mHttpConnection.setRequestBody(
            new ByteArrayInputStream(SIMPLE_BODY));
      final HttpRequestResponseHeader header = new HttpRequestResponseHeader();
      header.addRequestHeader(
            CONTENT_TYPE_PARAMETER, CONTENT_TYPE_PARAMETER_VALUE);
      header.addRequestHeader(
            CONNECTION_PARAMETER, CONNECTION_PARAMETER_VALUE_CLOSE);
      mHttpConnection.setRequestResponseHeader(header);

      // execute
      try
      {
         mHttpConnection.execute();
         fail("IOException expected");
      }
      catch (HttpConnectConnectionException expected)
      {
        // expected
      }
      // close
      mHttpConnection.closeConnection();
   }

   /**
    * Tests if a response header key value pair set for the connection
    * has been evaluated correctly.
    *
    * @throws Exception in case of any unexpected error
    */
   public void testSendSimplePostRequestWithExpectedResponseHeaderSuccess ()
         throws Exception
   {
      // connect
      mHttpConnection.establishConnection(
            DEFAULT_URL,
            CONNECT_TIMEOUT, READ_TIMEOUT);
      mHttpConnection.setRequestBody(
            new ByteArrayInputStream(SIMPLE_BODY));
      final HttpRequestResponseHeader header = new HttpRequestResponseHeader();
      header.addRequestHeader(
            CONTENT_TYPE_PARAMETER, CONTENT_TYPE_PARAMETER_VALUE);
      header.addRequestHeader(
            "ECHO_ResponseHeader1", "expected1");
      header.addResponseHeader(
            "ResponseHeader1", "expected1");
      mHttpConnection.setRequestResponseHeader(header);

      // execute
      mHttpConnection.execute();
      final String response = getResponseBodyAsString();
      final String connectionValue
            = mHttpConnection.getResponseHeader(CONNECTION_PARAMETER);
      final String expectedValue
            = mHttpConnection.getResponseHeader("ResponseHeader1");

      // close
      mHttpConnection.closeConnection();

      assertNotNull(NO_MESSAGE_BODY_IN_RESPONSE, response);
      assertEquals(RESPONSE_BODY_NOT_LIKE_EXPECTED, ECHO_SIMPLE_BODY, response);
      assertNotNull(ATTRIBUTE_CONNECTION_MISSING, connectionValue);
      assertNotNull("Expected response header missing", expectedValue);
      assertEquals("Value of expected response header not like expected - ",
            "expected1",
            expectedValue.toLowerCase(Constants.SYSTEM_LOCALE));
   }

   /**
    * Tests if the correct exception is thrown if evaluating a response header
    * key value pair set for the connection has been failed.
    *
    * @throws Exception in case of any unexpected error
    */
   public void testSendSimplePostRequestWithExpectedResponseHeaderFailed ()
         throws Exception
   {
      // connect
      mHttpConnection.establishConnection(
            DEFAULT_URL,
            CONNECT_TIMEOUT, READ_TIMEOUT);
      mHttpConnection.setRequestBody(
            new ByteArrayInputStream(SIMPLE_BODY));
      final HttpRequestResponseHeader header = new HttpRequestResponseHeader();
      header.addRequestHeader(
            CONTENT_TYPE_PARAMETER, CONTENT_TYPE_PARAMETER_VALUE);
      header.addRequestHeader(
            "ECHO_ResponseHeader2", "expected2");
      header.addResponseHeader(
            "ResponseHeader1", "expected2");
      header.addResponseHeader(
            "ResponseHeader2", "wrong value");
      mHttpConnection.setRequestResponseHeader(header);

      // execute
      try
      {
         mHttpConnection.execute();
         fail("Exception expected regarding response header");
      }
      catch (HttpInvalidResponseHeaderException expected)
      {
         // expected
      }
   }

   /**
    * Tests if the correct exception is thrown if the http response
    * body received is empty.
    *
    * @throws Exception in case of any unexpected error
    */
   public void testSendSimplePostRequestWithEmptyResponseBody ()
         throws Exception
   {
      // connect
      mHttpConnection.establishConnection(
            DEFAULT_URL,
            CONNECT_TIMEOUT, READ_TIMEOUT);
      mHttpConnection.setRequestBody(
            new ByteArrayInputStream(SIMPLE_BODY));
      final HttpRequestResponseHeader header = new HttpRequestResponseHeader();
      header.addRequestHeader(
            CONTENT_TYPE_PARAMETER, CONTENT_TYPE_PARAMETER_VALUE);
      header.addRequestHeader(
            "UseEmptyResponse", "true");
      mHttpConnection.setRequestResponseHeader(header);

      // execute
      try
      {
         mHttpConnection.execute();
         getResponseBodyAsString();
         fail("'HttpEmptyResponseException' expected");
      }
      catch (HttpEmptyResponseException expected)
      {
         // expected
      }

      // close
      mHttpConnection.closeConnection();
   }

   /**
    * Sends a request and expects no response.
    * @throws Exception for any unexpected error
    */
   public void testSendSimplePostRequestWithoutResponse ()
         throws Exception
   {
      // connect
      mHttpConnection.establishConnection(
            DEFAULT_URL,
            CONNECT_TIMEOUT, READ_TIMEOUT);
      mHttpConnection.setRequestBody(new ByteArrayInputStream(SIMPLE_BODY));
      final HttpRequestResponseHeader header = new HttpRequestResponseHeader();
      header.addRequestHeader(
            CONTENT_TYPE_PARAMETER, CONTENT_TYPE_PARAMETER_VALUE);
      header.addRequestHeader(
            CONNECTION_PARAMETER, CONNECTION_PARAMETER_VALUE_KEEPALIVE);
      header.addRequestHeader(
            "DoNotRespond", "True");
      mHttpConnection.setRequestResponseHeader(header);

      // execute
      byte[] response = null;
      try
      {
         mHttpConnection.execute();
         response = mHttpConnection.getResponseBody();
         fail("'HttpTimeoutException' expected.");
      }
      catch (HttpTimeoutConnectionException he)
      {
         assertNull("No response message expected", response);
      }
      // close
      mHttpConnection.closeConnection();
   }

   /**
    * Sends a request and expects a immediate close on server side.
    * @throws Exception for any unexpected errors
    */
   public void testSendSimplePostRequestWithImmediateClose ()
         throws Exception
   {
      // connect
      mHttpConnection.establishConnection(
            DEFAULT_URL,
            CONNECT_TIMEOUT, READ_TIMEOUT);
      mHttpConnection.setRequestBody(new ByteArrayInputStream(SIMPLE_BODY));
      final HttpRequestResponseHeader header = new HttpRequestResponseHeader();
      header.addRequestHeader(
            CONTENT_TYPE_PARAMETER, CONTENT_TYPE_PARAMETER_VALUE);
      header.addRequestHeader(
            CONNECTION_PARAMETER, CONNECTION_PARAMETER_VALUE_KEEPALIVE);
      header.addRequestHeader(
            "DoImmediateClose", "True");
      mHttpConnection.setRequestResponseHeader(header);

      // execute
      byte[] response = null;
      try
      {
         mHttpConnection.execute();
         response = mHttpConnection.getResponseBody();
         fail("HttpConnectionException expected");
      }
      catch (HttpConnectionException he)
      {
         assertNull("No response message expected", response);
      }

      // close
      mHttpConnection.closeConnection();
   }

   /**
    * Sends a request with Connection=close and receives the
    * response.
    * @throws Exception for any unexpected error
    */
   public void testSendMultiplePostRequestAfterServerClose ()
         throws Exception
   {
      // connect
      mHttpConnection.establishConnection(
            DEFAULT_URL,
            CONNECT_TIMEOUT, READ_TIMEOUT);

      // set message 1
      mHttpConnection.setRequestBody(new ByteArrayInputStream(SIMPLE_BODY));
      HttpRequestResponseHeader header = new HttpRequestResponseHeader();
      header.addRequestHeader(
            CONTENT_TYPE_PARAMETER, CONTENT_TYPE_PARAMETER_VALUE);
      header.addRequestHeader(
            CONNECTION_PARAMETER, CONNECTION_PARAMETER_VALUE_CLOSE);
      mHttpConnection.setRequestResponseHeader(header);

      String response = null;
      String connectionValue = null;

      // execute message 1
      mHttpConnection.execute();
      response = getResponseBodyAsString();
      connectionValue = mHttpConnection.getResponseHeader(CONNECTION_PARAMETER);

      // check reponse 1
      assertNotNull(NO_MESSAGE_BODY_IN_RESPONSE, response);
      assertEquals(RESPONSE_BODY_NOT_LIKE_EXPECTED, ECHO_SIMPLE_BODY, response);
      assertNotNull(ATTRIBUTE_CONNECTION_MISSING, connectionValue);
      assertEquals(CONNECTION_CLOSE_EXPECTED,
            "close",
            connectionValue.toLowerCase(Constants.SYSTEM_LOCALE));

      // release connection
      mHttpConnection.releaseConnection();

      // set message 2
      mHttpConnection.setRequestBody(
            new ByteArrayInputStream(
                  "This is the second POST request".getBytes()));
      header = new HttpRequestResponseHeader();
      header.addRequestHeader(
            CONTENT_TYPE_PARAMETER, CONTENT_TYPE_PARAMETER_VALUE);
      header.addRequestHeader(
            CONNECTION_PARAMETER, CONNECTION_PARAMETER_VALUE_KEEPALIVE);
      mHttpConnection.setRequestResponseHeader(header);

      // execute message 2
      mHttpConnection.execute();
      response = getResponseBodyAsString();

      // release connection
      mHttpConnection.releaseConnection();

      // set message 3
      mHttpConnection.setRequestBody(
            new ByteArrayInputStream(
               "This is the third POST request".getBytes()));
      header = new HttpRequestResponseHeader();
      header.addRequestHeader(
            CONTENT_TYPE_PARAMETER, CONTENT_TYPE_PARAMETER_VALUE);
      header.addRequestHeader(
            CONNECTION_PARAMETER, CONNECTION_PARAMETER_VALUE_KEEPALIVE);
      mHttpConnection.setRequestResponseHeader(header);

      // execute message 3
      mHttpConnection.execute();
      response = getResponseBodyAsString();
      mHttpConnection.closeConnection();
   }

   /**
    * Tests establishing a connection. Checks one simple good case and several
    * cases that leads to an ArgumentMalformedException caused by an
    * unsufficient URL parameter.
    */
   public void testEstablishConnection ()
   {
      // simple connect success
      mHttpConnection.establishConnection(
            "http://subwayca/php3/index.php3",
            CONNECT_TIMEOUT, READ_TIMEOUT);
      mHttpConnection.closeConnection();
      //

      // "invalid" argument leads to ArgumentMalformedException
      final String invalidUrl = "malformed url";
      try
      {
         mHttpConnection.establishConnection(
               invalidUrl, CONNECT_TIMEOUT, READ_TIMEOUT);
         fail("ArgumentMalformedException expected");
      }
      catch (ArgumentMalformedException expected)
      {
         //expected
      }
      //

      // "null" argument leads to ArgumentMalformedException
      try
      {
         mHttpConnection.establishConnection(
               null, CONNECT_TIMEOUT, READ_TIMEOUT);
         fail("ArgumentMalformedException expected");
      }
      catch (ArgumentMalformedException expected)
      {
         // expected
      }
      //

      // useless timeout values
      mHttpConnection.establishConnection(
            "http://www.heise.de", -1, -1);

      // close
      mHttpConnection.closeConnection();
   }

   /**
    * Releases a connection multiple times.
    */
   public void testReleaseConnectionSuccessMultipleUse ()
   {
      // connect
      mHttpConnection.establishConnection(
            DEFAULT_URL,
            CONNECT_TIMEOUT, READ_TIMEOUT);
       // multiple release
      mHttpConnection.releaseConnection();
      mHttpConnection.releaseConnection();

      // close
      mHttpConnection.closeConnection();
   }

   /**
    * Tests the release of connection after sending a message and a sending anew
    * after releasing.
    * @throws Exception if the test case fails with an exception.
    */
   public void testReleaseConnectionSuccessWithTwoSendMessages ()
       throws Exception
   {
      // connect
      mHttpConnection.establishConnection(
            DEFAULT_URL,
            CONNECT_TIMEOUT, READ_TIMEOUT);

      // set message 1
      HttpRequestResponseHeader header = new HttpRequestResponseHeader();
      header.addRequestHeader(
            CONTENT_TYPE_PARAMETER, CONTENT_TYPE_PARAMETER_VALUE);
      header.addRequestHeader(
            "Message1", "Nachricht 1");
      mHttpConnection.setRequestResponseHeader(header);

      mHttpConnection.setRequestBody(
            new ByteArrayInputStream("This is message one".getBytes()));

      // send message 1
      mHttpConnection.execute();

      // release connection
      mHttpConnection.releaseConnection();

      // set message 2
      header = new HttpRequestResponseHeader();
      header.addRequestHeader(
            CONTENT_TYPE_PARAMETER, CONTENT_TYPE_PARAMETER_VALUE);
      header.addRequestHeader(
            CONNECTION_PARAMETER, CONNECTION_PARAMETER_VALUE_CLOSE);
      header.addRequestHeader(
            "Message2", "Nachricht 2");
      mHttpConnection.setRequestResponseHeader(header);

      // send message 2
      mHttpConnection.execute();

      // close
      mHttpConnection.closeConnection();
   }

   /**
    * Tests releasing the connection and sending a second message without
    * sending the first message.
    * @throws Exception if the test case fails with an exception.
    */
   public void testReleaseConnectionSuccessWithOneSendMessage ()
       throws Exception
   {
      //    connect
      mHttpConnection.establishConnection(
            DEFAULT_URL,
            CONNECT_TIMEOUT, READ_TIMEOUT);

      // set message 1
      HttpRequestResponseHeader header = new HttpRequestResponseHeader();
      header.addRequestHeader(
            CONTENT_TYPE_PARAMETER, CONTENT_TYPE_PARAMETER_VALUE);
      header.addRequestHeader(
            "Message1", "Nachricht 1");
      mHttpConnection.setRequestResponseHeader(header);

      mHttpConnection.setRequestBody(
            new ByteArrayInputStream("This is message one".getBytes()));

      // release connection
      mHttpConnection.releaseConnection();

      // set message 2
      header = new HttpRequestResponseHeader();
      header.addRequestHeader(
            CONTENT_TYPE_PARAMETER, CONTENT_TYPE_PARAMETER_VALUE);
      header.addRequestHeader(
            CONNECTION_PARAMETER, CONNECTION_PARAMETER_VALUE_CLOSE);
      header.addRequestHeader(
            "Message2", "Nachricht 2");
      mHttpConnection.setRequestResponseHeader(header);

      // send message 2
      mHttpConnection.execute();
   }

   /**
    * Tests error case with a missing "releaseConnection" call before reusing
    * an already opened connection.
    * @throws Exception if the test case fails with an exception.
    */
   public void testReleaseConnectionFailed ()
       throws Exception
   {
      // connect
      mHttpConnection.establishConnection(
            DEFAULT_URL,
            CONNECT_TIMEOUT, READ_TIMEOUT);

      // set message 1
      HttpRequestResponseHeader header = new HttpRequestResponseHeader();
      header.addRequestHeader(
            CONTENT_TYPE_PARAMETER, CONTENT_TYPE_PARAMETER_VALUE);
      header.addRequestHeader(
            "Message1", "Nachricht 1");
      mHttpConnection.setRequestResponseHeader(header);

      mHttpConnection.setRequestBody(
            new ByteArrayInputStream("This is message one".getBytes()));

      // send message 1
      mHttpConnection.execute();

      // set message 2
      header = new HttpRequestResponseHeader();
      header.addRequestHeader(
            CONTENT_TYPE_PARAMETER, CONTENT_TYPE_PARAMETER_VALUE);
      header.addRequestHeader(
            "Message2", "Nachricht 2");
      mHttpConnection.setRequestResponseHeader(header);

      // send message 2
      try
      {
         mHttpConnection.execute();
         fail(ILLEGALSTATEEXCEPTION_EXPECTED);
      }
      catch (IllegalStateException ise)
      {
         assertEquals(ILLEGAL_STATE_MESSAGE_NOT_LIKE_EXPECTED,
               CONNECTION_MUST_BE_ESTABLISHED_OR_RELEASED,
               ise.getMessage());
      }
      // close
      mHttpConnection.closeConnection();
   }

   /**
    * Release a not established connection.
    */
   public void testReleaseConnectionWithInvalidState ()
   {
      // invalid state
      try
      {
         mHttpConnection.releaseConnection();
         fail("IllegalStatException expected");
      }
      catch (IllegalStateException ise)
      {
         assertEquals(ILLEGAL_STATE_MESSAGE_NOT_LIKE_EXPECTED,
               "Connection must be established before",
               ise.getMessage());
      }
   }
    /**
    * Closes the connection after successful sending.
    * @throws Exception if the test case fails with an exception.
    */
   public void testCloseConnectionSuccessAfterSending ()
       throws Exception
   {
      mHttpConnection.establishConnection(
            DEFAULT_URL,
            CONNECT_TIMEOUT, READ_TIMEOUT);

      //    set message
      final HttpRequestResponseHeader header = new HttpRequestResponseHeader();
      header.addRequestHeader(
            CONTENT_TYPE_PARAMETER, CONTENT_TYPE_PARAMETER_VALUE);
      header.addRequestHeader(
            CONNECTION_PARAMETER, CONNECTION_PARAMETER_VALUE_CLOSE);
      mHttpConnection.setRequestResponseHeader(header);

      mHttpConnection.setRequestBody(
            new ByteArrayInputStream("This is the message".getBytes()));

      mHttpConnection.execute();
      mHttpConnection.closeConnection();
   }

   /**
    * Multiple closing fails.
    */
   public void testCloseConnectionFailed ()
   {
      // connection not established
      try
      {
         mHttpConnection.closeConnection();
         fail(ILLEGALSTATEEXCEPTION_EXPECTED);
      }
      catch (IllegalStateException ise)
      {
         assertEquals(ILLEGAL_STATE_MESSAGE_NOT_LIKE_EXPECTED,
               "Connection must be established before",
               ise.getMessage());
      }

      // closed twice
      mHttpConnection.establishConnection(
            "http://subwayca:80",
            CONNECT_TIMEOUT, READ_TIMEOUT);
      try
      {
         mHttpConnection.closeConnection();
         mHttpConnection.closeConnection();
         fail(ILLEGALSTATEEXCEPTION_EXPECTED);
      }
      catch (IllegalStateException ise)
      {
         assertEquals(ILLEGAL_STATE_MESSAGE_NOT_LIKE_EXPECTED,
               "Connection must be established before",
               ise.getMessage());
      }
   }

   /**
    * Executes connection without establishing before.
    * @throws Exception if the test case fails with an exception.
    */
   public void testExecuteFailed ()
       throws Exception
   {
      // not connected
      try
      {
         mHttpConnection.execute();
         fail(ILLEGALSTATEEXCEPTION_EXPECTED);
      }
      catch (IllegalStateException ise)
      {
         assertEquals(ILLEGAL_STATE_MESSAGE_NOT_LIKE_EXPECTED,
               CONNECTION_MUST_BE_ESTABLISHED_OR_RELEASED,
               ise.getMessage());
      }

      // executed twice
      mHttpConnection.establishConnection(
            DEFAULT_URL,
            CONNECT_TIMEOUT, READ_TIMEOUT);

      final HttpRequestResponseHeader header = new HttpRequestResponseHeader();
      header.addRequestHeader(
            CONTENT_TYPE_PARAMETER, CONTENT_TYPE_PARAMETER_VALUE);
      mHttpConnection.setRequestResponseHeader(header);

      mHttpConnection.setRequestBody(
            new ByteArrayInputStream("This is the message".getBytes()));

      try
      {
         mHttpConnection.execute();
         mHttpConnection.execute();
         fail(ILLEGALSTATEEXCEPTION_EXPECTED);
      }
      catch (IllegalStateException ise)
      {
         assertEquals(ILLEGAL_STATE_MESSAGE_NOT_LIKE_EXPECTED,
               CONNECTION_MUST_BE_ESTABLISHED_OR_RELEASED,
               ise.getMessage());
      }
      mHttpConnection.closeConnection();
   }

   /**
    * Performs establishing a connection (to SubwayCA) , creating a request,
    * sending that request, receiving the response and closing the connection
    * USING SSL.
    * @throws Exception
    *          in case of missing keystore
    */
   public void testSendSimplePostRequestWithSSL ()
         throws Exception
   {
      final String keyStoreFilename = getBaseDir()
            + FILE_SEPARATOR + "test" + FILE_SEPARATOR + "data"
            + FILE_SEPARATOR + "ssl_store.jks";
      final String trustStoreFilename = getBaseDir()
            + FILE_SEPARATOR + "test" + FILE_SEPARATOR + "data"
            + FILE_SEPARATOR + "fwk_trusted.jks";
      System.setProperty("javax.net.ssl.keyStore", keyStoreFilename);
      System.setProperty("javax.net.ssl.trustStore", trustStoreFilename);
      System.setProperty("javax.net.ssl.trustStorePassword", "fawkez42");
      System.setProperty("javax.net.ssl.keyStorePassword", "fawkez42");
      mHttpConnection.initSsl("ssl", "sslssl");

      // connect
      mHttpConnection.establishConnection(DEFAULT_URL,
            CONNECT_TIMEOUT, READ_TIMEOUT);
      mHttpConnection.setRequestBody(new ByteArrayInputStream(SIMPLE_BODY));

      final HttpRequestResponseHeader header = new HttpRequestResponseHeader();
      header.addRequestHeader(
            CONTENT_TYPE_PARAMETER, CONTENT_TYPE_PARAMETER_VALUE);
      header.addRequestHeader(
            CONNECTION_PARAMETER, CONNECTION_PARAMETER_VALUE_CLOSE);
      mHttpConnection.setRequestResponseHeader(header);

      // execute
      mHttpConnection.execute();
      final String response = getResponseBodyAsString();
      final String connectionValue
            = mHttpConnection.getResponseHeader(CONNECTION_PARAMETER);

      // close
      mHttpConnection.closeConnection();

      assertNotNull(NO_MESSAGE_BODY_IN_RESPONSE, response);
      assertNotNull(ATTRIBUTE_CONNECTION_MISSING, connectionValue);
      assertEquals(CONNECTION_CLOSE_EXPECTED,
            "close",
            connectionValue.toLowerCase(Constants.SYSTEM_LOCALE));
   }

   /**
    * Gets the response body as String.
    * @return String
    *          the response body as string
    */
   private String getResponseBodyAsString ()
         throws HttpEmptyResponseException
   {
      return StringUtil.toString(mHttpConnection.getResponseBody());
   }
}
