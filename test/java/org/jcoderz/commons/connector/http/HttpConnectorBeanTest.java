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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jcoderz.commons.TestCase;
import org.jcoderz.commons.connector.ConnectionTimeoutErrorException;
import org.jcoderz.commons.connector.ConnectorException;
import org.jcoderz.commons.connector.http.transport.HttpEmptyResponseException;
import org.jcoderz.commons.connector.http.transport.HttpInvalidResponseHeaderException;
import org.jcoderz.commons.connector.http.transport.HttpRequestResponseHeader;
import org.jcoderz.commons.types.Url;



/**
 * JUnit remote (bean) tests for the commons http connector.
 *
 */
public class HttpConnectorBeanTest
      extends TestCase
{
   private static final String URL_SUBWAYCA
         = "http://subwayca/php3/index.php3";
   private static final String URL_SIMPLE_SERVER
   //      = "http://localhost:55555/index.html?Gruss=Hallo&Wort2=Welt";
         = "http://" + TestCase.getHostName() + ":55555";

   private HttpConnectorSessionInterface mBean = null;

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
         suite = getSuite(HttpConnectorBeanTest.class);
      }
      else
      {
         suite = new TestSuite(HttpConnectorBeanTest.class);
      }

      final Test setup = new HttpConnectorBeanTestSetup(suite);
      return setup;
   }

   /**
    * @see junit.framework.TestCase#setUp()
    */
   protected void setUp ()
         throws Exception
   {
      super.setUp();
      mBean = HttpConnectorSessionJNDIUtil.getHome().create();
   }

   protected void tearDown ()
         throws Exception
   {
      super.tearDown();
      mBean = null;
   }

   /**
    * Tests a simple connection to the subwayca.
    * @throws Exception in case of any error
    */
   public void testConnectingSubwayCA ()
         throws Exception
   {
      final Url url = Url.fromString(URL_SUBWAYCA);
      final ConnectorResponse response = mBean.sendAndReceive(
            getName().getBytes(), url, null);
      assertNotNull("Response object is null", response);
      assertTrue("Response message is empty",
            response.getResponse().length > 0);
      assertTrue("Request time not calculated", response.getRequestTime() > 0);
      assertTrue("Retries not expected", response.getTries() == 1);
   }

   /**
    * Tests a simple connection to the simple server
    * started whilst setup.
    * @throws Exception in case of any error
    */
   public void testConnectingSimpleServer ()
         throws Exception
   {
      final Url url = Url.fromString(URL_SIMPLE_SERVER);
      final ConnectorResponse response = mBean.sendAndReceive(
            getName().getBytes(), url, null);
      assertNotNull("Response object is null", response);
      assertTrue("Response message is empty",
            response.getResponse().length > 0);
      assertTrue("Request time not calculated", response.getRequestTime() > 0);
   }

   /**
    * Tests a connection to the simple server started whilst setup
    * including a header to set and an expected header in response.
    * @throws Exception in case of any error
    */
   public void testConnectingSimpleServerWithExpectedResponseHeaderSuccess ()
         throws Exception
   {
      final Url url = Url.fromString(URL_SIMPLE_SERVER);
      final HttpRequestResponseHeader header = new HttpRequestResponseHeader();
      header.addRequestHeader("ECHO_ExpectedHeader", "value");
      header.addResponseHeader("ExpectedHeader", "value");
      final ConnectorResponse response = mBean.sendAndReceive(
            getName().getBytes(), url, header);
      assertNotNull("Response object is null", response);
      assertTrue("Response message is empty",
            response.getResponse().length > 0);
      assertTrue("Request time not calculated", response.getRequestTime() > 0);
   }

   /**
    * Tests a connection to the simple server started whilst setup
    * including a header to validate a parameter in response.
    * The parameter to validate failed.
    * @throws Exception in case of any error
    */
   public void testConnectingSimpleServerWithExpectedResponseHeaderFailed ()
         throws Exception
   {
      final Url url = Url.fromString(URL_SIMPLE_SERVER);
      final HttpRequestResponseHeader header = new HttpRequestResponseHeader();
      header.addResponseHeader("Content-Type", "text/plain");
      ConnectorResponse response;
      try
      {
         response = mBean.sendAndReceive(
               getName().getBytes(), url, header);
         fail("Expected 'HttpInvalidResponseHeaderException' "
               + " with invalid value for 'Content-Type' in response");
      }
      catch (ConnectorException ce)
      {
         if (ce.getCause() instanceof HttpInvalidResponseHeaderException)
         {
            // expected
            final HttpInvalidResponseHeaderException expected
                  = (HttpInvalidResponseHeaderException) ce.getCause();
            assertTrue("Content-Type not invalid in response",
                  expected.getInvalidParameter().containsKey("Content-Type"));
         }
         else
         {
            fail("Not the expected exception: " + ce.getMessage());
         }
      }
   }

   /**
    * Tests a connection to the simple server started whilst setup
    * with an empty message body received in response.
    *
    * @throws Exception in case of any error
    */
   public void testConnectingSimpleServerWithEmptyResponseBody ()
         throws Exception
   {
      final Url url = Url.fromString(URL_SIMPLE_SERVER);
      final HttpRequestResponseHeader header = new HttpRequestResponseHeader();
      header.addRequestHeader("UseEmptyResponse", "true");
      ConnectorResponse response;
      try
      {
         response = mBean.sendAndReceive(
               getName().getBytes(), url, header);
         fail("Expected 'HttpEmptyResponseException'");
      }
      catch (ConnectorException ce)
      {
         if (!(ce.getCause() instanceof HttpEmptyResponseException))
         {
            fail("Not the expected exception: " + ce.getCause().getMessage());
         }
      }
   }

   /**
    * Tests a timeout whilst waiting for a response from the simple server.
    * @throws Exception in case of any error
    */
   public void testConnectingSimpleServerWithTimeout ()
         throws Exception
   {
      final Url url = Url.fromString(URL_SIMPLE_SERVER);
      final HttpRequestResponseHeader header = new HttpRequestResponseHeader();
      header.addRequestHeader("DoNotRespond", "True");
      ConnectorResponse response;
      try
      {
         response = mBean.sendAndReceive(
               getName().getBytes(), url, header);
         fail("Expected 'TimeoutException'");
      }
      catch (ConnectionTimeoutErrorException expected)
      {
         // expected
      }
      catch (ConnectorException ce)
      {
         fail("Not the expected exception: " + ce.getMessage());
      }
   }

}

