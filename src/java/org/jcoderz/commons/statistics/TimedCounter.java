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

import java.text.DecimalFormat;

import org.jcoderz.commons.types.Date;



/**
 * TODO Write javadoc!
 *
 * @author Albrecht Messner
 */
public class TimedCounter
      extends Counter
{
   private long mStartTime;
   private long mStopTime;

   /**
    * Constructor.
    * @param name the name of this counter
    */
   public TimedCounter (String name)
   {
      super(name);
      mStartTime = 0;
      mStopTime = 0;
   }

   /**
    * Start this counter's timer.
    */
   public synchronized void start ()
   {
      mStartTime = System.currentTimeMillis();
   }

   /**
    * Stop this counter's timer.
    */
   public synchronized void stop ()
   {
      mStopTime = System.currentTimeMillis();
   }

   /**
    * @see org.jcoderz.commons.statistics.Counter#reset()
    */
   public synchronized void reset ()
   {
      super.reset();
      mStopTime = 0;
      start();
   }

   /**
    * Returns the amount of time this Counter was running.
    * @return the amount of time this Counter was running
    */
   public synchronized long getDuration ()
   {
      final long duration;
      if (mStopTime != 0)
      {
         duration = mStopTime - mStartTime;
      }
      else
      {
         duration = System.currentTimeMillis() - mStartTime;
      }
      return duration;
   }

   /**
    * Returns the frequency of this counter, i.e. getCount() / duration.
    * @return the frequency of this counter, i.e. getCount() / duration.
    */
   public synchronized double getFrequency ()
   {
      final long duration = getDuration();
      double result = 0;
      final double durationInSec = duration / (double) Date.MILLIS_PER_SECOND;

      if (durationInSec > 0)
      {
         result = getCount() / durationInSec;
      }
      return result;
   }

   /** {@inheritDoc} */
   public String toString ()
   {
      final DecimalFormat df = new DecimalFormat("0.000");

      final StringBuffer sbuf = new StringBuffer();
      sbuf.append("[TimedCounter name=").append(getName());
      sbuf.append(", duration=").append(getDuration());
      sbuf.append(", count=").append(getCount());
      sbuf.append(", freq=").append(df.format(getFrequency()));
      sbuf.append("]");
      return sbuf.toString();
   }
}
