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
package org.jcoderz.commons.statistics;

/**
 * This class implements a simple counter that can be incremented and
 * decremented.
 * The class is thread-safe.
 *
 * @author Albrecht Messner
 */
public class Counter
   implements Resettable
{
   private String mName;

   /** Stores the counter's value. */
   private long mCounter = 0;

   /**
    * Constructor
    * @param name the counter's name
    */
   public Counter (String name)
   {
      mName = name;
   }
   /**
    * Increment the counter by one.
    * @return the incremented counter value
    */
   public synchronized long increment ()
   {
      mCounter++;
      return mCounter;
   }

   /**
    * Decrement the counter by one.
    * @return the decremented counter value
    */
   public synchronized long decrement ()
   {
      mCounter--;
      return mCounter;
   }

   /**
    * Returns the current value of the counter.
    * @return the current value of the counter.
    */
   public synchronized long getCount ()
   {
      return mCounter;
   }

   /**
    * @see org.jcoderz.commons.statistics.Resettable#reset()
    */
   public synchronized void reset ()
   {
      mCounter = 0;
   }

   /**
    * @return Returns the name.
    */
   public String getName ()
   {
      return mName;
   }

   /**
    * @see java.lang.Object#toString()
    */
   public String toString ()
   {
      return "[Counter: mName=" + getName() + ", mCounter=" + getCount() + "]";
   }
}
