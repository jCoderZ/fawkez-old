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
package org.jcoderz.commons;

import java.io.File;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.naming.NamingException;


/**
 * <p>Test cases which subclasses this class will be run inside
 * an ejb server vm, without transaction. Just implement subclasses
 * as any other tests.
 *
 * @author Michael Griffel
 */
public class ServerTestCase
      extends TestCase
{
   private static final String DEFAULT_WL_DOMAINDIR = "../";
   private static final String WL_DOMAINDIR = "wl.domaindir";

   /**
    * Returns the path to the web logic domains directory.
    * @return the path to the web logic domains directory.
    */
   public static File getWeblogicDomainDir ()
   {
      return new File(System.getProperty(WL_DOMAINDIR, DEFAULT_WL_DOMAINDIR));
   }

   /**
    * Overrides runbare to run this test in the ejb server.
    *
    * @throws Throwable if any exception is thrown
    */
   public void runBare ()
         throws Throwable
   {
      try
      {
         createTestSession().runTest(this.getClass().getName(), getName());
      }
      catch (CommonsTestRunnerException e)
      {
         throw e.getCause();
      }
      catch (java.rmi.RemoteException e)
      {
         if (e.detail != null)
         {
            throw e.detail;
         }
         throw e;
      }
   }

   /**
    * Runs this test case at the server by calling TestCase's run.
    *
    * @throws Throwable if any exception is thrown
    */
   public void runBareAtServer ()
         throws Throwable
   {
      super.runBare();
   }

   public static String getTestServletUrl (String path, String optionalParams)
         throws Exception
   {
       /*
      final String localServer = System.getProperty("weblogic.Name");
      final Context ctx = new InitialContext();
      final MBeanHome mbeanHome
            = (MBeanHome) ctx.lookup(MBeanHome.LOCAL_JNDI_NAME);
      final Set serverConfigSet = mbeanHome.getMBeansByType("ServerConfig");
      ServerMBean serverConfig = null;
      for (final Iterator it = serverConfigSet.iterator(); it.hasNext(); )
      {
         serverConfig = (ServerMBean) it.next();
         if (serverConfig.getName().equals(localServer))
         {
            break;
         }
      }
      if (serverConfig == null)
      {
         throw new Exception("Failed to determine listen port of local server");
      }
      final int serverListenPort = serverConfig.getListenPort();
      final StringBuffer urlStr = new StringBuffer();
      urlStr.append("http://localhost:").append(serverListenPort);
      urlStr.append("/fawkez-test").append(path);
      if (optionalParams != null)
      {
         urlStr.append(optionalParams);
      }
      return urlStr;
      */
       return null;
   }

   /**
    * Returns a connection to the test runner session bean.
    *
    * @return a connection to the test runner session bean.
    *
    * @throws NamingException when the lookup to the
    *       CommonsTestRunnerSession bean fails.
    * @throws CreateException when the creation of the bean fail.s
    * @throws RemoteException when a remote connection problem occurs.
    */
   protected CommonsTestRunnerSessionInterface createTestSession ()
         throws NamingException, CreateException, RemoteException
   {
      return CommonsTestRunnerSessionJNDIUtil.getHome().create();
   }

}
