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

import java.util.Iterator;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.jcoderz.phoenix.findbugs.message.jaxb.BugPatternType;
import org.jcoderz.phoenix.findbugs.message.jaxb.MessageCollection;

/**
 *
 * @author Michael Griffel
 */
public final class FindBugsFindingType extends FindingType
{
   private static final transient String CLASSNAME
      = FindBugsFindingType.class.getName();
   private static final transient Logger logger
      = Logger.getLogger(CLASSNAME);

   private static final String FINDBUGS_MESSAGE_JAXB_CONTEXT
      = "org.jcoderz.phoenix.findbugs.message.jaxb";

   private static final String FINDBUGS_MESSAGE_FILE
      = "org/jcoderz/phoenix/findbugs/messages.xml";

   private final String mMessagePattern;

   private FindBugsFindingType (String symbol, String shortText,
                                String description, String messagePattern)
   {
      super(symbol, shortText, description);
      mMessagePattern = messagePattern;
   }

   /**
    *
    */
   public static void initialize ()
   {
      try
      {
         JAXBContext jaxbContext
            = JAXBContext.newInstance(FINDBUGS_MESSAGE_JAXB_CONTEXT,
               FindBugsFindingType.class.getClassLoader());
         Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
         logger.finest("Try to unmarshalling " + FINDBUGS_MESSAGE_FILE);
         MessageCollection messageCollection
            = (MessageCollection) unmarshaller.unmarshal(
                  FindBugsFindingType.class.getClassLoader().getResourceAsStream(
                     FINDBUGS_MESSAGE_FILE));
         for (Iterator iterator = messageCollection.getContent().iterator();
              iterator.hasNext(); )
         {
             final Object obj = iterator.next();
             if (obj instanceof BugPatternType)
             {
                 final BugPatternType e = (BugPatternType) obj;
                 new FindBugsFindingType(e.getType(), e.getShortDescription(),
                      e.getDetails(), e.getLongDescription());
             }
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Cannot initialize FindBugsFindingTypes", e);
      }
   }

   /**
    * @return
    */
   public final String getMessagePattern ()
   {
      return mMessagePattern;
   }
}
