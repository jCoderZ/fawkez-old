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

import java.util.StringTokenizer;

/**
 * This class represents a special comment in the following form
 * -- &amp;cmpgen.java-type org.jcoderz.ipp.Msisdn
 * -- &amp;cmpgen.storeMethod="toString()"
 * -- &amp;cmpgen.loadMethod="fromString(java.lang.String)".
 *
 * @author Albrecht Messner
 */
public class SpecialColumnComment
{
   private String mJavaType;
   private String mStoreMethod;
   private String mLoadMethod;
   private String mCurrencyColumn;
   private String mPeriodFieldName;
   private String mPeriodEndDateColumn;
   private String mWeblogicColumnType;
   private boolean mSkipInInterface = false;

   /** {@inheritDoc} */
   public final String toString ()
   {
      return "[SpecialColumnComment "
            + "java-type=" + mJavaType
            + ", store-method=" + mStoreMethod
            + ", load-method=" + mLoadMethod
            + "]";
   }
   
   /**
    * Parse a special comment.
    * @param t the token to parse
    * @throws ParseException if the comment has a syntax error
    */
   public final void parseComment (Token t) throws ParseException
   {
      final String s = t.getValue();
      if (s.indexOf("@cmpgen") != -1)
      {
         if (s.indexOf("java-type") != -1)
         {
            if (isSetJavaType())
            {
               throw new ParseException(
                       "Duplicate special comment 'java-type'", -1, -1);
            }
            mJavaType = getValue(s);
         }
         else if (s.indexOf("store-method") != -1)
         {
            if (isSetStoreMethod())
            {
               throw new ParseException(
                  "Duplicate special comment 'store-method'", -1, -1);
            }
            mStoreMethod = getValue(s);
         }
         else if (s.indexOf("load-method") != -1)
         {
            if (isSetLoadMethod())
            {
               throw new ParseException(
                  "Duplicate special comment 'load-method'", -1, -1);
            }
            mLoadMethod = getValue(s);
         }
         else if (s.indexOf("currency-column") != -1)
         {
            if (isSetCurrencyColumn())
            {
               throw new ParseException(
                     "Duplicate special comment 'currency-column'", -1, -1);
            }
            mCurrencyColumn = getValue(s);
         }
         else if (s.indexOf("period-field-name") != -1)
         {
            if (isSetPeriodFieldName())
            {
               throw new ParseException(
                     "Duplicate special comment 'period-field-name'", -1, -1);
            }
            mPeriodFieldName = getValue(s);
         }
         else if (s.indexOf("period-end-date-column") != -1)
         {
            if (isSetPeriodEndDateColumn())
            {
               throw new ParseException(
                     "Duplicate special comment 'period-end-date-column'", 
                     -1, -1);
            }
            mPeriodEndDateColumn = getValue(s);
         }
         else if (s.indexOf("skip-in-interface") != -1)
         {
            mSkipInInterface = true;
         }
         else if (s.indexOf("weblogic-dbms-column-type") != -1)
         {
            if (isSetWeblogicColumnType())
            {
               throw new ParseException(
                     "Duplicate special comment 'weblogic.dbms-column-type'",
                     -1, -1);
            }
            mWeblogicColumnType = getValue(s);
         }
         else
         {
            throw new ParseException("Invalid special comment", -1, -1);
         }
      }
   }
   
   private String getValue (String s)
   {
      final StringTokenizer tok = new StringTokenizer(s, "=");
      tok.nextToken(); // just skip first token
      String secondPart = tok.nextToken();
      secondPart = secondPart.trim();
      if (secondPart.startsWith("\""))
      {
         secondPart = secondPart.substring(1);
      }
      if (secondPart.endsWith("\""))
      {
         secondPart = secondPart.substring(0, secondPart.length() - 1);
      }
      return secondPart.trim();
   }

   /**
    * Returns the period field name.
    * @return the period field name
    */
   public final String getPeriodFieldName ()
   {
      return mPeriodFieldName;
   }

   /**
    * Returns the end date column.
    * @return the end date column
    */
   public final String getPeriodEndDateColumn ()
   {
      return mPeriodEndDateColumn;
   }

   /**
    * Returns the currency column.
    * @return the currency column
    */
   public final String getCurrencyColumn ()
   {
      return mCurrencyColumn;
   }

   /**
    * Returns the java type.
    * @return the java type
    */
   public final String getJavaType ()
   {
      return mJavaType;
   }

   /**
    * Returns the load method.
    * @return the load method
    */
   public final String getLoadMethod ()
   {
      return mLoadMethod;
   }

   /**
    * Returns the store method.
    * @return the store method
    */
   public final String getStoreMethod ()
   {
      return mStoreMethod;
   }

   /**
    * Check whether all required fields have been set.
    * @throws ParseException if the comment is invalid
    */
   public final void validate () throws ParseException
   {
      if (isSetLoadMethod() || isSetStoreMethod())
      {
         // if one is set, both must be present
         if (! (isSetLoadMethod() && isSetStoreMethod()))
         {
            throw new ParseException(
               "'store-method' and 'load-method' must be "
               + "both present or both absent",
               -1,
               -1);
         }

         // if we have load and store methods, we also need the
         // java type
         if (! isSetJavaType())
         {
            throw new ParseException(
               "Invalid special comment, mandatory field missing",
               -1,
               -1);
         }
      }

      if (isSetPeriodFieldName())
      {
         // if one is set, both must be present
         if (! (isSetPeriodEndDateColumn()))
         {
            throw new ParseException(
               "'period-end-date-column' must be"
               + " present when 'period-field-name' has been specified.",
               -1,
               -1);
         }
      }
   }

   public final boolean isSkipInInterface ()
   {
      return mSkipInInterface;
   }
   
   /**
    * Check whether the field mJavaType has been set.
    * @return true if the field is not-null, false otherwise
    */
   public final boolean isSetJavaType ()
   {
      return mJavaType != null;
   }   

   /**
    * Check whether the field mStoreMethod has been set.
    * @return true if the field is not-null, false otherwise
    */
   public final boolean isSetStoreMethod ()
   {
      return mStoreMethod != null;
   }   

   /**
    * Check whether the field mLoadMethod has been set.
    * @return true if the field is not-null, false otherwise
    */
   public final boolean isSetLoadMethod ()
   {
      return mLoadMethod != null;
   }
   
   /**
    * Check whether the field mCurrencyColumn has been set.
    * @return true if the field is not-null, false otherwise
    */
   public final boolean isSetCurrencyColumn ()
   {
      return mCurrencyColumn != null;
   }

   /**
    * Check whether the field mPeriodFieldName has been set.
    * @return true if the field is not-null, false otherwise
    */
   public final boolean isSetPeriodFieldName ()
   {
      return mPeriodFieldName != null;
   }

   /**
    * Check whether the field mPeriodEndDateColumn has been set.
    * @return true if the field is not-null, false otherwise
    */
   public final boolean isSetPeriodEndDateColumn ()
   {
      return mPeriodEndDateColumn != null;
   }

   public String getWeblogicColumnType ()
   {
      return mWeblogicColumnType;
   }
   
   public boolean isSetWeblogicColumnType ()
   {
      return mWeblogicColumnType != null;
   }
}
