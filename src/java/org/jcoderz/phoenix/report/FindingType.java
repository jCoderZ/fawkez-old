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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andreas Mandel
 */
public class FindingType
{
   private static final Map FINDING_TYPES = new HashMap();
   private final String mSymbol;
   private final String mShortText;
   private final String mDescription;


   protected FindingType (String symbol, String shortText, String description)
   {
      mSymbol = symbol.intern();
      mShortText = shortText;
      mDescription = description;
      FINDING_TYPES.put(mSymbol, this);
   }


   public static FindingType fromString (String symbol)
   {
      new LazyInit();

      FindingType result = (FindingType) FINDING_TYPES.get(symbol);
      if (result == null)
      {
         result = new FindingType(symbol, symbol, symbol);
      }
      return result;
   }

   public String getSymbol ()
   {
      return mSymbol;
   }

   public String getShortText ()
   {
      return mShortText;
   }

   public String getDescription ()
   {
      return mDescription;
   }

   public String toString ()
   {
      return mSymbol;
   }

   public int hashCode ()
   {
      return mSymbol.hashCode();
   }

   public boolean equals (Object o)
   {
      return (o instanceof FindingType)
              && mSymbol.equals(((FindingType) o).mSymbol);
   }

   protected static class LazyInit
   {
      static
      {
         // TODO: Make the location of the message XML files overridable!
         CheckstyleFindingType.initialize();
         FindBugsFindingType.initialize();
         PmdFindingType.initialize();
         CpdFindingType.initialize();
         SystemFindingType.initialize();
      }

   }
}
