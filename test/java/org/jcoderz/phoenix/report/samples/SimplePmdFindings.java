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
package org.jcoderz.phoenix.report.samples;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This class is used to trigger PMD finders.
 * @author Andreas Mandel
 */
public final class SimplePmdFindings
{
   /** Used to test the finder. */
   public static int sNoneFinalStatic;

   /**
    * Trigger PMD basic/AvoidDecimalLiteralsInBigDecimalConstructor.
    */
   public static final BigDecimal TEST = new BigDecimal(1.123);

   private static Object sSingleton = null;

   /**
    * Trigger PMD design/UncommentedEmptyConstructor
    */
   private SimplePmdFindings ()
   {
   }


   /**
    * Trigger method for findings.
    */
   public void sampleMethod ()
   {
      // Trigger PMD basic/ClassCastExceptionWithToArray.
      final Collection c = new ArrayList();
      final Integer[] a = (Integer []) c.toArray();

      // Trigger PMD basic/MisplacedNullCheck
      if (a.toString().equals("hi") && a != null)
      {
         a.getClass();
      }

      // Trigger PMD basic/UnusedNullCheckInEquals
      if (a != null && c.equals(a))
      {
         a.getClass();
      }

      final BigDecimal bd = new BigDecimal("1");

      // Trigger PMD basic/UselessOperationOnImmutable
      bd.add(new BigDecimal("10"));

      final String s = "TEST";
      // Trigger PMD design/PositionLiteralsFirstInComparisons
      if (s.equals("2"))
      {
         s.getClass();
      }
   }

   /** Trigger PMD design/UncommentedEmptyMethod. */
   public void doSomething ()
   {
   }

   /** Trigger method for findings. */
   public void bool ()
   {
      boolean b = true;

      // Trigger PMD controversial/BooleanInversion
      b = !b; // slow
      b ^= true; // fast
   }

   /** Trigger method for findings. */
   public void design ()
   {
      // Trigger PMD design/AssignmentToNonFinalStatic
      sNoneFinalStatic = 1;
   }

   /**
    * Trigger method for findings.
    * @return a singleton object.
    */
   public Object getSingleton ()
   {
      // Trigger PMD design/NonThreadSafeSingleton
      if (sSingleton == null)
      {
         sSingleton = new Object();
      }
      return sSingleton;
   }

   /**
    * Trigger PMD basic/UselessOverridingMethod.
    * @return a String.
    */
   public String toString ()
   {
      return super.toString();
   }

   /**
    * Trigger PMD design.xml/UnnecessaryLocalBeforeReturn.
    * @return 97
    */
   public int foo ()
   {
      final int x = 97;
      return x;
   }
}
