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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.jcoderz.phoenix.checkstyle.message.jaxb.CheckstyleMessages;
import org.jcoderz.phoenix.checkstyle.message.jaxb.FindingData;

/**
 * @author Andreas Mandel
 */
public final class CheckstyleFindingType
      extends FindingType
{
   private static final List CHECKSTYLE_FINDING_TYPES;

   private static final String CHECKSTYLE_MESSAGE_JAXB_CONTEXT
      = "org.jcoderz.phoenix.checkstyle.message.jaxb";

   private static final String CHECKSTYLE_MESSAGE_FILE
      = "org/jcoderz/phoenix/checkstyle/checkstyle-messages.xml";

   private final String mMessagePattern;

   static
   {
      CHECKSTYLE_FINDING_TYPES = new ArrayList();
   }

   public static final CheckstyleFindingType CS_INTERFACE_TYPE =
      new CheckstyleFindingType("CS_INTERFACE_TYPE", "Interface type.",
         "Interfaces should describe a type and hence have methods.",
         "interfaces should describe a type and hence have methods.");

   public static final CheckstyleFindingType CS_LINE_TO_LONG =
      new CheckstyleFindingType("CS_LINE_TO_LONG", "Line too long.",
         "Line is longer than the allowed number of characters.",
         "Line is longer than [0-9]+ characters.");

   public static final CheckstyleFindingType CS_HEADER_MISMATCH =
      new CheckstyleFindingType("CS_HEADER_MISMATCH", "Header does not match.",
         "Line does not match expected header line. "
         + "Please use the global header.",
         "Line does not match expected header line of .*\\.");

   public static final CheckstyleFindingType CS_JAVADOC_MISSING =
      new CheckstyleFindingType("CS_JAVADOC_MISSING",
         "Missing a Javadoc comment.",
         "Missing a Javadoc comment.",
         "Missing a Javadoc comment\\.");

   public static final CheckstyleFindingType CS_JAVADOC_UNUSED_TAG =
      new CheckstyleFindingType("CS_JAVADOC_UNUSED_TAG",
         "Unused Javadoc tag.",
         "Unused Javadoc tag.",
         "Unused .* tag for '.*'\\.");

   public static final CheckstyleFindingType CS_JAVADOC_RETURN_EXPECTED =
      new CheckstyleFindingType("CS_JAVADOC_RETURN_EXPECTED",
         "Expected an @return tag.",
         "Expected an @return tag.",
         "Expected an @return tag.");

   public static final CheckstyleFindingType CS_JAVADOC_EXPECTED_TAG =
      new CheckstyleFindingType("CS_JAVADOC_EXPECTED_TAG",
         "Missing Javadoc tag.",
         "Missing Javadoc tag.",
         "Expected .* tag for '.*'\\.");

   public static final CheckstyleFindingType CS_JAVADOC_CLASS_INFO =
      new CheckstyleFindingType("CS_JAVADOC_CLASS_INFO",
         "Unable to get class information for something.",
         "Unable to get class information for something.",
         "Unable to get class information for .* tag '.*'\\.");

   public static final CheckstyleFindingType CS_JAVADOC_HTML_UNCLOSED =
      new CheckstyleFindingType("CS_JAVADOC_HTML_UNCLOSED",
         "Incomplete HTML tag.",
         "Incomplete/Unclosed HTML tag.",
         "Incomplete HTML tag found: .*");

   public static final CheckstyleFindingType CS_INVALID_PATTERN =
      new CheckstyleFindingType("CS_INVALID_PATTERN",
         "Name does not match given pattern.",
         "Name does not match given pattern.",
         "Name '.*' must match pattern '.*'\\.");

   public static final CheckstyleFindingType CS_NO_WHITESPACE_AFTER_MSG_DECL =
      new CheckstyleFindingType("CS_NO_WHITESPACE_AFTER_MSG_DECL",
         "Missing whitespace.",
         "After the method declaration there should be a ' '.",
         "No whitespace \\( \\(\\) after method declaration\\.");

   public static final CheckstyleFindingType CS_TODO =
      new CheckstyleFindingType("CS_TODO",
         "Comment matches to-do format.",
         "Comment matches to-do format.",
         "Comment matches to-do format '.*'\\.");

   public static final CheckstyleFindingType CS_MAGIC =
      new CheckstyleFindingType("CS_MAGIC",
         "Dont use magics in the code.",
         "Magics make the code hard to maintain and understand. "
         +" Define appropriate constant instead.",
         "Dont use magic .* in the code\\.");

   public static final CheckstyleFindingType CS_WHITESPACE_AFTER =
      new CheckstyleFindingType("CS_WHITESPACE_AFTER",
         "Whitespace not allowed.",
         "Whitespace not allowed.",
         "'.*' is followed by whitespace\\.");

   public static final CheckstyleFindingType CS_NO_WHITESPACE_AFTER =
      new CheckstyleFindingType("CS_NO_WHITESPACE_AFTER",
         "Whitespace expected.",
         "Whitespace expected.",
         "'.*' is not followed by whitespace\\.");

   public static final CheckstyleFindingType CS_WHITESPACE_BEFORE =
      new CheckstyleFindingType("CS_WHITESPACE_BEFORE",
         "Whitespace not allowed.",
         "Whitespace not allowed.",
         "'.*' is preceeded with whitespace\\.");

   public static final CheckstyleFindingType CS_NO_WHITESPACE_BEFORE =
      new CheckstyleFindingType("CS_NO_WHITESPACE_AFTER",
         "Whitespace expected.",
         "Whitespace expected.",
         "'.*' is not preceeded with whitespace\\.");

   public static final CheckstyleFindingType CS_MISSING_TAG =
      new CheckstyleFindingType("CS_MISSING_TAG",
         "A required javadoc tag is missing.",
         "A required javadoc tag is missing.",
         "Type Javadoc comment is missing an .* tag\\.");

   public static final CheckstyleFindingType CS_HIDDEN_FIELD =
      new CheckstyleFindingType("CS_HIDDEN_FIELD",
         "A field is hidden.",
         "A field is hidden.",
         "'.*' hides a field\\.");

   public static final CheckstyleFindingType CS_CONTAINS_TAB =
      new CheckstyleFindingType("CS_CONTAINS_TAB",
         "Line contains a tab character.",
         "Line contains a tab character. You should use spaces for "
         + "indentation.",
         "Line contains a tab character\\.");

   public static final CheckstyleFindingType CS_NO_NEWLINE =
      new CheckstyleFindingType("CS_NO_NEWLINE",
         "File does not end with a newline.",
         "File does not end with a newline.",
         "File does not end with a newline\\.");

   public static final CheckstyleFindingType CS_MAX_LEN_METHOD =
      new CheckstyleFindingType("CS_MAX_LEN_METHOD",
         "Method length exceeds the maximum allowed length.",
         "A Method should have a moderate length...",
         "Method length is [\\.,0-9]+ lines \\(max allowed is [0-9]+\\)\\.");

   public static final CheckstyleFindingType CS_MAX_LEN_ANON_CLASS =
      new CheckstyleFindingType("CS_MAX_LEN_ANON_CLASS",
         "Length of anonymous inner class exceeds the maximum allowed length.",
         "A anonymous inner class should have a moderate length...",
         "Anonymous inner class length is [0-9]+ lines "
         + "\\(max allowed is [0-9]+\\)\\.");

   public static final CheckstyleFindingType CS_EMPTY_BLOCK =
      new CheckstyleFindingType("CS_EMPTY_BLOCK",
         "Empty block detected.",
         "If you think this is ok you must at least put a comment inside "
         + "this block, describing why it is ok.",
         "Empty .* block\\.");

   public static final CheckstyleFindingType CS_IMPORT_UNUSED =
      new CheckstyleFindingType("CS_IMPORT_UNUSED",
         "Unused import.",
         "Unused import.",
         "Unused import - .*\\.");

   public static final CheckstyleFindingType CS_SPECIAL_INDENT =
      new CheckstyleFindingType("CS_SPECIAL_INDENT",
         "Indentation violation.",
         "Several keywords require a special indentation.",
         "Expected indentation for '.*' is '.*' but was at '.*'\\.");

   public static final CheckstyleFindingType CS_NESTED_TRY_DEPTH =
      new CheckstyleFindingType("CS_NESTED_TRY_DEPTH",
         "Deeply nested trys.",
         "The nesting level for the try/catches is to deep.",
         "Nested try depth is [0-9]+ \\(max allowed is [0-9]+\\)\\.");

   public static final CheckstyleFindingType CS_NUMBER_OF_PARAMETERS =
      new CheckstyleFindingType("CS_NUMBER_OF_PARAMETERS",
         "Too many parameters.",
         "Too many parameters.",
         "More than [0-9]+ parameters\\.");

   public static final CheckstyleFindingType CS_METHOD_UNUSED =
      new CheckstyleFindingType("CS_METHOD_UNUSED",
         "Method unused.",
         "Method is never used.",
         "Unused private method '.*'\\.");

   public static final CheckstyleFindingType CS_LOCAL_VARIABLE_UNUSED =
      new CheckstyleFindingType("CS_LOCAL_VARIABLE_UNUSED",
         "Local variable unused.",
         "Local variable is never used.",
         "Unused local variable '.*'\\.");

   public static final CheckstyleFindingType CS_ILLEGAL_INDENTATION =
      new CheckstyleFindingType("CS_ILLEGAL_INDENTATION",
         "Indentation must be a multiple of 3.",
         "Indentation must be a multiple of 3.",
         "Indentation must be a multiple of 3\\.");

   public static final CheckstyleFindingType CS_FIELD_UNUSED =
      new CheckstyleFindingType("CS_FIELD_UNUSED",
         "Field unused.",
         "Field is never used.",
         "Unused private field '.*'\\.");

   public static final CheckstyleFindingType CS_EQUALS_NEWLINE =
      new CheckstyleFindingType("CS_EQUALS_NEWLINE",
         "The equals operator should be on a new line.",
         "The equals operator should be on a new line.",
         "The equals operator should be on a new line\\.");

   public static final CheckstyleFindingType CS_ILLEGAL_LINE =
      new CheckstyleFindingType("CS_ILLEGAL_PATTERN",
         "Line matches a illegal pattern.",
         "Line matches a illegal pattern.",
         "Line matches the illegal pattern '.*'\\.");

   public static final CheckstyleFindingType CS_UPPER_CASE_L =
      new CheckstyleFindingType("CS_UPPER_CASE_L", "Use uppercase L.",
         "Long constants should use a uppercase L the lower case L looks "
            + "a lot like 1. 123L vs. 123l.",
         "Should use uppercase 'L'\\.");

   public static final CheckstyleFindingType CS_NO_LOG_LEVEL_INFO =
      new CheckstyleFindingType("CS_NO_LOG_LEVEL_INFO",
         "Invalid log level for trace log.",
         "Trace log messages should have log level smaller than info, for "
            + "higher severity use predefined log messages.",
         "Maximum allowed log level for trace log is '.*' but was '.*'\\.");

   public static final CheckstyleFindingType CS_BRACE_ON_NEW_LINE =
       new CheckstyleFindingType("CS_BRACE_ON_NEW_LINE",
          "The brace should not be on a new line.",
          "The brace should not be on a new line.",
          "'[\\{\\}\\(\\)]' should be on the (previous|same) line\\.");

   public static final CheckstyleFindingType CS_INLINE_CONDITIONAL =
       new CheckstyleFindingType("CS_INLINE_CONDITIONAL",
          "Avoid inline conditionals.",
          "Avoid inline conditionals.",
          "Avoid inline conditionals\\.");

   public static final CheckstyleFindingType CS_REDUNDANT_MODIFIER =
       new CheckstyleFindingType("CS_REDUNDANT_MODIFIER",
          "Avoid redundant code.",
          "Avoid redundant code.",
          "Redundant '.*' modifier\\.");


   private CheckstyleFindingType (String symbol, String shortText,
         String description, String messagePattern)
   {
      super(symbol, shortText, description);
      mMessagePattern = messagePattern;
      CHECKSTYLE_FINDING_TYPES.add(this);
   }

   public static FindingType detectFindingTypeForMessage (String message)
   {
      new FindingType.LazyInit();
      FindingType result = null;

      final Iterator i = CHECKSTYLE_FINDING_TYPES.iterator();
      while (i.hasNext() && (result == null))
      {
         CheckstyleFindingType type = (CheckstyleFindingType) i.next();
         if (message.matches(type.getMessagePattern()))
         {
            result = type;
         }
      }
      return result;
   }

   /**
    * @return Returns the messagePattern.
    */
   private String getMessagePattern ()
   {
      return mMessagePattern;
   }

   public static void initialize ()
   {
      try
      {
         JAXBContext jaxbContext
            = JAXBContext.newInstance(CHECKSTYLE_MESSAGE_JAXB_CONTEXT,
               CheckstyleFindingType.class.getClassLoader());
         Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
         CheckstyleMessages messageCollection
            = (CheckstyleMessages) unmarshaller.unmarshal(
                  CheckstyleFindingType.class.getClassLoader().getResourceAsStream(
                     CHECKSTYLE_MESSAGE_FILE));
         for (Iterator iterator = messageCollection.getFindingType().iterator();
              iterator.hasNext(); )
         {
            FindingData e = (FindingData) iterator.next();
            new CheckstyleFindingType(e.getSymbol(), e.getShortDescription(),
                  e.getDetailedDescription(), e.getMessagePattern());

         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Cannot initialize CheckstyleFindingTypes", e);
      }
   }
}
