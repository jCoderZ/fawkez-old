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

/**
 * The token class hold a token type and the corresponding value.
 * @author Michael Griffel
 */
public final class Token
{
   private final TokenType mType;
   private final String mValue;
   
   /**
    * Constructor for token w/o a value.
    * @param type token type.
    */
   public Token (TokenType type)
   {
      mType = type;
      mValue = "";
   }

   /**
    * Constructor for token w/ value.
    * @param type the token type.
    * @param value the token value.
    */
   public Token (TokenType type, String value)
   {
      mType = type;
      mValue = value;
   }
   
   /**
    * Returns the token type of this token.
    * @return the token type of this token.
    */
   public TokenType getType ()
   {
      return mType;
   }

   /**
    * Returns the value of this token.
    * @return the value of this token.
    */
   public String getValue ()
   {
      return mValue;
   }
   
   /**
    * Returns the string representation of this token.
    * @return the string representation of this token.
    */
   public String toString ()
   {
      return "[TOKEN: " + mType.toString() + ": '" + mValue + "']";
   }
}
