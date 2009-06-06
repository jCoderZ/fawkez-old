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
package org.jcoderz.commons.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jcoderz.commons.Loggable;


/**
 * Helper class around throwables.
 * @author Andreas Mandel
 */
public final class ThrowableUtil
{
   /**
    * Stores the Throwable.getCause() method if this method is available.
    * This should be the case for all JDKs > 1.4.
    */
   private static final Method GET_CAUSE;
   /**
    * Stores the Throwable.initCause(Throwable) method if this method
    * is available.
    * This should be the case for all JDKs > 1.4.
    */
   private static final Method INIT_CAUSE;
   /** A empty object array. */
   private static final Object[] EMPTY_ARRAY = new Object[0];

   private static final String CLASSNAME = ThrowableUtil.class.getName();
   private static final Logger logger = Logger.getLogger(CLASSNAME);

   private static final int MAX_NESTING_DEPTH = 15;

   static
   {
      Method theGetCauseMethod = null;
      Method theInitCauseMethod = null;
      try
      {
         theGetCauseMethod
               = Throwable.class.getDeclaredMethod("getCause", new Class[0]);
         theInitCauseMethod
               = Throwable.class.getDeclaredMethod("initCause",
                  new Class[] {Throwable.class});
      }
      catch (Exception ex)
      {
         // Warning, cause this should not fail with JDK > 1.4
         logger.log(Level.WARNING, "Could not initialize, will run without.",
               ex);
      }
      GET_CAUSE = theGetCauseMethod;
      INIT_CAUSE = theInitCauseMethod;
   }

   private ThrowableUtil ()
   {
      // NO Instances
   }

   /**
    * Tries to fix the exception chaining for the given Throwable.
    * Some exception classes still use a none standart way
    * to nest exceptions. This method tries best to detect this
    * classes and pass the nested exceptions into the standart
    * nesting mechanism available since JDK1.4 with the throwable
    * class. It is save to call this method several times for a
    * give exception.
    * @param ex the exception to be checked.
    */
   public static void fixChaining (Throwable ex)
   {
      try
      {
         Throwable current = ex;
         int nesting = 0;
         while (INIT_CAUSE != null && current != null
               && !(current instanceof Loggable))
         {
            if (nesting++ > MAX_NESTING_DEPTH)
            {
               logger.log(Level.FINEST,
                     "Stoped fixing exception nesting cause max depth "
                     + "reached for given exeption.", ex);
               break;
            }
            if (current.getCause() == null)
            {
               final Method theGetCauseMethod
                     = findGetCauseMethod(current.getClass().getMethods());
               if (theGetCauseMethod != null)
               {
                  initCause(current, theGetCauseMethod);
               }
            }
            current = current.getCause();
         }
      }
      catch (Exception unexpected)
      {
         // do not risc any side effect here
         logger.log(
               Level.SEVERE, "Unexpected exception, ignored.", unexpected);
      }
   }

   /**
    * Dumps the stack trace of the given throwable to its String representation.
    * @param thr the throwable to dump the stack trace from.
    * @return a String representation of the given throwable
    * @see Throwable#printStackTrace()
    */
   public static String toString (Throwable thr)
   {
       final StringWriter sw = new StringWriter();
       PrintWriter pw = null;
       try
       {
           pw = new PrintWriter(sw);
           thr.printStackTrace(pw);
       }
       finally
       {
           IoUtil.close(pw);
           IoUtil.close(sw);
       }
       return sw.toString();
   }

   static Method findGetCauseMethod (final Method[] methods)
   {
      Method theGetCauseMethod = null;
      for (int i = 0; i < methods.length; i++)
      {
         if (methods[i].getDeclaringClass() == Throwable.class)
         {
            continue;
         }
         final int modifier = methods[i].getModifiers();
         if (methods[i].getParameterTypes().length == 0
             && Modifier.isPublic(modifier)
             && !Modifier.isStatic(modifier)
             && Throwable.class.isAssignableFrom(methods[i].getReturnType()))
         {
            if (theGetCauseMethod != null)
            {
               // 2nd hit, savety first
               logger.info("Found 2 matching methods "  + theGetCauseMethod
                     + " or " + methods[i] + ".");
               theGetCauseMethod = null;
               break;
            }
            theGetCauseMethod = methods[i];
         }
      }
      return theGetCauseMethod;
   }


   private static void initCause (Throwable current, Method theGetCauseMethod)
   {
      try
      {
         final Throwable cause
               = (Throwable) theGetCauseMethod.invoke(
                  current, EMPTY_ARRAY);
         INIT_CAUSE.invoke(current, new Object[] {cause});
      }
      catch (Exception e)
      {
         logger.log(Level.FINEST, "Failed to init cause for " + current
               + " using " + theGetCauseMethod + " got a exception.", e);
      }
   }

}
