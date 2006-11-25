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


import java.rmi.RemoteException;


/**
 * Session bean for running JUnit tests inside a server.
 * Don't call this yourself, but implement a subclasses of
 * {@link org.jcoderz.commons.ServerTestCase} which will use this to run
 * itself inside the server.
 *
 * @author Michael Griffel
 */
public interface CommonsTestRunnerSessionInterface
{
   /**
    * Runs a test case inside a server.
    * We send the names instead of the test case to avoid
    * serialization of the test case.
    *
    * @param  testClassName the name of the test class, an instance
    *       of ServerTestCase
    * @param  testMethodName the name of the test method to run
    * @throws RemoteException in case on an remote error.
    * @throws CommonsTestRunnerException that wraps any java.lang.Throwable
    *       thrown by the test method.
    */
   void runTest (String testClassName, String testMethodName)
         throws RemoteException, CommonsTestRunnerException;

   /**
    * Runs a test case inside a server with a new container-managed
    * transaction.
    * We send the names instead of the test case to avoid
    * serialization of the test case.
    *
    * @param  testClassName the name of the test class, an instance
    *       of ServerTestCase
    * @param  testMethodName the name of the test method to run
    * @throws RemoteException in case on an remote error.
    * @throws CommonsTestRunnerException that wraps any java.lang.Throwable
    *       thrown by the test method.
    */
   void runTestWithTx (String testClassName, String testMethodName)
         throws RemoteException, CommonsTestRunnerException;

}

