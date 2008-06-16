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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Albrecht Messner
 */
public class Template
{
   public static final String PARAM_START = "${";
   public static final String PARAM_START_ESCAPED = "\\$\\{";
   public static final char PARAM_END = '}';
   public static final String PARAM_END_ESCAPED = "\\}";

   private final String mSourceName;
   private final String mTemplateString;
   private String mTargetName;

   public Template (String sourceName, String templateString)
   {
      mSourceName = sourceName;
      // per default the target name is the same as the source name
      mTargetName = sourceName;
      mTemplateString = templateString;
   }

   public String getSourceName ()
   {
      return mSourceName;
   }

   public String getTargetName ()
   {
      return mTargetName;
   }

   public void setTargetName (String targetName)
   {
      mTargetName = targetName;
   }

   private Set getAllParameters ()
   {
      final Set parameters = new HashSet();
      findParameters(parameters, mTemplateString);
      findParameters(parameters, mTargetName);

      return parameters;
   }

   public Set getParameters ()
   {
      final Set parameters = getAllParameters();

      for (final Iterator it = parameters.iterator(); it.hasNext(); )
      {
         final String param = (String) it.next();
         if (param.startsWith("jcoderz_header"))
         {
            it.remove();
         }
      }

      return parameters;
   }

   public String parametrizeTarget (Map map) throws TemplateGeneratorException
   {
      return parametrizeString(mTargetName, map);
   }

   public String parametrize (Map map) throws TemplateGeneratorException
   {
      return parametrizeString(mTemplateString, map);
   }

   private String parametrizeString (String s, Map map)
         throws TemplateGeneratorException
   {
      String parametrizedTemplate = s;

      final Set parameters = getAllParameters();

      for (final Iterator it = parameters.iterator(); it.hasNext(); )
      {
         final String key = (String) it.next();
         String value = (String) map.get(key);

         if (key.startsWith("jcoderz_header"))
         {
            final String headerType = key.substring(key.lastIndexOf('_') + 1);
            value = TemplateGenerator.getJcoderzHeader(headerType);
         }

         if (value == null)
         {
            throw new IllegalArgumentException(
                  "No replacement found for parameter " + key);
         }

         final String variable = PARAM_START_ESCAPED + key + PARAM_END_ESCAPED;
         // System.out.println("Replacing " + key + " with " + value);

         value = escapeString(value);
         parametrizedTemplate
            = parametrizedTemplate.replaceAll(variable, value);
      }
      // System.out.println("Parametrized Template:" + parametrizedTemplate);
      return parametrizedTemplate;
   }

   private String escapeString (String unescaped)
   {
      final StringBuffer result = new StringBuffer();

      for (int i = 0; i < unescaped.length(); i++)
      {
         final char c = unescaped.charAt(i);
         switch (c)
         {
            case '\\':
               result.append("\\\\");
               break;
            case '$':
               result.append("\\$");
               break;
            default:
               result.append(c);
               break;
         }
      }

      return result.toString();
   }

   private void findParameters (Set parameters, String s)
   {
      int paramStartPos = 0;
      while ((paramStartPos = s.indexOf(
            PARAM_START, paramStartPos)) != -1)
      {
         paramStartPos += PARAM_START.length();
         final int paramEndPos = s.indexOf(PARAM_END, paramStartPos);
         if (paramEndPos == -1)
         {
            throw new IllegalArgumentException(
                  "No matching close tag found for opening tag at "
                  + paramStartPos);
         }
         final String paramName = s.substring(paramStartPos, paramEndPos);
         parameters.add(paramName);
         paramStartPos = paramEndPos + 1;
      }
   }
}
