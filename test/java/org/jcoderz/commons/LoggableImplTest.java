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


import java.util.Set;
import java.util.logging.Level;
import junit.framework.TestCase;


/**
 * Test for LoggableImpl class.
 *
 * @author Andreas Mandel
 */
public class LoggableImplTest
      extends TestCase
{
   private static final String TEST_INSTANCE_ID = "TEST_INSTANCE";
   private static final String TEST_NODE = "TEST_NODE";
   private static final long TEST_THREAD_ID = 12345L;
   private static final String TEST_DUMMY_PARAMETER_NAME
         = "DUMMY";
   private static final LogMessageInfo TEST_LOG_MESSAGE_INFO
         = new TestLogMessageInfo();

   /**
    * Test for void LoggableImpl(LogMessageInfo, long, String, String).
    */
   public final void testLoggableImplLogMessageInfolongStringString ()
   {
      final LoggableImpl testObject = new LoggableImpl(null,
            TEST_LOG_MESSAGE_INFO, TEST_THREAD_ID, TEST_INSTANCE_ID, TEST_NODE);

      assertEquals("Instance id getter changes value.", TEST_INSTANCE_ID,
            testObject.getInstanceId());
      assertEquals("Instance id parameter changes value.", TEST_INSTANCE_ID,
            testObject.getParameter(LoggableImpl.INSTANCE_ID_PARAMETER_NAME)
                  .get(0));

      assertEquals("Node id getter changes value.", TEST_NODE, testObject
            .getNodeId());
      assertEquals("Node id parameter changes value.", TEST_NODE, testObject
            .getParameter(LoggableImpl.NODE_ID_PARAMETER_NAME).get(0));

      assertEquals("Thread id getter changes value.", TEST_THREAD_ID,
            testObject.getThreadId());
      assertEquals("Thread id parameter changes value.", new Long(
            TEST_THREAD_ID), testObject.getParameter(
            LoggableImpl.THREAD_ID_PARAMETER_NAME).get(0));

      assertEquals("Symbol of testclass changed.", "FOO_SYMBOL",
            TEST_LOG_MESSAGE_INFO.getSymbol());

      TEST_LOG_MESSAGE_INFO.formatMessage(null, null); // coverage
      TEST_LOG_MESSAGE_INFO.getSolution();
      TEST_LOG_MESSAGE_INFO.getBusinessImpact();
   }

   /** Tests LoggableImpl(LogMessageInfo, long, String, String, Throwable). */
   public final void testLoggableImplLogMessageInfolongStringStringThrowable ()
   {
      final Exception testException = new Exception();
      final LoggableImpl testObject = new LoggableImpl(null,
            TEST_LOG_MESSAGE_INFO, TEST_THREAD_ID, TEST_INSTANCE_ID,
            TEST_NODE, testException);

      assertEquals("Cause getter changes value.", testException,
            testObject.getCause());
   }

   /** Tests initCause method. */
   public final void testInitCause ()
   {
      final LoggableImpl testObject
            = new LoggableImpl(null, TEST_LOG_MESSAGE_INFO, TEST_THREAD_ID,
               TEST_INSTANCE_ID, TEST_NODE);

      testObject.initCause(null);
      assertEquals("Cause getter changes value. (null)", null,
            testObject.getCause());
   }

   /** Test the addParameter, getParameter and getParameterNames method. */
   public final void testAddParameter ()
   {
      final LoggableImpl testObject
            = new LoggableImpl(null, TEST_LOG_MESSAGE_INFO, TEST_THREAD_ID,
               TEST_INSTANCE_ID, TEST_NODE);

      testObject.addParameter(TEST_DUMMY_PARAMETER_NAME, "value1");
      testObject.addParameter(TEST_DUMMY_PARAMETER_NAME, "value2");
      testObject.addParameter("FOO_NULL", null);
      testObject.addParameter("FOO_NULL", "value2");

      assertEquals("Parameter value not as expected 1st.", "value1",
            testObject.getParameter(TEST_DUMMY_PARAMETER_NAME).get(0));
      assertEquals("Parameter value not as expected 2nd.", "value2",
            testObject.getParameter(TEST_DUMMY_PARAMETER_NAME).get(1));
      assertEquals("Parameter value (null) not as expected 2st.", null,
            testObject.getParameter("FOO_NULL").get(0));

      final Set names = testObject.getParameterNames();

      assertTrue("Parameter name not in list (" + TEST_DUMMY_PARAMETER_NAME
            + ")", names.contains(TEST_DUMMY_PARAMETER_NAME));
      assertTrue("Internal parameter name not in list ("
            + LoggableImpl.EVENT_TIME_PARAMETER_NAME + ")",
            names.contains(LoggableImpl.EVENT_TIME_PARAMETER_NAME));

   }

   /** Tests the getLogMessageInfo method. */
   public final void testGetLogMessageInfo ()
   {
      final LoggableImpl testObject
            = new LoggableImpl(null, TEST_LOG_MESSAGE_INFO, TEST_THREAD_ID,
               TEST_INSTANCE_ID, TEST_NODE);

      assertEquals("Message info changed?", TEST_LOG_MESSAGE_INFO,
            testObject.getLogMessageInfo());
   }

   /** Tests the get tracking number method. */
   public final void testGetTrackingNumber ()
   {
      final LoggableImpl testObject1
            = new LoggableImpl(null, TEST_LOG_MESSAGE_INFO, TEST_THREAD_ID,
               TEST_INSTANCE_ID, TEST_NODE);
      final LoggableImpl testObject2
            = new LoggableImpl(null, TEST_LOG_MESSAGE_INFO, TEST_THREAD_ID,
               TEST_INSTANCE_ID, TEST_NODE);

      assertFalse("Tracking number should never be the same.",
            testObject1.getTrackingNumber().equals(
               testObject2.getTrackingNumber()));
   }

   /** Tests the GetTrackingNumberInheritance. */
   public final void testGetTrackingNumberInheritance ()
   {
      final BaseException a = new BaseException(TEST_LOG_MESSAGE_INFO);
      final Loggable b = new BaseException(TEST_LOG_MESSAGE_INFO, a);

      assertEquals("Tracking number must be inherited from cause.",
            a.getTrackingNumber(), b.getTrackingNumber());
   }

   /** Tests the get event time method. */
   public final void testGetEventTime ()
   {
      final long before = System.currentTimeMillis();
      final LoggableImpl testObject1
            = new LoggableImpl(null, TEST_LOG_MESSAGE_INFO, TEST_THREAD_ID,
               TEST_INSTANCE_ID, TEST_NODE);
      final long after = System.currentTimeMillis();

      assertTrue("Timestamp must be after before mark.",
            before <= testObject1.getEventTime());
      assertTrue("Timestamp must be after after mark.",
            testObject1.getEventTime() <= after);
   }

   /** Tests the get node id method. */
   public final void testGetNodeId ()
   {
      final LoggableImpl testObject
            = new LoggableImpl(null, TEST_LOG_MESSAGE_INFO, TEST_THREAD_ID,
               TEST_INSTANCE_ID, TEST_NODE);

      assertEquals("Node id getter changes value.", TEST_NODE,
            testObject.getNodeId());

      final LoggableImpl testNullObject
            = new LoggableImpl(null, TEST_LOG_MESSAGE_INFO, TEST_THREAD_ID,
               TEST_INSTANCE_ID, null);

      assertEquals("Node id getter changes value (null).", null,
            testNullObject.getNodeId());
   }

   /** Tests the get instance id method. */
   public final void testGetInstanceId ()
   {
      final LoggableImpl testObject
            = new LoggableImpl(null, TEST_LOG_MESSAGE_INFO, TEST_THREAD_ID,
               TEST_INSTANCE_ID, TEST_NODE);

      assertEquals("Instance id getter changes value.", TEST_INSTANCE_ID,
            testObject.getInstanceId());
   }

   /** Tests the get thread id method. */
   public final void testGetThreadId ()
   {
      final LoggableImpl testObject
            = new LoggableImpl(null, TEST_LOG_MESSAGE_INFO, TEST_THREAD_ID,
               TEST_INSTANCE_ID, TEST_NODE);

      assertEquals("Thread id getter changes value.", TEST_THREAD_ID,
            testObject.getThreadId());
   }

   /** Tests the get cause method. */
   public final void testGetCause ()
   {
      final LoggableImpl testObject
            = new LoggableImpl(null, TEST_LOG_MESSAGE_INFO, TEST_THREAD_ID,
               TEST_INSTANCE_ID, TEST_NODE);
      final Exception testException = new Exception();

      testObject.initCause(testException);

      assertEquals("Cause getter changes value.", testException,
            testObject.getCause());
      assertEquals("Cause getter changes value. (parameter)", testException,
            testObject.getParameter(LoggableImpl.CAUSE_PARAMETER_NAME)
               .get(0));

   }

   /** Tests the log method. */
   public final void testLog ()
   {
      final LoggableImpl testObject
            = new LoggableImpl(null, TEST_LOG_MESSAGE_INFO, TEST_THREAD_ID,
               TEST_INSTANCE_ID, TEST_NODE);

      testObject.log(); // should not throw a exception...
   }

   /** Tests the get message method. */
   public final void testGetMessage ()
   {
      final LoggableImpl testObject
            = new LoggableImpl(null, TEST_LOG_MESSAGE_INFO, TEST_THREAD_ID,
               TEST_INSTANCE_ID, TEST_NODE);

      testObject.addParameter(TEST_DUMMY_PARAMETER_NAME, "value");

      final String message = testObject.getMessage();
      assertTrue("Message must contain dummy object '" + message + "'.",
            message.indexOf("DUMMY:value") != -1);
   }

   /** Internal test class. */
   private static final class TestLogMessageInfo
         extends LogMessageInfoImpl
         implements LogMessageInfo
   {
      static final int INT_VALUE = 4711;
      static final long serialVersionUID = 1;
      private TestLogMessageInfo ()
      {
         super("FOO_SYMBOL", INT_VALUE, Level.INFO,
               "Test Pattern {0} {1,DATE} {1,TIME} DUMMY:{2} ",
               "solution", BusinessImpact.MIDDLE, Category.SECURITY,
               new String[]
                  {
                     LoggableImpl.THREAD_ID_PARAMETER_NAME,
                     LoggableImpl.EVENT_TIME_PARAMETER_NAME,
                     TEST_DUMMY_PARAMETER_NAME
                  },
                "Application Foo", "Foo", "Group Bar", "Bar"
               );
      }
   }
}
