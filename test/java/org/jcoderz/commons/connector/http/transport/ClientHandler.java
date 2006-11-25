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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import org.jcoderz.commons.util.Constants;



/**
 * Handler for http client requests.
 */
public class ClientHandler
      extends Thread
{
   /** Class name used for logging. */
   private static final String CLASSNAME
         = ClientHandler.class.getName();
   /** Logger in use. */
   private static final Logger logger
         = Logger.getLogger(CLASSNAME);
   private static final int MAX_REQUESTS = 1000;
   private static final int SLEEP_TIME_BETWEEN_REQUESTS = 50;
   private static final int BUFFER_SIZE = 4096;
   private static final String NEW_LINE = "\n";
   private static final String UTF8 = "UTF-8";
   private final Socket mIncoming;
   private final int mCounter;
   private InputStream mInStream = null;
   private OutputStream mOutStream = null;
   private boolean mHaveToStop = false;

   // request
   private String mRequestLine;
   private final Map mRequestParameter = new HashMap();
   private String mRequestBody;
   private String mRequestContentType;
   private boolean mConnectionHaveToBeClosed = false;
   private boolean mDoNotRespond = false;
   private boolean mDoImmediateClose = false;
   private boolean mUseEmptyResponse = false;

   // response
   private String mResponseLine;
   private String mResponseBody;
   private final Map mResponseParameter = new HashMap();

   /**
    * Constructor.
    *
    * @param incoming
    *          the incoming socket
    * @param counter
    *          the number of handler
    */
   public ClientHandler (Socket incoming, int counter)
   {
      mIncoming = incoming;
      mCounter = counter;

      logger.info("+++ Creating Thread "
            + mCounter + "x"
            + " for Socket(" + incoming.getPort() + ")\n");
   }

   /**
    * Starting handler thread.
    */
   public void run ()
   {
      try
      {
         mInStream = mIncoming.getInputStream();
         mOutStream = mIncoming.getOutputStream();
         for (int i = 1; i < MAX_REQUESTS; i++)
         {
            if (!gotIncomingRequest())
            {
               break;
            }

            logger.info(
                  "+++ ITERATION " + i + "x Thread " + mCounter + "\n");
            try
            {
               // read response
               readRequest();

               // if the client wants an immediate close without response
               if (mDoImmediateClose)
               {
                  break;
               }

               // if the client do not want to receive a response
               if (!mDoNotRespond)
               {
                  writeResponse();
               }
            }
            catch (Exception ie)
            {
               logger.warning(
                     "Exception (" + ie.getMessage() + ") while read/write");
               ie.printStackTrace();
               break;
            }

            if (mConnectionHaveToBeClosed)
            {
               break;
            }
         } // end for
         mInStream.close();
         mOutStream.close();
         mIncoming.close();
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
      logger.info("+++ Terminating Thread " + mCounter + "x\n");
   }

   private boolean gotIncomingRequest ()
   {
      int bytesAvailable = 0;
      boolean handleRequest = true;
      for (;;)
      {
         try
         {
            bytesAvailable = mInStream.available();
         }
         catch (IOException ioe)
         {
            logger.warning(
                  "IOException (" + ioe.getMessage()
                  + ") on inputstream");
            handleRequest = false;
            break;
         }

         // take a break..
         try
         {
            Thread.sleep(SLEEP_TIME_BETWEEN_REQUESTS);
         }
         catch (InterruptedException ie)
         {
            // ignore
         }

         if (bytesAvailable > 0)
         {
            break;
         }
         if (mHaveToStop)
         {
            //hangUp = true;
            handleRequest = false;
            break;
         }

      } // end for
      return handleRequest;
   }


   /**
    * Reads the request.
    * @throws Exception
    *          in case of an error
    */
   public void readRequest ()
         throws Exception
   {
      // read the response message
      try
      {
         // try to read <POST / HTTP/1.0>
         readRequestLine();

         // try to read
         //             <Connection: Keep-Alive> etc.
         readHeaderAttributes();

         // try to read
         //              request body
         readRequestBody();
      }
      catch (Exception ex)
      {
         throw ex;
      }

      // dump request
      final StringBuffer buffer = new StringBuffer();
      buffer.append(
            "\n- REQUEST ------------------------------------------\n");
      buffer.append(requestToString());
      buffer.append(
            "\n-----------------------------------------------------\n");
      logger.info(buffer.toString());
   }


   /**
    * Reads the request header line.
    * @throws Exception
    *          in case of an error
    */
   protected void readRequestLine ()
         throws Exception
   {
      String errorText = null; // used by throwing exceptions

      String path = null;
      String httpVersion = null;

      try
      {  // try to find a HTTP status line in the read input data
         mRequestLine = readLine();
         while (mRequestLine != null && !mRequestLine.startsWith("POST"))
         {
            mRequestLine = readLine();
         }
      }
      catch (IOException ioe)
      {
         errorText = "IOException with message: " + ioe.getMessage();
         throw new Exception(errorText);
      }

      if (mRequestLine == null)
      {
         // A null requestLine means the connection was lost
         // before we got a response.  Try again.
         errorText = "Error in parsing the request line : unable to find line"
               + " starting with \"POST\"";
         throw new Exception(errorText);
      }
      // <POST> found...

      final int pathIndex = mRequestLine.indexOf("/");
      int afterPathIndex = 0;
      if (pathIndex > 0)
      {
         afterPathIndex = mRequestLine.indexOf(" ", pathIndex);
         if (afterPathIndex > pathIndex)
         {
            path = mRequestLine.substring(pathIndex, afterPathIndex - 1);
         }
      }
      if (path == null)
      {
         errorText = "Error in parsing the request line : unable to find path";
         throw new Exception(errorText);
      }
      // <POST /> found..

      httpVersion = mRequestLine.substring(afterPathIndex).trim();
      if (httpVersion == null)
      {
         errorText
            = "Error in parsing the request line : unable to find HTTP version";
         throw new Exception(errorText);
      }
      // <POST / HTTP/1.x> found..
   }

   /**
    * Reads the header attributes.
    * @throws Exception
    *          in case of an error
    */
   private void readHeaderAttributes ()
         throws Exception
   {
      String errorText = null; // used by throwing exceptions
      String headerLine = null;
      for (;;)
      {
         try
         {
            headerLine = readLine();
         }
         catch (IOException ioe)
         {
            errorText = "IOException with message: " + ioe.getMessage();
            throw new Exception(errorText);
         }

         // if all header attributes read..
         if ((headerLine == null) || (headerLine.length() < 1))
         {
            break;
         }

         final int colon = parseHeaderLine(headerLine);
         final String name = headerLine.substring(0, colon).trim();
         final String value = headerLine.substring(colon + 1).trim();

         addRequestHeaderParameter(name, value);
      } // end for

      assertRequestParameterGiven();

      // remember the Content-Type header
      mRequestContentType
            = (String) mRequestParameter.get("content-type");

      checkSpecialParameter();
      setEchoParameterToResponse();
   }

   private void checkSpecialParameter ()
   {
      // check for Connection: Close
      final String connectionValue
            = (String) mRequestParameter.get("connection");

      if (connectionValue != null
            && connectionValue.toLowerCase(
                  Constants.SYSTEM_LOCALE).equals("close"))
      {
         mConnectionHaveToBeClosed = true;
      }

      // check for parameter "DoNotRespond"
      final String doNotRespondValue
            = (String) mRequestParameter.get("donotrespond");

      if (doNotRespondValue != null
            && doNotRespondValue.toLowerCase(
                  Constants.SYSTEM_LOCALE).equals("true"))
      {
         mDoNotRespond = true;
      }

      // check for parameter "DoImmediateClose"
      final String doImmediateCloseValue
            = (String) mRequestParameter.get("doimmediateclose");

      if (doImmediateCloseValue != null
            && doImmediateCloseValue.toLowerCase(
                  Constants.SYSTEM_LOCALE).equals("true"))
      {
         mDoImmediateClose = true;
      }

      // check for parameter "UseEmptyResponse"
      final String useEmptyResponse
            = (String) mRequestParameter.get("useemptyresponse");

      if (useEmptyResponse != null
            && useEmptyResponse.toLowerCase(
                  Constants.SYSTEM_LOCALE).equals("true"))
      {
         mUseEmptyResponse = true;
      }
   }

   /**
    * Detects a request parameter with prefix "ECHO_" in request
    * and adds these parameter without prefix to response.
    */
   private void setEchoParameterToResponse ()
   {
      for (final Iterator it = mRequestParameter.keySet().iterator();
            it.hasNext();)
      {
         final String key = (String) it.next();
         final String value = (String) mRequestParameter.get(key);

         final String prefix = "echo_";
         final int prefixLength = prefix.length();
         if (key.startsWith(prefix))
         {
            final String responseKey = key.substring(prefixLength);
            mResponseParameter.put(responseKey, value);
         }
      }
   }

   private int parseHeaderLine (String headerLine)
         throws Exception
   {
      // Parse the header name and value
      final int colon = headerLine.indexOf(":");
      if (colon < 0)
      {
         final String errorText
            = "Unable to parse header - no colon found: " + headerLine;
         throw new Exception(errorText);
      }
      return colon;
   }

   private void addRequestHeaderParameter (String name, String value)
   {
      if (name != null && value != null)
      {
         mRequestParameter.put(name.toLowerCase(
               Constants.SYSTEM_LOCALE), value);
         logger.info("Header Parameter: " + name + "=" + value);
      }
   }

   private void assertRequestParameterGiven ()
         throws Exception
   {
      if (mRequestParameter.isEmpty())
      {
         final String errorText = "no header parameter found";
         throw new Exception(errorText);
      }
   }

   /**
    * Reads the request body.
    * @throws Exception
    *          in case of an error
    */
   private void readRequestBody ()
         throws Exception
   {
      String errorText = null; // used by throwing exceptions
      byte[] responseBody = null;

      // check Content-Length
      final String stringValue
            = (String) mRequestParameter.get("content-length");
      if (stringValue == null)
      {
         errorText = "no Content-Length attribute found";
         logger.warning("Throwing Exception: " + errorText);
         throw new Exception(errorText);
      }
      final int expectedLength = Integer.parseInt(stringValue);

      // check Content-Type
      final String encoding = getEncoding();
      if (encoding == null)
      {
         errorText = "no Content-Type attribute found";
         logger.warning("Throwing Exception: " + errorText);
         throw new Exception(errorText);
      }

      try
      {
         responseBody = read(expectedLength);
      }
      catch (IOException ioe)
      {
         errorText = "IOException with message: " + ioe.getMessage();
         throw new Exception(errorText);
      }

      try
      {
         mRequestBody
            = new String(responseBody, 0, responseBody.length, encoding);
      }
      catch (UnsupportedEncodingException ee)
      {
         errorText = "error while reading body: "
                  + " encoding (" + encoding + ") is not supported";
         logger.warning("Throwing Exception: " + errorText);
         throw new Exception(errorText);
      }
   }

   /**
    * Reads a line from InputStream.
    * @return String
    *          the read line
    * @throws IOException
    *          in case of an error
    */
   public String readLine ()
         throws IOException
   {
      String result = null;
      StringBuffer buffer = new StringBuffer();
      for (;;)
      {
         final int ch = mInStream.read();
         if (ch < 0)
         {
            if (buffer.length() == 0)
            {
               buffer = null;
               break;
            }
            else
            {
               break;
            }
         }
         else if (ch == '\r')
         {
            continue;
         }
         else if (ch == '\n')
         {
            break;
         }
         buffer.append((char) ch);
      }
      if (buffer != null)
      {
         result = buffer.toString();
      }
      return result;
   }

   /**
    * Reads the response body.
    *
    * @param expectedLength
    *          the expected length of body
    * @return byte[]
    *          the request body
    * @throws IOException
    *          in case of an error
    */
   public byte[] read (int expectedLength)
         throws IOException
   {
      final byte[] buffer = new byte[BUFFER_SIZE]; // todo ..configurable!
      final ByteArrayOutputStream tempOut = new ByteArrayOutputStream();

      int bytesRead = 0;
      int foundLength = 0;

      // if expectedLength == -1 ..infinite read til timeout
      //    at the moment a header with "Content-Length" -1 is invalid
      //    and this infinite read scenario is not possible..
      while (expectedLength == -1 || foundLength < expectedLength)
      {
         bytesRead = mInStream.read(buffer);

         // no bytes read
         if (bytesRead == -1)
         {
            break;
         }
         tempOut.write(buffer, 0, bytesRead);
         foundLength += bytesRead;
         if (expectedLength > -1)
         {
            // everything read as expected
            if (foundLength == expectedLength)
            {
               break;
            }
            else if (foundLength > expectedLength)
            {
               final StringBuffer strbuf = new StringBuffer();
               strbuf.append("++ WARNING WHILE READING RESPONSE BODY ++\n");
               strbuf.append("++ expected length (" + expectedLength
                     + ") exceeded ++\n");
               strbuf.append("++ found " + foundLength + " bytes ++");
               logger.warning(strbuf.toString());
               break;
            }
         }
      } // end while

      try
      {
         tempOut.close();
      }
      catch (IOException ioe)
      {
         final String errorText
               = "unable to close buffer with data read from socket"
                  + " as response body";
         throw new IOException(errorText);
      }
      return tempOut.toByteArray();
   }

   /**
    * Gets the encoding.
    * @return String
    *          the used encoding
    */
   public String getEncoding ()
   {
      String result = null;
      final String contentType = (String) mRequestParameter.get("content-type");

      if (contentType != null)
      {
         if (contentType.toUpperCase(
               Constants.SYSTEM_LOCALE).equals("TEXT/PLAIN"))
         {
            result = UTF8;
         }
         else
         {
            result = getEncodingFromCharsetValue (contentType);
         }
      }
      return result;
   }

   /**
    * Gets the encoding extracted from parameter value CHARSET.
    *
    * @param contentType
    *          the parameter value set for Content-Type
    * @return String
    *          the encoding extracted from CHARSET if found in
    *          Content-Type, null else
    */
   private String getEncodingFromCharsetValue (String contentType)
   {
      String result = UTF8; // as default
      final String charsetName = "CHARSET="; // must be upper case
      final int charsetIndex
            = contentType.toUpperCase(Constants.SYSTEM_LOCALE).
                  indexOf(charsetName);
      if (charsetIndex > 0)
      {
         final int quote1 = contentType.
            indexOf("\"", charsetIndex + charsetName.length());
         final int quote2 = contentType.
            indexOf("\"", quote1 + 1);
         if (quote1 > 0)
         {
            result = contentType.
               substring(quote1 + 1, quote2).
                     toUpperCase(Constants.SYSTEM_LOCALE);
         }
         else
         {
            result = contentType.
               substring(charsetIndex + charsetName.length()).
                     toUpperCase(Constants.SYSTEM_LOCALE);
         }
      }
      else
      {
         final String messageText = "no charset defined in Content-Type ("
                                 + contentType + ")";
         logger.info(messageText);
      }
      return result;
   }

   /**
    * Writes the response message.
    * @throws Exception
    *          in case of an error
    */
   public void writeResponse ()
         throws Exception
   {
      StringBuffer buffer = new StringBuffer();

      // create response
      // header line
      mResponseLine = "HTTP/1.0 200 OK";

      // body
      if (!mUseEmptyResponse)
      {
         buffer.append("Echo:");
         buffer.append(mRequestBody);
      }
      mResponseBody = buffer.toString();

      // the message to be send
      final byte[] messageAsByte = mResponseBody.getBytes(UTF8);
      //

      // header parameter
      if (mConnectionHaveToBeClosed)
      {
         mResponseParameter.put("Connection", "Close");
      }
      else
      {
         mResponseParameter.put("Connection", "Keep-Alive");
      }

      final int bodyLength =  messageAsByte.length;
      mResponseParameter.put(
            "Content-Length", Integer.toString(bodyLength));
      mResponseParameter.put("Content-Type", mRequestContentType);

      // complete response
      buffer = new StringBuffer();
      buffer.append(mResponseLine);
      buffer.append(NEW_LINE);
      buffer.append(getResponseParameterAsString());
      buffer.append(NEW_LINE);
      buffer.append(mResponseBody);

      // ..for dumping
      StringBuffer strbuf = new StringBuffer();
      strbuf.append("\n-- RESPONSE --\n");
      strbuf.append(buffer.toString());
      strbuf.append("\n--------------\n");
      logger.info(strbuf.toString());

      // complete message as byte array
      final byte[] messageAsBytes = buffer.toString().getBytes(UTF8);

      mOutStream.write(messageAsBytes);
   }

   /**
    * Gets response header parameter.
    * @return  String
    *          parameter ready for sending
    */
   private String getResponseParameterAsString ()
   {
      final StringBuffer buffer = new StringBuffer();
      final Iterator keyIterator = mResponseParameter.keySet().iterator();
      while (keyIterator.hasNext())
      {
         final String key = (String) keyIterator.next();
         final String value = (String) mResponseParameter.get(key);
         buffer.append(key);
         buffer.append(": ");
         buffer.append(value);
         buffer.append(NEW_LINE);
      }
      return buffer.toString();
   }

   /**
    * Gets the request as string.
    * @return String
    *          the incoming request
    */
   private String requestToString ()
   {
      final StringBuffer buffer = new StringBuffer();
      buffer.append(mRequestLine);
      buffer.append(NEW_LINE);
      final Iterator keyIterator = mRequestParameter.keySet().iterator();
      while (keyIterator.hasNext())
      {
         final String key = (String) keyIterator.next();
         final String value = (String) mRequestParameter.get(key);
         buffer.append(key);
         buffer.append(": ");
         buffer.append(value);
         buffer.append(NEW_LINE);
      }
      buffer.append(NEW_LINE);
      buffer.append(mRequestBody);
      return buffer.toString();
   }

   /**
    * Checks flag indicating to stop handler thread.
    */
   public void haveToStop ()
   {
      mHaveToStop = true;
   }

}
