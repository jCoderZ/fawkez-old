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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jcoderz.commons.util.IoUtil;

/**
 * Creates a HTML line by line view on the given Java file.
 * HTML code for eclipse style colors>
 * <pre>
 *   &gt;style type="text/css">
 *  .multi-line-comment { color:#3F7F5F; }
 *  .single-line-comment { color:#3F7F5F; }
 *  .string { color:#2A00FF; }
 *  .keyword { color:#7F0055; font-weight:bold; }
 *  .javadoc { color:#3F5FBF; }
 *  .javadoc-keyword { color:#7F9FBF; font-weight:bold; }
 *  .javadoc-html { color:#7F9F9F; }
 *  &gt;/style>
 * </pre>
 *
 * TODO: Support for annotations & type variables.
 * TODO: Support usage of tab characters
 * TODO: Document original source of this code
 * 
 * @author Andreas Mandel
 */
public class HtmlView
{
   private static final String COMMENT_MAGIC = "&COMMENT_";
   private static final String STRING_MAGIC = "&STRING_";
   private static final String END_MAGIC = ";";

   private static final String CLASSNAME = HtmlView.class.getName();

   private static final Logger logger = Logger.getLogger(CLASSNAME);

   private static final String[] KEYWORDS =
   {
      "class", "throws", "abstract", "assert", "boolean", "break", "byte",
      "case",
      "catch", "char", "const", "continue", "default", "do", "double",
      "else", "enum", "extends", "final", "finally", "float", "for",
      "implements", "import", "instanceof", "int", "interface", "long",
      "native", "new", "package", "private", "protected", "public",
      "return", "short", "static", "strictfp", "super", "switch",
      "synchronized", "this", "throw", "transient", "try",
      "void", "volatile", "while", "null", "true", "false", "if"
   };

   private static final String[] KEYWORD_REPLACEMENT;
   private static final HtmlView HTML_VIEW = new HtmlView();

   static
   {
      KEYWORD_REPLACEMENT = new String[KEYWORDS.length];
      final StringBuffer stringBuffer = new StringBuffer();
      for (int i = 0; i < KEYWORDS.length; i++)
      {
         stringBuffer.setLength(0);
         stringBuffer.append("<span class='keyword'>");
         stringBuffer.append(KEYWORDS[i]);
         stringBuffer.append("</span>");
         KEYWORD_REPLACEMENT[i] = stringBuffer.toString();
      }
   }

   private final List mCommentList = new ArrayList();
   private final List mStringList = new ArrayList();
   private final List mLines = new ArrayList();
   private final StringBuffer mFileData = new StringBuffer();
   private final StringBuffer mStringBuffer = new StringBuffer();
   private File mSourceFile;
   private int mNumberOfLines = -1;
   private String mClassname;
   private String mPackage;

   /**
    * Creates a new HtmlView object.
    */
   public HtmlView ()
   {
      //
   }

   /**
    * Creates a new HtmlView object and uses the given file as input.
    * @param file The input file.
    * @throws IOException If processing of the file fails.
    */
   public HtmlView (File file)
         throws IOException
   {
       privateReset(file);
   }

   /**
    * Resets all collected data and takes the given file as new input file.
    * @param file The new input file.
    * @throws IOException If processing of the file fails.
    */
   public void reset (File file)
         throws IOException
   {
      privateReset(file);
  }


   /**
    * Returns the given line if available or null if no such line
    * exists.<br />
    *
    * @param lineNumber The number of the line to be returned.
    * @return the given line if available or null if no such line
    *         exists.
    */
   public String getLine (int lineNumber)
   {
      return (String) mLines.get(lineNumber - 1);
   }

   /**
    * Returns the number of lines found in the current source file.
    * @return The number of lines found in the current source file.
    */
   public int getNumberOfLines ()
   {
      return mNumberOfLines;
   }

   /**
    * Insert a marker into the given code line.
    *
    * FIXME: If a second marker is set in a line that is behind the first
    * marker, the chars of the first marcher are counted as code chars.
    *
    * @param lineNumber The line number of the code to be marked.
    * @param column The column to be marked (starting from 1...).
    * @param htmlCode The code to be inserted at the given position.
    * @return true, if the code was marked successfully.
    */
   public boolean insertMarker (int lineNumber, int column, String htmlCode)
   {
      boolean result;
      if (lineNumber > mNumberOfLines)
      {
         result = false;
      }
      else
      {
         final String line = getLine(lineNumber);

         // now find the given column
         boolean quote = false;
         boolean entity = false;
         int pos = 1;
         int charPos = -1;
         for (int i = 0; i < line.length(); i++)
         {
            final char current = line.charAt(i);
            if (current == '<')
            {
               quote = true;
            }
            else
            {
               if (quote)
               {
                  quote = (current != '>');
                  entity &= (current != ';');
               }
               else if (entity)
               {
                  entity = (current != ';');
               }
               else
               {
                  if (pos == column)
                  {
                     charPos = i;
                     break;
                  }
                  pos++;
               }
            }
            if (current == '&')
            {
               entity = true;
            }
         }

         if (charPos != -1)
         {
            final StringBuffer lineBuffer = new StringBuffer(line);

            lineBuffer.insert(charPos, htmlCode);
            mLines.set(lineNumber - 1, lineBuffer.toString());
         }
         result = charPos != -1;
      }
      return result;
   }

   /**
    * Reset all member variables to behave as a new viewer.
    * @param file the file to look at,
    * @throws IOException if file io fails.
    */
   private void privateReset (File file) 
           throws IOException 
   {
       mSourceFile = file;
       mCommentList.clear();
       mStringList.clear();
       mLines.clear();
       mFileData.setLength(0);
       mClassname = null;
       mPackage = null;
       mNumberOfLines = -1;
       readFile();
       parseFile();
       splitLines();
   }

   private void splitLines ()
   {
      int pos = 0;
      int lastPos = 0;

      while ((pos = mFileData.indexOf("\n", lastPos)) >= 0)
      {
         mLines.add(mFileData.substring(lastPos, pos));
         lastPos = pos + 1;
      }
      if (lastPos < mFileData.length())
      {
         mLines.add(mFileData.substring(lastPos));
      }
   }

   private void readFile ()
         throws IOException
   {
      if (logger.isLoggable(Level.FINEST))
      {
         logger.finest("Reading source file '" + mSourceFile + "'...");
      }

      int linesCount = 0;
      BufferedReader br = null;
      int i = 0;
      try
      {
         br = new BufferedReader(new FileReader(mSourceFile));
         while ((i = br.read()) >= 0)
         {
            switch (i)
            {
               case '&':  mFileData.append("&amp;");  break;
               case '>':  mFileData.append("&gt;");   break;
               case '<':  mFileData.append("&lt;");   break;
               case ' ':  mFileData.append("&#160;"); break;
               case '\t': mFileData.append("&#160;&#160;&#160;&#160;"); break;
               case '\r': break;
               case '\n': linesCount++;
               default:   mFileData.append((char) i);
            }
         }
      }
      finally
      {
          IoUtil.close(br);
      }
      if (i != '\n')
      {
         mFileData.append('\n');
         linesCount++; // last char is not a newline....
      }
      mNumberOfLines = linesCount;
   }

   private void parseFile ()
   {
      extractMultiLineComments();
      extractSingleLineComments();
      extractQuotedChars();
      extractStrings();
      parseClassNameAndPackage();
      highlightKeywords();
      handleJavaDocComments();
      mergeComments();
      mergeStrings();
   }

   /**
    *
    */
   private void mergeStrings ()
   {
      final StringBuffer stringBuffer = mStringBuffer;
      for (int i = 0; i < mStringList.size(); i++)
      {
         final String comment = (String) mStringList.get(i);
         final String key = STRING_MAGIC + (i + 1) + END_MAGIC;

         stringBuffer.setLength(0);
         stringBuffer.append("<span class='string'>");
         stringBuffer.append(comment);
         stringBuffer.append("</span>");

         final int pos = mFileData.indexOf(key);
         mFileData.replace(pos, pos + key.length(), stringBuffer.toString());
      }
   }

   /**
    *
    */
   private void mergeComments ()
   {
      final StringBuffer commentBuffer = mStringBuffer;
      for (int i = 0; i < mCommentList.size(); i++)
      {
         final String comment = (String) mCommentList.get(i);
         final String key = COMMENT_MAGIC + (i + 1) + END_MAGIC;

         final String type;
         if (comment.startsWith("/**"))
         {
            type = "javadoc";
         }
         else if (comment.charAt(1) == '/')
         {
            type = "single-line-comment";
         }
         else
         {
            type = "multi-line-comment";
         }

         commentBuffer.setLength(0);
         commentBuffer.append("<span class='" + type + "'>");
         int lastPos = 0;
         int pos = 0;
         while ((pos = comment.indexOf("\n", lastPos)) != -1)
         {
            commentBuffer.append(comment.substring(lastPos, pos));
            commentBuffer.append("</span>\n<span class='" + type + "'>");
            lastPos = pos + 1;
         }
         commentBuffer.append(comment.substring(lastPos));
         commentBuffer.append("</span>");

         pos = mFileData.indexOf(key);
         mFileData.replace(pos, pos + key.length(), commentBuffer.toString());
      }
   }

   /**
    *
    */
   private void handleJavaDocComments ()
   {
      final StringBuffer commentBuffer = new StringBuffer();
      for (int i = 0; i < mCommentList.size(); i++)
      {
         final String comment = (String) mCommentList.get(i);
         if (comment.startsWith("/**"))
         {
            // javadoc comment...
            commentBuffer.setLength(0);
            commentBuffer.append(comment);

            int pos = 0;
            // highlight  html tags
            while ((pos = commentBuffer.indexOf("&lt;", pos)) != -1)
            {
               int end = commentBuffer.indexOf("&gt;", pos);

               if (end != -1 && end < commentBuffer.indexOf("\n", pos))
               {
                  end += "&gt;".length();
                  final String str = "<span class='javadoc-html'>"
                     + commentBuffer.substring(pos, end) + "</span>";
                  commentBuffer.replace(pos, end, str);
                  pos += str.length();
               }
               else
               {
                  pos += 1;
               }
            }

            // highlight javadoc keywords
            pos = 0;
            while ((pos = commentBuffer.indexOf("@", pos)) != -1)
            {
               int end = pos;
               while (end < commentBuffer.length()
                     && !Character.isWhitespace(commentBuffer.charAt(end))
                     && commentBuffer.charAt(end) != '&')
               {
                  end++;
               }
               final String str = "<span class='javadoc-keyword'>"
                     + commentBuffer.substring(pos, end) + "</span>";
               commentBuffer.replace(pos, end, str);
               pos += str.length();
            }
            mCommentList.set(i, commentBuffer.toString());
         }
      }
   }

   private void highlightKeywords ()
   {
      // highlight keywords
      for (int i = 0; i < KEYWORDS.length; i++)
      {
         int pos = 0;
         final int len = KEYWORDS[i].length();
         while ((pos = mFileData.indexOf(KEYWORDS[i], pos)) != -1)
         {
            if (pos > 0
                  && Character.isJavaIdentifierPart(mFileData.charAt(pos - 1)))
            {
               pos += len;
               continue;
            }
            final int end = pos + len;
            if (end < (mFileData.length() - 1)
                  && Character.isJavaIdentifierPart(mFileData.charAt(end)))
            {
               pos += len;
               continue;
            }
            mFileData.replace(pos, pos + len, KEYWORD_REPLACEMENT[i]);
            pos += KEYWORD_REPLACEMENT[i].length();
         }
      }
   }

   private void parseClassNameAndPackage ()
   {
      // find class name:
      final StringTokenizer st 
            = new StringTokenizer(mFileData.toString(), " \n\r;\t&");
      mClassname = null;
      mPackage = "NoPackage";
      while (st.hasMoreTokens() && mClassname == null)
      {
         final String token = st.nextToken();
         if ("class".equals(token))
         {
             mClassname = readNextNonNbspToken(st);
         }
         else if ("interface".equals(token))
         {
             mClassname = readNextNonNbspToken(st);
         }
         else if ("package".equals(token))
         {
             mPackage = readNextNonNbspToken(st);
         }
      }
      if (mClassname == null)
      {
         mClassname = "NoClass";
      }
   }

   /**
    * Returns the next token that is neither "nbsp" nor "#160".
    * @param st the next token that is neither "nbsp" nor "#160",
    *   or the last token of the tokenizer.
    */
   private String readNextNonNbspToken (final StringTokenizer st)
   {
       String result = st.nextToken();
       while (st.hasMoreTokens() 
               && ("#160".equals(result) || "nbsp".equals(result)))
       {
           // new string to release rest of memory used for the file
           result = new String(st.nextToken());
       }
       return result;
   }

   private void extractStrings ()
   {
      // Quoted strings
      int pos = 0;
      while ((pos = mFileData.indexOf("\"", pos)) > 0)
      {
         if (isEscaped(pos))
         {
            pos++;
            continue;
         }
         // search the end of the string...
         int end = mFileData.indexOf("\"", pos + 1);
         while (end != -1 && isEscaped(end))
         {
            end = mFileData.indexOf("\"", end + 1);
         }
         if (end == -1)
         {
            logger.log(Level.WARNING, "Panic, did not find closing quote "
                  + "in " + mSourceFile + "."
                  + "Data: " + mFileData.substring(pos) + ".");
            end = mFileData.length() - 1;
         }
         end++;
         // string constant
         mStringList.add(mFileData.substring(pos, end));
         mFileData.replace(pos, end, STRING_MAGIC + mStringList.size()
               + END_MAGIC);
      }
   }
   /**
    *
    */
   private void extractQuotedChars ()
   {
      // Quoted chars
      int pos = 0;
      while ((pos = mFileData.indexOf("\'", pos)) > 0)
      {
         if (isQuotedOrInComment(pos))
         {
            pos++;
            continue;
         }
         // search the end of the char...
         int end = pos + 1;
         while (mFileData.charAt(end) != '\'')
         {
            if (mFileData.charAt(end) == '\\')
            {
               // char in quote is escaped
               end++;
            }
            end++;
            if (end > mFileData.length() || mFileData.charAt(end) == '\n')
            {
               logger.log(Level.WARNING, "Panic, did not find closing quote "
                     + "in " + mSourceFile + " for opening at " + pos + "."
                     + " Data: " + mFileData.substring(pos, end) + ".");
               break;
            }
         }
         end++;
         // single char constant
         mStringList.add(mFileData.substring(pos, end));
         mFileData.replace(pos, end, STRING_MAGIC + mStringList.size()
               + END_MAGIC);
      }
   }

   private void extractSingleLineComments ()
   {
      // Single line comments
      int pos = 0;
      while ((pos = mFileData.indexOf("//", pos)) >= 0)
      {
         if (isQuotedOrInComment(pos))
         {
            pos++;
            continue;
         }
         // search the end of the comment...
         int end = mFileData.indexOf("\n", pos);
         if (end == -1)
         {
            end = mFileData.length() - 1;
         }
         // multi-line comment
         mCommentList.add(mFileData.substring(pos, end));
         mFileData.replace(pos, end, COMMENT_MAGIC + mCommentList.size()
               + END_MAGIC);
      }
   }

   private void extractMultiLineComments ()
   {
      // multi-line Comments....
      int pos = 0;
      while ((pos = mFileData.indexOf("/*", pos)) >= 0)
      {
         if (isQuotedOrInComment(pos))
         {
            pos++;
            continue;
         }
         // search the end of the comment...
         int end = mFileData.indexOf("*/", pos);
         if (end == -1)
         {
            logger.log(Level.WARNING, "Panic, did not find comment closing "
                  + "in " + mSourceFile + ".");
            end = mFileData.length() - 1;
         }
         else
         {
            end += "*/".length();
         }
         mCommentList.add(mFileData.substring(pos, end));
         mFileData.replace(pos, end, COMMENT_MAGIC + mCommentList.size()
               + END_MAGIC);
      }
   }

   /**
    * Searches backward to determine whether the char at the given position is
    * escaped or not.
    * @param pos The position to examine.
    * @return true if the char at position is escaped, false otherwise.
    */
   private boolean isEscaped (int pos)
   {
      boolean escaped = true;
      do
      {
         pos--;
         escaped = !escaped;
      }
      while (pos >= 0 && mFileData.charAt(pos) == '\\');
      return escaped;
   }

   /**
    * Returns true if the char at the given position is quoted, false
    * otherwise.
    * @param pos the position to be examined.
    * @return true if the char at the given position is quoted, false
    *         otherwise.
    */
   private boolean isQuotedOrInComment (int pos)
   {
      int start = pos;
      while (start > 0 && mFileData.charAt(start) != '\n')
      {
         start--;
      }
      boolean quoted = false;
      boolean comment = false;
      for (int i = start; i < pos; i++)
      {
         final char c = mFileData.charAt(i);
         if (c == '\\')
         { // next char is escaped -> ignore it
            i++;
         }
         // do not care about single quote ' cause /* is a multi-char
         // sequence
         else if (c == '\"')
         {
            quoted = !quoted;
         }
         else if (!quoted && c == '/' && i < pos
               && mFileData.charAt(i + 1) == '/')
         {
            comment = true;
            break;
         }
      }
      return quoted || comment;
   }

   /**
    * @return Returns the classname.
    */
   public String getClassname ()
   {
      return mClassname;
   }

   /**
    * @return Returns the package.
    */
   public String getPackage ()
   {
      return mPackage;
   }

   /**
    * Creates a substring of the given string-buffer making sure
    * there is no reference back to the stringbuffer.
    *
    * @param in the string buffer to read from
    * @param offset the start offset
    * @param length the number of chars to copie
    * @return a new string containing a copy of the stringbuffers content. 
    */
   public static String substring (StringBuffer in, int offset, int length)
   {
      final char[] data = new char[length];
      in.getChars(offset, length, data, 0);
      return new String(data);
   }

}
