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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.jcoderz.commons.util.StringUtil;

/**
 * @author Andreas Mandel
 */
public class FindingType
{
    private static final String CLASSNAME = FindingType.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASSNAME);  
    private static final Map<String,FindingType> 
        FINDING_TYPES = new HashMap<String,FindingType>();
    private static final Set<Origin> 
        INITIALIZED_FINDING_TYPES = new HashSet<Origin>();
    private final String mSymbol;
    private final String mShortText;
    private final String mDescription;


    protected FindingType (String symbol, String shortText, String description)
    {
        mSymbol = symbol.intern();
        mShortText 
            = StringUtil.isNullOrBlank(shortText) ? mSymbol : shortText;
        mDescription 
            = StringUtil.isNullOrBlank(description) ? mShortText : description;
        FINDING_TYPES.put(mSymbol, this);
    }


   public static FindingType fromString (String symbol)
   {
      new LazyInit();

      FindingType result = FINDING_TYPES.get(symbol);
      if (result == null)
      {
         result = new FindingType(symbol, null, null);
      }
      return result;
   }

   public static void initialize (Origin findingType)
   {
       if (!INITIALIZED_FINDING_TYPES.contains(findingType))
       {
           INITIALIZED_FINDING_TYPES.add(findingType);
           if (Origin.CHECKSTYLE.equals(findingType))
           {
               CheckstyleFindingType.initialize();
           }
           else if (Origin.COVERAGE.equals(findingType))
           {
               // No stuff here
           }
           else if (Origin.FINDBUGS.equals(findingType))
           {
               FindBugsFindingType.initialize();
           }
           else if (Origin.PMD.equals(findingType))
           {
               PmdFindingType.initialize();
           }
           else if (Origin.CPD.equals(findingType))
           {
               CpdFindingType.initialize();
           }
           else if (Origin.SYSTEM.equals(findingType))
           {
               SystemFindingType.initialize();
           }
           else
           {
               GenericReportReader.initialize(findingType);
           }
       }
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
          initialize(Origin.CHECKSTYLE);
          initialize(Origin.FINDBUGS);
          initialize(Origin.PMD);
          initialize(Origin.CPD);
          initialize(Origin.SYSTEM);
      }
   }
}
