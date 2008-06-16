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
package org.jcoderz.phoenix.templategen;

import java.util.regex.Pattern;

/**
 * @author Albrecht Messner
 */
public class Parameter
{
   private final String mName;
   private final int mMinLength;
   private final int mMaxLength;
   private final boolean mMultiLine;

   private String mDescription;
   private String mDefaultValue;
   private String mRegexp;

   public Parameter (
         String name,
         int minLength,
         int maxLength,
         boolean multiLine)
   {
      mName = name;
      mMinLength = minLength;
      mMaxLength = maxLength;
      mMultiLine = multiLine;
   }

   public int getMaxLength ()
   {
      return mMaxLength;
   }

   public int getMinLength ()
   {
      return mMinLength;
   }

   public boolean isMultiLine ()
   {
      return mMultiLine;
   }

   public String getName ()
   {
      return mName;
   }

   public String getDescription ()
   {
      return mDescription;
   }

   public void setDescription (String string)
   {
      mDescription = string;
   }

   public String getDefaultValue ()
   {
      return mDefaultValue;
   }

   public String getRegexp ()
   {
      return mRegexp;
   }

   public void setDefaultValue (String string)
   {
      mDefaultValue = string;
   }

   public void setRegexp (String string)
   {
      mRegexp = string;
   }

   public void checkValue (String value) throws TemplateGeneratorException
   {
      if (value.length() < mMinLength
            || value.length() > mMaxLength)
      {
         throw new TemplateGeneratorException(
               "Invalid value '" + value + "' for parameter " + mName
               + ": length must be between " + mMinLength
               + " and " + mMaxLength + " characters");
      }
      if (mRegexp != null)
      {
         if (! Pattern.matches(mRegexp, value))
         {
            throw new TemplateGeneratorException(
                  "Invalid value '" + value + "' for parameter " + mName
                  + ": does not match regular expression "
                  + "'" + mRegexp + "'");
         }
      }
   }

   /** {@inheritDoc} */
   public String toString ()
   {
      return "[Parameter name=" + mName
            + ", minLength=" + mMinLength
            + ", maxLength=" + mMaxLength
            + ", multiLine=" + mMultiLine
            + ", description=" + mDescription
            + ", default=" + mDefaultValue
            + ", regexp=" + mRegexp
            + "]";
   }
}
