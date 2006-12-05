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
package org.jcoderz.phoenix.report;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.Detector;

/**
 * Enumerated type of a severity.
 *
 * Instances of this class are immutable.
 *
 * The following severities are defined:
 * <ul>
 *    <li>Severity.FILTERED</li>
 *    <li>Severity.FALSE_POSITIVE</li>
 *    <li>Severity.OK</li>
 *    <li>Severity.INFO</li>
 *    <li>Severity.WARNING</li>
 *    <li>Severity.ERROR</li>
 *    <li>Severity.COVERAGE</li>
 *    <li>Severity.CPD</li>
 * </ul>
 *
 * @author Andreas Mandel
 */
public final class Severity
   implements Serializable, Comparable
{
   private static final long serialVersionUID = 1L;

   /** The name of the severity */
   private final transient String mName;

   /** Ordinal of next severity to be created. */
   private static int sNextOrdinal = 0;

   /** Assign a ordinal to this severity. */
   private final int mOrdinal = sNextOrdinal++;

   /** Maps a string representation to an enumerated value. */
   private static final Map FROM_STRING = new HashMap();

   /** The severity filtered. */
   public static final Severity FILTERED
      = new Severity("filtered");

   /** The severity false-positive. */
   public static final Severity FALSE_POSITIVE
      = new Severity("false-positive");

   /** The severity none. */
   public static final Severity OK
      = new Severity("ok");

   /** The severity info. */
   public static final Severity INFO
      = new Severity("info");

   /** The severity coverage. */
   public static final Severity COVERAGE
      = new Severity("coverage");

   /** The severity cpd. */
   public static final Severity CPD
      = new Severity("cpd");

   /** The severity warning. */
   public static final Severity WARNING
      = new Severity("warning");

   /** The severity error. */
   public static final Severity ERROR
      = new Severity("error");

   /** The maximum possible severity. */
   public static final Severity MAX_SEVERITY = ERROR;

   /** The maximum possible severity as int. */
   public static final int MAX_SEVERITY_INT = ERROR.toInt();

   /** Internal list of all available severities. */
   private static final Severity[] PRIVATE_VALUES =
      {
         Severity.FILTERED,
         Severity.FALSE_POSITIVE,
         Severity.OK,
         Severity.INFO,
         Severity.COVERAGE,
         Severity.CPD,
         Severity.WARNING,
         Severity.ERROR
      };

   /** Immutable list of the severities. */
   public static final List VALUES =
      Collections.unmodifiableList(Arrays.asList(PRIVATE_VALUES));

   /** Private Constructor. */
   private Severity (String name)
   {
      mName = name;
      FROM_STRING.put(mName, this);
   }

   /**
    * Creates a Severity object from its int representation.
    *
    * @param i the int representation of the severity to be returned.
    * @return the Severity object represented by this int.
    * @throws IllegalArgumentException If the assigned int value isn't listed
    *         in the internal severity table
    */
   public static Severity fromInt (int i)
      throws IllegalArgumentException
   {
      try
      {
         return PRIVATE_VALUES[i];
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
         throw new IllegalArgumentException(
            "Illegal int representation of Severity");
      }
   }

   /**
    * Creates a Severity object from its String representation.
    *
    * @param str the str representation of the severity to be returned.
    * @return the Severity object represented by this str.
    * @throws IllegalArgumentException If the given str value isn't listed 
    *     in the internal severity table
    */
   public static Severity fromString (String str)
      throws IllegalArgumentException
   {
      final Severity result = (Severity) FROM_STRING.get(str);
      if (result == null)
      {
         throw new IllegalArgumentException(
            "Illegal string representation of Severity");
      }
      return result;
   }

   /**
    * Returns the int representation of this severity.
    *
    * @return the int representation of this severity.
    */
   public int toInt ()
   {
      return mOrdinal;
   }

   /**
    * Returns the String representation of this severity.
    *
    * @return the String representation of this severity.
    */
   public String toString ()
   {
      return mName;
   }

   /**
    * Resolves instances being deserialized to a single instance
    * per severity.
    */
   private Object readResolve ()
      throws ObjectStreamException
   {
      return PRIVATE_VALUES[mOrdinal];
   }

   /** {@inheritDoc} */
   public int compareTo (Object o)
   {
      return mOrdinal - ((Severity) o).mOrdinal;
   }

   /**
    * Parses the findbugs severity representation.
    * @param priority the string read from the xml
    * @return the jCoderZ severity representation.
    */
   public static Severity fromFindBugsPriority (String priority)
   {
         final Severity ret;
         switch (Integer.parseInt(priority))
         {
             case Detector.IGNORE_PRIORITY:
                 ret = Severity.FALSE_POSITIVE;
                 break;
             case Detector.EXP_PRIORITY:
                 /* fall throug */
             case Detector.LOW_PRIORITY:
               ret = Severity.INFO;
               break;
            case Detector.NORMAL_PRIORITY:
               ret = Severity.WARNING;
               break;
            case Detector.HIGH_PRIORITY:
               ret = Severity.ERROR;
               break;

            default :
               throw new RuntimeException("Unknown priority from FindBugs: "
                     + priority);
         }
         return ret;
   }

   /**
    * Converts the jCoderZ priority in the FindBugs xml representation. 
    * @return the jCoderZ priority in FindBugs xml representation.
    */
   public String toFindBugsPriority ()
   {
      final String ret;
      if (Severity.ERROR == this)
      {
          ret = String.valueOf(Detector.HIGH_PRIORITY);
      }
      else if (Severity.WARNING == this)
      {
          ret = String.valueOf(Detector.NORMAL_PRIORITY);
      }
      else if (Severity.FALSE_POSITIVE == this)
      {
          ret = String.valueOf(Detector.IGNORE_PRIORITY);
      }
      else
      {
         ret = String.valueOf(Detector.LOW_PRIORITY);
      }
      return ret;
   }

   /**
    * Returns the maximum severe code of this and the given severitiy.
    * @param other the severity to compare with.
    * @return the maximum severe code of this and the given severitiy.
    */
   public Severity max (Severity other)
   {
      final Severity result;

      if (compareTo(other) > 0)
      {
         result = this;
      }
      else
      {
         result = other;
      }
      return result;
   }
}
