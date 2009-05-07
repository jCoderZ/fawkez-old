/*
 * $Id: CheckstyleFindingType.java 1173 2008-09-22 10:04:44Z amandel $
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

import java.util.ArrayList;
import java.util.List;

/**
 * Enumeration type for javadoc findings.
 * It also holds a method to get the finding type from the message
 * received. This is needed due to the fact that there is no reliable
 * enumeration of javadoc findings.
 * <p>Once assigned the symbols should not be changed without a urgent
 * need. The symbols are used to generate wiki page link.</p>
 *
 * @author Andreas Mandel
 */
public final class JavaDocFindingType
      extends FindingType
{
   private static final List<JavaDocFindingType> JAVA_DOC_FINDING_TYPES;

   private final String mMessagePattern;
   private final Severity mSeverity;

   static
   {
       JAVA_DOC_FINDING_TYPES = new ArrayList<JavaDocFindingType>();
   }

   /**
    * Generic finding that captures all messages.
    */
   public static final JavaDocFindingType JD_GENERIC_WARNING =
      new JavaDocFindingType("JD_GENERIC_WARNING", "JavaDoc Warning",
         "Interfaces should describe a type and hence have methods.",
         ".*", Severity.DESIGN);
   


   private JavaDocFindingType (String symbol, String shortText,
         String description, String messagePattern, Severity severity)
   {
      super(symbol, shortText, description);
      mMessagePattern = messagePattern;
      mSeverity = severity;
      JAVA_DOC_FINDING_TYPES.add(this);
   }



    private JavaDocFindingType (String symbol, String shortText,
        String description, String messagePattern)
    {
        this(symbol, shortText, description, messagePattern,
            Severity.CODE_STYLE);
    }

   /**
    * Reads the given message and tries to find a matching finding type.
    * @param message the message to read.
    * @return the finding type matching to the message, or null if no such
    *   type was found.
    */
   public static FindingType detectFindingTypeForMessage (String message)
   {
      new FindingType.LazyInit();
      FindingType result = null;

      for (final JavaDocFindingType type : JAVA_DOC_FINDING_TYPES)
      {
         if (message.matches(type.getMessagePattern()))
         {
            result = type;
            break;
         }
      }
      return result;
   }

   /** @return the severity assigned to findings of this type by default. */
   public Severity getSeverity ()
   {
       return mSeverity;
   }

   /**
    * @return Returns the messagePattern.
    */
   private String getMessagePattern ()
   {
      return mMessagePattern;
   }

   /**
    * Init of the enum.
    */
   public static void initialize ()
   {
       // No external file used here.
       // There is no list available containing the JavaDoc messages.
       // In JDK sources 
       // j2se\src\share\classes\com\sun\tools\doclets\internal\toolkit\resources
       // might be used as input?
       // Best might be to introduce a schema to describe finding types
       // for plain text based input. Andreas.
   }
}
