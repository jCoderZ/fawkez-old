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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jcoderz.commons.InternalErrorException;



/**
 * This class provides utility functions for the http connector.
 *
 * @author Michael Griffel
 */
public final class HttpConnectionUtil
{
   /** The JNDI name of the http connector. */
   public static final String HTTP_CONNECTOR_JNDI_NAME
         = "HttpConnector";
    /** The JNDI name of the http connector. */
   public static final String HTTP_CONNECTOR_EIS_NAME
         = "java:comp/env/eis/HttpConnector";

   /** The full qualified name of this class. */
   private static final String CLASSNAME
         = HttpConnectionUtil.class.getName();

   /** The logger to use. */
   private static final Logger logger = Logger.getLogger(CLASSNAME);

   private HttpConnectionUtil ()
   {
      // no instances allowed - provides only static helper methods.
   }

   /**
    * Returns a http connection.
    *
    * @param spec the http connection spec identifying the target system
    * @return a http connection.
    */
   public static HttpConnection getHttpConnection (HttpConnectionSpec spec)
   {
      InitialContext context = null;
      final HttpConnection connection;
      try
      {
         context = new InitialContext();
         final HttpConnectionFactory factory
               = (HttpConnectionFactory) context.lookup(
                  HTTP_CONNECTOR_EIS_NAME);
         connection = factory.getConnection(spec);
      }
      // possible exceptions: ResourceException, NamingException
      catch (Exception e)
      {
         throw new InternalErrorException(
               "Failed to create http connection", e);
      }
      finally
      {
         if (context != null)
         {
            try
            {
               context.close();
            }
            catch (NamingException e)
            {
               logger.log(Level.FINER,
                     "Failed to close InitialContext: " + e, e);
            }
         }
      }
      return connection;
   }

   /**
    * Closes the HTTP connection (safe).
    *
    * This method tries to close the given HTTP connection and
    * if an ResourceException occurs a message with the level
    * {@link Level#FINE} is logged. It's safe to pass a
    * <code>null</code> reference for the argument.
    *
    * @param in the HTTP connection that should be closed.
    */
   public static void close (HttpConnection in)
   {
      if (in != null)
      {
         try
         {
            in.close();
         }
         catch (Exception x)
         {
            logger.log(Level.FINE, "Error while closing HTTP "
                  + "connection: HttpConnection.close()", x);
         }
      }
   }

}
