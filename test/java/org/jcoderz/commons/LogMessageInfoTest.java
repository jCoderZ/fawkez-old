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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import junit.framework.TestCase;

/**
 * Tests the LogMessageInfo class.
 *
 * @author Michael Griffel
 */
public class LogMessageInfoTest
      extends TestCase
{
   private static final TstLogMessage CODE = TstLogMessage.TEST_MESSAGE;

   /**
    * Tests that it is possible to use the int representation of the log
    * message in a switch statement.
    */
   public void testMisc ()
   {

      switch (CODE.toInt())
      {
         case TstLogMessage.TestMessage.INT_VALUE:
               // expected
            break;
         default:
            fail("Unexpected int " + CODE.toInt() + ", expected "
                  + TstLogMessage.TestMessage.INT_VALUE);
      }
   }

   /**
    * Tests the method {@link TstLogMessage#fromString(String)}.
    */
   public void testFromString ()
   {
      assertSame("fromString method should return the same instance", CODE,
            TstLogMessage.fromString(CODE.toString()));
   }

   /**
    * Tests the method {@link TstLogMessage#fromInt(int)}.
    */
   public void testFromInt ()
   {
      assertSame("fromInt method should return the same instance", CODE,
            TstLogMessage.fromInt(CODE.toInt()));
   }

   /**
    * Tests the method {@link LogMessageInfoImpl#toInt()}.
    */
   public void testToInt ()
   {
      assertEquals("constant int representation",
            TstLogMessage.TestMessage.INT_VALUE,
            TstLogMessage.TEST_MESSAGE.toInt());
   }

   /**
    * Tests the method {@link LogMessageInfoImpl#toString()}.
    */
   public void testToString ()
   {
      assertEquals("string representation should be the message symbol",
            TstLogMessage.TEST_MESSAGE.getSymbol(),
            TstLogMessage.TEST_MESSAGE.toString());
   }

   /**
    * Tests the method {@link LogMessageInfoImpl#getSymbol()}.
    */
   public void testGetSymbol ()
   {
      assertEquals("message symbol",
            "FWK_TST_TEST_MESSAGE", TstLogMessage.TEST_MESSAGE.getSymbol());
   }

   /**
    * Tests the method {@link LogMessageInfoImpl#getLogLevel()}.
    */
   public void testGetLogLevel ()
   {
      assertEquals("default log level should be OFF",
            Level.INFO, TstLogMessage.TEST_MESSAGE.getLogLevel());
   }

   /**
    * Tests the method {@link LogMessageInfoImpl#getMessagePattern()}.
    */
   public void testGetMessagePattern ()
   {
      final String regex = ".*\\{0\\}.*\\{1,date\\}.*\\{1,time\\}.*";
      assertTrue("message pattern test",
            TstLogMessage.TEST_MESSAGE.getMessagePattern().matches(regex));
   }

   /**
    * Tests the method {@link LogMessageInfoImpl#formatMessage(Map, StringBuffer)}.
    */
   public void testFormatMessage ()
   {
      final Map parameters = new HashMap();
      parameters.put(TstLogMessage.TestMessage.PARAM_FOO,
            Collections.singletonList("param"));
      parameters.put(TstLogMessage.TestMessage.PARAM_NOW,
            Collections.singletonList(new Date()));
      System.out.println("message: "
            + TstLogMessage.TEST_MESSAGE.formatMessage(parameters, null));
   }

   /**
    * Tests the method {@link LogMessageInfoImpl#getSolution()}.
    */
   public void testGetSolution ()
   {
      assertNotNull("test solution", TstLogMessage.TEST_MESSAGE.getSolution());
   }

   /**
    * Tests the method {@link LogMessageInfoImpl#getBusinessImpact()}.
    */
   public void testGetBusinessImpact ()
   {
      assertEquals("test business impact", BusinessImpact.UNDEFINED,
            TstLogMessage.TEST_MESSAGE.getBusinessImpact());
   }

   /**
    * Tests the method {@link Object#hashCode()}.
    */
   public void testHashCode ()
   {
      assertEquals("fromString method should return the same hashCode",
            CODE.hashCode(),
            TstLogMessage.fromString(CODE.toString()).hashCode());
      assertEquals("fromInt method should return the same hashCode",
            CODE.hashCode(),
            TstLogMessage.fromInt(CODE.toInt()).hashCode());
   }

   /**
    * Tests the method readResolve().
    * @throws Exception in case of an unexpected error.
    */
   public void testSerialize ()
         throws Exception
   {
      final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
      final ObjectOutputStream objOut = new ObjectOutputStream(bOut);

      objOut.writeObject(CODE);
      objOut.flush();

      final ByteArrayInputStream bIn
            = new ByteArrayInputStream(bOut.toByteArray());
      final ObjectInputStream objIn = new ObjectInputStream(bIn);

      final TstLogMessage p = (TstLogMessage) objIn.readObject();

      assertEquals("Object must be equal after serialization", p, CODE);
      assertSame("Object must be same(!) after serialization", p, CODE);
   }

   /**
    * Test the usage of the test message w/ exception wrapper.
    */
   public void testSampleError ()
   {
      final String parameter = "bar";
      final SampleError error = new SampleError(parameter);
      error.log();
      assertEquals("The parameter foo should not be modified",
            parameter,
            error.getParameter(TstLogMessage.TestMessage.PARAM_FOO).get(0));
   }

   /**
    * Tests the serializable implementation.
    * @throws Exception in case of an unexpected error.
    */
   public void testSerializable ()
         throws Exception
   {
      final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
      final ObjectOutputStream objOut = new ObjectOutputStream(bOut);
      ByteArrayInputStream bIn;
      ObjectInputStream objIn;

      objOut.writeObject(TstLogMessage.TEST_MESSAGE);
      objOut.flush();
      bIn = new ByteArrayInputStream(bOut.toByteArray());
      objIn = new ObjectInputStream(bIn);
      final TstLogMessage messageRead = (TstLogMessage) objIn.readObject();

      assertSame("Values or reference changed during serialization.",
            TstLogMessage.TEST_MESSAGE, messageRead);
   }

   /**
    * Tests the if the special audit log event is generated.
    */
   public void testAuditLogEvent ()
   {
      final String dummyToString = "Dummy Audit Log Event";
      final AuditPrincipal principal = new AuditPrincipal()
         {
            private static final long serialVersionUID = 1L;

            public String toString ()
            {
               return dummyToString;
            }
         };
      final AuditLogEvent event
            = TstLogMessage.TestAuditMessage.create(principal);
      assertEquals("Expected dummy to string representation", dummyToString,
            event.getAuditPrincipal().toString());
      event.log();
   }

   /**
    * Sample exception using test log message.
    *
    * @author Michael Griffel
    */
   public static final class SampleError
         extends BaseException
      {
         static final long serialVersionUID = 1;

         /**
          * Constructor.
          * @param foo The parameter value for foo.
          */
         public SampleError (String foo)
         {
            super(TstLogMessage.TEST_MESSAGE);
            addParameter(TstLogMessage.TestMessage.PARAM_FOO, foo);
            addParameter(TstLogMessage.TestMessage.PARAM_NOW, new Date());
         }
      }

}
