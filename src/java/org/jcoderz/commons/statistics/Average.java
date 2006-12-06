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
 * Compute the average of a number of positive long values.
 * @author Albrecht Messner
 */
public class Average
   implements Resettable
{
   private final String mName;

   private long mNumberOfValues;
   private long mMinimumValue;
   private long mMaximumValue;
   private long mSumOfValues;

   /**
    * Constructor.
    * @param name The name of the average
    */
   public Average (String name)
   {
      mName = name;
      mNumberOfValues = 0;
      mMinimumValue = Long.MAX_VALUE;
      mMaximumValue = 0;
      mSumOfValues = 0;
   }

   /** {@inheritDoc} */
   public synchronized void reset ()
   {
      mNumberOfValues = 0;
      mMinimumValue = Long.MAX_VALUE;
      mMaximumValue = 0;
      mSumOfValues = 0;
   }

   /**
    * Update the average value with the given argument.
    * @param value the value with which the average is updated
    */
   public void update (long value)
   {
      if (value < 0)
      {
         throw new IllegalArgumentException("Values < 0 not allowed");
      }
      long lastSumOfValues;
      long sumOfValues;
      synchronized (this)
      {
         if (value < mMinimumValue)
         {
            mMinimumValue = value;
         }
         if (value > mMaximumValue)
         {
            mMaximumValue = value;
         }

         lastSumOfValues = mSumOfValues;
         mSumOfValues += value;
         sumOfValues = mSumOfValues;
         mNumberOfValues++;
      }

      if (sumOfValues < lastSumOfValues)
      {
         throw new RuntimeException("Sum overflow: "
            + lastSumOfValues + " + " + value + " gives " + sumOfValues);
      }
   }

   /**
    * Computes the average of the values added with {@link #update(long)}
    * so far.
    * @return the average
    */
   public synchronized long getAverage ()
   {
      final long avg;

      if (mNumberOfValues > 0)
      {
         avg = mSumOfValues / mNumberOfValues;
      }
      else
      {
         avg = 0;
      }

      return avg;
   }

   /**
    * Returns the minimum value of this average, or Long.MAX_VALUE if
    * no value has been added so far.
    * @return the minimum value of this average
    */
   public synchronized long getMinimum ()
   {
      return mMinimumValue;
   }

   /**
    * Returns the maximum value of this average, or zero if no
    * value has been added so far.
    * @return the maximum value of this average
    */
   public synchronized long getMaximum ()
   {
      return mMaximumValue;
   }

   /**
    * Returns the number of values that have been added to this average.
    * @return the number of values that have been added to this average
    */
   public synchronized long getCount ()
   {
      return mNumberOfValues;
   }

   /** {@inheritDoc} */
   public String toString ()
   {
      final StringBuffer sbuf = new StringBuffer();
      sbuf.append("[Average ").append(mName);
      sbuf.append(" count=").append(getCount());
      sbuf.append(", min=").append(getMinimum());
      sbuf.append(", avg=").append(getAverage());
      sbuf.append(", max=").append(getMaximum());
      sbuf.append(']');
      return sbuf.toString();
   }

   /**
    * Returns the name.
    * @return String
    */
   public String getName ()
   {
      return mName;
   }
}
