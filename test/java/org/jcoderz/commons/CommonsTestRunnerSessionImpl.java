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


import java.lang.reflect.Constructor;


/**
 * Implementation for the TestRunnerSeession bean.
 *
 * @author Michael Griffel
 */
public class CommonsTestRunnerSessionImpl
      implements CommonsTestRunnerSessionInterface
{
   /**
    * {@inheritDoc}
    * @ejb.transaction type="Supports"
    * @ejb.interface-method view-type="remote"
    */
   public void runTest (String testClassName, String testMethodName)
         throws CommonsTestRunnerException
   {
      executeTest(testClassName, testMethodName);
   }

   /**
    * {@inheritDoc}
    * @ejb.transaction type="Required"
    * @ejb.interface-method view-type="remote"
    */
   public void runTestWithTx (String testClassName, String testMethodName)
         throws CommonsTestRunnerException
   {
      executeTest(testClassName, testMethodName);
   }

   private void executeTest (String testClassName, String testMethodName)
         throws CommonsTestRunnerException
   {
      try
      {
         final Class theClass = Class.forName(testClassName);
         final Class[] argTypes = {};

         final Constructor constructor = theClass.getConstructor(argTypes);
         final Object[] args = new Object[]{};
         final Object obj = constructor.newInstance(args);
         final ServerTestCase test = (ServerTestCase) obj;
         test.setName(testMethodName);
         test.runBareAtServer();
      }
      // This is necessary as we don't know which exceptions will occur
      catch (Throwable x)
      {
         throw new CommonsTestRunnerException(x);
      }
   }
}
