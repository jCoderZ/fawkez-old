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
package org.jcoderz.commons.connector;

import junit.framework.TestCase;

/**
 * Tests the class {@link org.jcoderz.commons.connector.UserPassword}.
 *
 */
public class UserPasswordTest
      extends TestCase
{
   private static final String USER = "anton";
   private static final  String PWD = "tirol";
   private UserPassword mUp1;
   private UserPassword mUp2;
   private UserPassword mUp3;
   private UserPassword mUp4;
   private UserPassword mUp5;
   private UserPassword mUp6;
   private UserPassword mUp7;
   private UserPassword mUp8;

   /**
    * @see junit.framework.TestCase#setUp()
    */
   public void setUp ()
         throws Exception
   {
      super.setUp();

      mUp1 = UserPassword.fromUserPassword(null, null);
      mUp2 = UserPassword.EMPTY_USER_PASSWORD;
      mUp3 = UserPassword.fromUserPassword(USER, null);
      mUp4 = UserPassword.fromUserPassword(USER, PWD);
      mUp5 = UserPassword.fromUserPassword(USER, null);
      mUp6 = UserPassword.fromUserPassword(USER, PWD);
      mUp7 = UserPassword.fromUserPassword(null, PWD);
      mUp8 = UserPassword.fromUserPassword(null, PWD);
   }

   /**
    * Tests the method {@link UserPassword#hashCode()}.
    */
   public void testHashCode ()
   {
      final String msqEqual = "The hash codes must be equal.";
      final String msqUnequal = "The hash codes must be unequal.";

      // null user, null password
      assertEquals(msqEqual, mUp1.hashCode(), mUp2.hashCode());
      assertTrue(msqUnequal, !(mUp1.hashCode() == mUp3.hashCode()));
      assertTrue(msqUnequal, !(mUp1.hashCode() == mUp4.hashCode()));
      assertTrue(msqUnequal, !(mUp1.hashCode() == mUp7.hashCode()));

      // non-null user, null password
      assertEquals(msqEqual, mUp3.hashCode(), mUp5.hashCode());
      assertTrue(msqUnequal, !(mUp3.hashCode() == mUp1.hashCode()));
      assertTrue(msqUnequal, !(mUp3.hashCode() == mUp4.hashCode()));
      assertTrue(msqUnequal, !(mUp3.hashCode() == mUp7.hashCode()));

      // non-null user, non-null password
      assertEquals(msqEqual, mUp4.hashCode(), mUp6.hashCode());
      assertTrue(msqUnequal, !(mUp4.hashCode() == mUp1.hashCode()));
      assertTrue(msqUnequal, !(mUp4.hashCode() == mUp3.hashCode()));
      assertTrue(msqUnequal, !(mUp4.hashCode() == mUp7.hashCode()));

      // null user, non-null password
      assertEquals(msqEqual, mUp7.hashCode(), mUp8.hashCode());
      assertTrue(msqUnequal, !(mUp7.hashCode() == mUp1.hashCode()));
      assertTrue(msqUnequal, !(mUp7.hashCode() == mUp4.hashCode()));
      assertTrue(msqUnequal, !(mUp7.hashCode() == mUp5.hashCode()));
   }

   /**
    * Tests the method {@link UserPassword#equals(Object)}.
    *
    */
   public void testEquals ()
   {
      // null user, null password
      checkEquals(mUp1, mUp1);
      checkEquals(mUp1, mUp2);
      checkUnEquals(mUp1, mUp3);
      checkUnEquals(mUp1, mUp4);
      checkUnEquals(mUp1, mUp7);

      // non-null user, null password
      checkEquals(mUp3, mUp3);
      checkEquals(mUp3, mUp5);
      checkUnEquals(mUp3, mUp1);
      checkUnEquals(mUp3, mUp4);
      checkUnEquals(mUp3, mUp7);

      // non-null user, non-null password
      checkEquals(mUp4, mUp4);
      checkEquals(mUp4, mUp6);
      checkUnEquals(mUp4, mUp1);
      checkUnEquals(mUp4, mUp3);
      checkUnEquals(mUp4, mUp7);

      // null user, non-null password
      checkEquals(mUp7, mUp7);
      checkEquals(mUp7, mUp8);
      checkUnEquals(mUp7, mUp1);
      checkUnEquals(mUp7, mUp4);
      checkUnEquals(mUp7, mUp5);
   }

   private void checkEquals (UserPassword a, UserPassword b)
   {

      assertEquals(
            "The UserPassword instances must be equal. UserPassword left: "
               + a.toString() + " UserPassword right: " + b.toString(), a, b);
   }

   private void checkUnEquals (UserPassword a, UserPassword b)
   {
      assertFalse("The UserPassword instances must be unequal. UserPassword "
            + "left: " + a.toString() + " UserPassword right: " + b.toString(),
               a.equals(b));
   }
}

