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
package org.jcoderz.phoenix.sqlparser;

/**
 * @author Albrecht Messner
 */
public class CreateSequenceStatement
      extends SqlStatement
{
    private static final int DEFAULT_SEQUENCE_CACHE = 20; 
   private String mName;

   private long mIncrementBy = 1;
   private Long mStartWith;
   
   private Long mMaxValue;
   private boolean mNoMaxValue = true;
   
   private Long mMinValue;
   private boolean mNoMinValue = true;
   
   private boolean mCycle = false;
   private long mCache = DEFAULT_SEQUENCE_CACHE;
   private boolean mOrder = false;
   
   public CreateSequenceStatement (String name)
   {
      mName = name;
   }

   /**
    * @see java.lang.Object#toString()
    */
   public String toString ()
   {
      final StringBuffer sbuf = new StringBuffer();
      sbuf.append("[CREATE SEQUENCE Statement: ");
      sbuf.append("name = ").append(getName());
      sbuf.append(", increment by = ").append(getIncrementBy());
      if (getStartWith() != null)
      {
         sbuf.append(", start with = ").append(getStartWith());
      }
      if (getMaxValue() != null)
      {
         sbuf.append(", max value = ").append(getMaxValue());
      }
      sbuf.append(", no maxvalue = ").append(mNoMaxValue);
      if (getMinValue() != null)
      {
         sbuf.append(", min value = ").append(getMinValue());
      }
      sbuf.append(", no minvalue = ").append(mNoMinValue);
      sbuf.append(", cycle = ").append(mCycle);
      sbuf.append(", cache = " ).append(mCache);
      sbuf.append(", order = ").append(mOrder);
      sbuf.append("]");
      return sbuf.toString();
   }

   /**
    * @return Returns the name.
    */
   public String getName ()
   {
      return mName;
   }
   /**
    * @return Returns the cache.
    */
   public long getCache ()
   {
      return mCache;
   }
   /**
    * @param cache The cache to set.
    */
   public void setCache (long cache)
   {
      mCache = cache;
   }
   /**
    * @return Returns the cycle.
    */
   public boolean isCycle ()
   {
      return mCycle;
   }
   /**
    * @param cycle The cycle to set.
    */
   public void setCycle (boolean cycle)
   {
      mCycle = cycle;
   }
   /**
    * @return Returns the incrementBy.
    */
   public long getIncrementBy ()
   {
      return mIncrementBy;
   }
   /**
    * @param incrementBy The incrementBy to set.
    */
   public void setIncrementBy (long incrementBy)
   {
      mIncrementBy = incrementBy;
   }
   /**
    * @return Returns the maxValue.
    */
   public Long getMaxValue ()
   {
      return mMaxValue;
   }
   /**
    * @param maxValue The maxValue to set.
    */
   public void setMaxValue (Long maxValue)
   {
      mMaxValue = maxValue;
   }
   /**
    * @return Returns the minValue.
    */
   public Long getMinValue ()
   {
      return mMinValue;
   }
   /**
    * @param minValue The minValue to set.
    */
   public void setMinValue (Long minValue)
   {
      mMinValue = minValue;
   }
   /**
    * @return Returns the noMaxValue.
    */
   public boolean isNoMaxValue ()
   {
      return mNoMaxValue;
   }
   /**
    * @param noMaxValue The noMaxValue to set.
    */
   public void setNoMaxValue (boolean noMaxValue)
   {
      mNoMaxValue = noMaxValue;
   }
   /**
    * @return Returns the noMinValue.
    */
   public boolean isNoMinValue ()
   {
      return mNoMinValue;
   }
   /**
    * @param noMinValue The noMinValue to set.
    */
   public void setNoMinValue (boolean noMinValue)
   {
      mNoMinValue = noMinValue;
   }
   /**
    * @return Returns the order.
    */
   public boolean isOrder ()
   {
      return mOrder;
   }
   /**
    * @param order The order to set.
    */
   public void setOrder (boolean order)
   {
      mOrder = order;
   }
   /**
    * @return Returns the startWith.
    */
   public Long getStartWith ()
   {
      return mStartWith;
   }

   /**
    * @param startWith The startWith to set.
    */
   public void setStartWith (Long startWith)
   {
      mStartWith = startWith;
   }
}
