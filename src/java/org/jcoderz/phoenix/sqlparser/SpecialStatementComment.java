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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.StringTokenizer;

/**
 * @author Albrecht Messner
 */
public final class SpecialStatementComment
{
   public static final int TYPE_BEAN_NAME = 1;
   public static final int TYPE_JAVADOC = 2;
   public static final int TYPE_OPTIMISTIC_VERSION_COUNT = 3;
   public static final int TYPE_SKIP_APPSERVER_SUPPORT = 4;

   private final String mContent;
   private final Token mToken;
   private final int mType;
   
   private SpecialStatementComment (String content, Token token, int type)
   {
      mContent = content;
      mToken = token;
      mType = type;
   }
   
   /** {@inheritDoc} */
   public String toString ()
   {
      return "[SpecialStatementComment: type = "
            + mType + ", content = " + mContent + "]";
   }
   
   public static final SpecialStatementComment parseComment (Token t)
         throws ParseException
   {
      final String content;
      final int type;
      final SpecialStatementComment result;
      final String s = t.getValue();
      if (s.indexOf("@cmpgen") == -1)
      {
         // not a special comment
         result = null;
      }
      else
      {
         if (s.indexOf("bean-name") != -1)
         {
            content = getValue(s);
            type = TYPE_BEAN_NAME;
            result = new SpecialStatementComment(content, t, type);
         }
         else if (s.indexOf("optimistic-version-count") != -1)
         {
            content = "";
            type = TYPE_OPTIMISTIC_VERSION_COUNT;
            result = new SpecialStatementComment(content, t, type);
         }
         else if (s.indexOf("skip-appserver-support") != -1)
         {
            content = "";
            type = TYPE_SKIP_APPSERVER_SUPPORT;
            result = new SpecialStatementComment(content, t, type);
         }
         else if (s.indexOf("javadoc") != -1)
         {
            final StringReader sr = new StringReader(s);
            final BufferedReader br = new BufferedReader(sr);

            final StringBuffer sbuf = new StringBuffer();
            try
            {
               String line;
               while ((line = br.readLine()) != null)
               {
                  if (line.indexOf("/*") == -1
                        && line.indexOf("*/") == -1)
                  {
                     if (sbuf.length() > 0)
                     {
                        sbuf.append(System.getProperty("line.separator"));
                     }
                     sbuf.append(line);
                  }
               }
            }
            catch (IOException e)
            {
               // unexpected, we are reading from a string reader!
               throw new RuntimeException(e);
            }
            content = sbuf.toString();
            type = TYPE_JAVADOC;
            result = new SpecialStatementComment(content, t, type);
         }
         else
         {
            throw new ParseException("Invalid special comment " + s, -1, -1);
         }
      }
      return result;
   }
   
   private static String getValue (String s)
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
    * @return Returns the content of this special comment.
    */
   public String getContent ()
   {
      return mContent;
   }
   
   public int getType ()
   {
      return mType;
   }
}


