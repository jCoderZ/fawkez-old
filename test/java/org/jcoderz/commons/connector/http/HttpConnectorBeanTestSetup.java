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

import junit.extensions.TestSetup;
import junit.framework.Test;
import org.jcoderz.commons.connector.http.transport.SimpleServer;

/**
 * Test setup used for HttpConnectionBeanTest.
 *
 */
public class HttpConnectorBeanTestSetup
      extends TestSetup
{
   private static final int DEFAULT_SERVER_PORT = 55555;
   private SimpleServer mServer;

   /**
    * Constructor.
    * @param test
    *          test using this setup
    */
   public HttpConnectorBeanTestSetup (Test test)
   {
      super(test);
   }

   /**
    * Sets up the environment for the HttpConnnectionBeanTest.
    * Starting a simple http server.
    * @throws Exception
    *          in case of any error
    */
   public void setUp ()
         throws Exception
   {
      mServer = new SimpleServer(DEFAULT_SERVER_PORT);
      mServer.start();
      while (!mServer.isServerStarted())
      {
         Thread.yield();
      }
   }

   /**
    * Tears down the environment for the HttpConnectionBeanTest.
    * Sets flag for stopping simple http server.
    * @throws Exception
    *          in case of any error
    */
   public void tearDown ()
         throws Exception
   {
      mServer.doStop();
   }
}
