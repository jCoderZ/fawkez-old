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

import org.jcoderz.commons.ArgumentMalformedException;

/**
 * Utility class that provides random related utility functions.
 *
 * @author Michael Griffel
 */
public final class RandomUtil
{
   private RandomUtil ()
   {
      // utility class - only static methods.
   }

   /**
    * Returns a random long number between the value <tt>min</tt>
    * and the value <tt>max</tt>.
    * @param min the minimum value.
    * @param max the maximum value.
    * @return a random long number between the value <tt>min</tt>
    *       and the value <tt>max</tt>.
    * @throws ArgumentMalformedException if the value <tt>min</tt>
    *       is greater than the value <tt>max</tt>
    */
   public static long random (long min, long max)
         throws ArgumentMalformedException
   {
      if (min > max)
      {
         throw new ArgumentMalformedException("min", String.valueOf(min),
               "Value min " + min + " must be lesser than value max " + max);
      }
      return (long) (Math.random() * (max - min) + min);
   }
}
