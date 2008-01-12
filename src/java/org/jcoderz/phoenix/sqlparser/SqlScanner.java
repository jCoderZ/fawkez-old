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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jcoderz.commons.util.Constants;

/**
 * Simple SQL Scanner.
 *
 * @author Michael Griffel
 */
public final class SqlScanner
      implements ScannerInterface
{
   private final BufferedInputStream mInputStream;
   private int mColumn = 0;
   private int mLine = 1;
   private boolean mReportWhitespace = true;
   private int mSaveColumn = 0;

   /**
    * create a new SQL Scanner.
    * @param input the input stream to read SQL data from
    */
   public SqlScanner (InputStream input)
   {
      mInputStream = new BufferedInputStream(input);
   }

   /**
    * Returns the reportWhitespace.
    * @return the reportWhitespace.
    */
   public boolean isSetReportWhitespace ()
   {
      return mReportWhitespace;
   }

   /**
    * Sets the reportWhitespace to given <code>reportWhitespace</code>.
    * @param reportWhitespace The reportWhitespace to set.
    */
   public void setReportWhitespace (boolean reportWhitespace)
   {
      mReportWhitespace = reportWhitespace;
   }

   /**
    * Returns the line.
    * @return the line.
    */
   public int getLine ()
   {
      return mLine;
   }

   /**
    * Returns the offset.
    * @return the offset.
    */
   public int getColumn ()
   {
      return mColumn;
   }

   /**
    * This is just a wrapper around the real nextToken() method for logging.
    * @return the next token
    * @throws ParseException if a syntax error is encountered
    * @see org.jcoderz.phoenix.sqlparser.ScannerInterface#nextToken()
    */
   public Token nextToken ()
         throws ParseException
   {
      return getNextToken();
   }

   /** {@inheritDoc} */
   private Token getNextToken ()
         throws ParseException
   {
      for (;;)
      {
         mark();
         final int c = read();

         if (c == -1) // EOF
         {
            return new Token(TokenType.EOF);
         }
         else if (isNewlineChar((char) c))
         {
            final Token t = eatNewline(c);
            if (mReportWhitespace)
            {
               return t;
            }
            continue;
         }
         else if (Character.isWhitespace((char) c))
         {
            final Token t = eatWhitespaces(c);
            if (mReportWhitespace)
            {
               return t;
            }
            continue;
         }
         else if (c == '(')
         {
            return new Token(TokenType.OPEN_PAREN, asString(c));
         }
         else if (c == ')')
         {
            return new Token(TokenType.CLOSE_PAREN, asString(c));
         }
         else if (c == ';')
         {
            return new Token(TokenType.SEMICOLON, asString(c));
         }
         else if (c == ',')
         {
            return new Token(TokenType.COMMA, asString(c));
         }
         else if (c == '/') // maybe block comment or single slash
         {
            mark();
            if (read() == '*') // a block comment
            {
               final String comment = eatBlockComment();
               return new Token(TokenType.COMMENT, comment);
            }
            reset();
            return new Token(TokenType.SLASH, asString(c));
         }
         else if (c == '-') // comment or numeric
         {
            mark();
            final int d = read();

            final Token t;
            if (d == '-') // -> comment
            {
               final StringBuffer sb = new StringBuffer();
               sb.append("--");
               for (;;)
               {
                  mark();
                  final int e = read();
                  if (e == '\n' || e == -1) // end of line or eof
                  {
                     reset();
                     break;
                  }
                  sb.append((char) e);
               }
               t =  new Token(TokenType.COMMENT, sb.toString());
            }
            else if (Character.isDigit((char) d))// (negative) nummeric
            {
               final StringBuffer sb = new StringBuffer();
               sb.append('-');
               sb.append((char) d);
               for (;;)
               {
                  mark();
                  final int e = read();
                  if (! Character.isDigit((char) e))
                  {
                     reset();
                     break;
                  }
                  sb.append((char) e);
               }

               final String negativeNumeric = sb.toString();
               try
               {
                  Integer.parseInt(negativeNumeric);
                  t = new Token(TokenType.NUMERIC_LITERAL, negativeNumeric);
               }
               catch (NumberFormatException shouldNotOccur)
               {
                  throw new ParseException("Cannot parse negative numberic '"
                        + negativeNumeric
                        + "'", shouldNotOccur, mLine, mColumn);
               }
            }
            // operator '- ', '-(' or '-function'
            else if (d == '(' || Character.isLetter((char) d)
                  || Character.isWhitespace((char) d))
            {
               reset();
               return new Token(TokenType.OPERATOR, asString(c));
            }
            else
            {
               throw new ParseException("Unexpected char '" + (char) d
                   + "', expected '-' or digit.", mLine, mColumn);
            }
            return t;
         }
         else if (c == '"' || c == '\'') // literal
         {
            final String literal = readStringLiteral(c);
            return new Token(TokenType.STRING_LITERAL, literal);
         }
         else // keywords, identifier
         {
            final String word = readWord(c);

            try
            {
               // FIXME: prefix keyword? otherwise 'comma' will be a keyword
               final TokenType tokenType
                  = TokenType.fromString(
                        word.toLowerCase(Constants.SYSTEM_LOCALE));
               return new Token(tokenType, word);
            }
            catch (IllegalArgumentException ignore)
            {
               // not a known keyword
            }

            // numeric literal?
            try
            {
               new BigDecimal(word); // well-formed?
               return new Token(TokenType.NUMERIC_LITERAL, word);
            }
            catch (NumberFormatException ignore)
            {
               // not a numeric
            }

            // otherwise it must be a identifier (hopefully)
            return new Token(TokenType.IDENTIFIER, word);
         }
      }
   }

   private String eatBlockComment ()
         throws ParseException
   {
      // read block comment
      final StringBuffer sb = new StringBuffer();
      sb.append("/*");
      for (;;)
      {
         mark();
         final int d = read();
         if (d == '*') // maybe end of block comment
         {
            mark();
            if (read() != '/') // not end of block comment
            {
               reset();
               sb.append((char) d);
               continue;
            }
            sb.append("*/");
            break;
         }
         else if (isNewlineChar((char) d))
         {
            ++mLine; mColumn = 0;
         }
         sb.append((char) d);
      }
      return sb.toString();
   }

   private String readWord (int c)
         throws ParseException
   {
      final StringBuffer sb = new StringBuffer();
      sb.append((char) c);
      for (;;)
      {
         mark();
         final int d = read();

         if (isSpecialCharacter((char) d))
         {
            reset();
            break;
         }
         sb.append((char) d);
      }
      return sb.toString();
   }

   private String readStringLiteral (int c)
         throws ParseException
   {
      final StringBuffer sb = new StringBuffer();
      sb.append((char) c);
      for (;;)
      {
         final int d = read();
         sb.append((char) d);

         if (d == '"' || d == '\'')
         {
            break;
         }
      }
      return sb.toString();
   }

   private static boolean isSpecialCharacter (char c)
   {
      return (Character.isWhitespace(c) || c == '(' || c == ')'
            || c == ';' || c == ',' || c == '-');
   }

   private Token eatNewline (int c)
         throws ParseException
   {
      final Token t;
      if (c == Constants.LINE_FEED_CHAR) // UNIX newline?
      {
         ++mLine; mColumn = 0;
         t = new Token(TokenType.NEWLINE, asString(Constants.LINE_FEED_CHAR));
      }
      else if (c == Constants.CARRIAGE_RETURN_CHAR) // WINDOWS newline?
      {
         mark();
         if (read() != Constants.LINE_FEED_CHAR) // eat LF
         {
            reset();
         }
         ++mLine; mColumn = 0;
         t = new Token(TokenType.NEWLINE,
                 asString(Constants.CARRIAGE_RETURN_CHAR)
                 + asString(Constants.LINE_FEED_CHAR));
      }
      else
      {
         throw new ParseException("Unexpected newline char '"
               + (char) c + "'", mLine, mColumn);
      }
      return t;
   }

   private Token eatWhitespaces (int c)
         throws ParseException
   {
      final StringBuffer sb = new StringBuffer();
      sb.append((char) c); // TODO: assertTrue(isWhitespace(c));
      for (;;)
      {
         mark();
         final int d = read();

         if (Character.isWhitespace((char) d)
               && ! isNewlineChar((char) d))
         {
            sb.append((char) d);
         }
         else // not a whitespace, or is newline
         {    // which must be reported separately
            reset();
            break;
         }
      }
      return new Token(TokenType.WHITESPACE, sb.toString());
   }

   private void reset ()
         throws ParseException
   {
      try
      {
         mInputStream.reset();
         mColumn = mSaveColumn;
      }
      catch (IOException e)
      {
         final ParseException pe
            = new ParseException(e, mLine, mColumn);
         pe.initCause(e);
         throw pe;
      }
   }

   private void mark ()
   {
      mSaveColumn = mColumn;
      mInputStream.mark(Integer.MAX_VALUE);
   }

   private static String asString (int c)
   {
      return Character.toString((char) c);
   }

   private int read ()
         throws ParseException
   {
      int c = -1;
      try
      {
         ++mColumn;
         c = mInputStream.read();
      }
      catch (IOException e)
      {
         throw new ParseException(e, mLine, mColumn);
      }
      return c;
   }

   private static boolean isNewlineChar (char c)
   {
      return (c == Constants.LINE_FEED_CHAR
              || c == Constants.CARRIAGE_RETURN_CHAR);
   }

   /**
    * Simple SQL Scanner that reads the file given at argument 1 and dumps
    * the tokens to <code>stderr</code> and the content on <code>stdout</code>.
    *
    * @param args command line arguments
    * @throws Exception An error occurred
    */
   public static void main (String[] args)
         throws Exception
   {
      final SqlScanner scanner
         = new SqlScanner(new FileInputStream(args[0]));

      final List tokens = new ArrayList();

      for (;;)
      {
         final Token t = scanner.nextToken();
         System.err.println(scanner.getLine() + ": "
               + scanner.getColumn() + " = " + t);
         tokens.add(t);
         if (t.getType() == TokenType.EOF)
         {
            break;
         }
      }

      for (final Iterator iterator = tokens.iterator(); iterator.hasNext();)
      {
         final Token t = (Token) iterator.next();
         System.out.print(t.getValue());
      }
      System.out.flush();
   }
}
