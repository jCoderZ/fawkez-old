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


import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;


/**
 * Enumerated type of a token type.
 *
 * Instances of this class are immutable.
 *
 * The following token types are defined:
 * <ul>
 *    <li>TokenType.CREATE</li>
 *    <li>TokenType.TABLE</li>
 *    <li>TokenType.OPEN_PAREN</li>
 *    <li>TokenType.CLOSE_PAREN</li>
 *    <li>TokenType.IDENTIFIER</li>
 *    <li>TokenType.COMMA</li>
 *    <li>TokenType.SEMICOLON</li>
 *    <li>TokenType.WHITESPACE</li>
 *    <li>TokenType.NEWLINE</li>
 *    <li>TokenType.STRING_LITERAL</li>
 *    <li>TokenType.NUMERIC_LITERAL</li>
 *    <li>TokenType.COMMENT</li>
 *    <li>TokenType.NOT</li>
 *    <li>TokenType.NULL</li>
 *    <li>TokenType.CONSTRAINT</li>
 *    <li>TokenType.PRIMARY</li>
 *    <li>TokenType.KEY</li>
 *    <li>TokenType.UNIQUE</li>
 *    <li>TokenType.CHECK</li>
 *    <li>TokenType.IN</li>
 *    <li>TokenType.EOF</li>
 * </ul>
 *
 * @author Michael Griffel
 */
public final class TokenType
        implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** The name of the token type */
    private final transient String mName;

    /** Ordinal of next token type to be created */
    private static int sNextOrdinal = 0;

    /** Assign a ordinal to this token type */
    private final int mOrdinal = sNextOrdinal++;

    /** Maps a string representation to an enumerated value */
    private static final Map FROM_STRING = new HashMap();

    /** The token type create */
    public static final TokenType CREATE = new TokenType("create");

    /** The token type table */
    public static final TokenType TABLE = new TokenType("table");

    /** The token type open_paren */
    public static final TokenType OPEN_PAREN = new TokenType("open_paren");

    /** The token type close_paren */
    public static final TokenType CLOSE_PAREN = new TokenType("close_paren");

    /** The token type identifier */
    public static final TokenType IDENTIFIER = new TokenType("identifier");

    /** The token type comma */
    public static final TokenType COMMA = new TokenType("comma");

    /** The token type semicolon */
    public static final TokenType SEMICOLON = new TokenType("semicolon");

    /** The token type slash */
    public static final TokenType SLASH = new TokenType("slash");

    /** The token type whitespace */
    public static final TokenType WHITESPACE = new TokenType("whitespace");

    /** The token type newline */
    public static final TokenType NEWLINE = new TokenType("newline");

    /** The token type string_literal */
    public static final TokenType STRING_LITERAL = new TokenType(
            "string_literal");

    /** The token type numeric_literal */
    public static final TokenType NUMERIC_LITERAL = new TokenType(
            "numeric_literal");

    /** The token type comment */
    public static final TokenType COMMENT = new TokenType("comment");

    /** The token type not */
    public static final TokenType NOT = new TokenType("not");

    /** The token type null */
    public static final TokenType NULL = new TokenType("null");

    /** The token type constraint */
    public static final TokenType CONSTRAINT = new TokenType("constraint");

    /** The token type primary */
    public static final TokenType PRIMARY = new TokenType("primary");

    /** The token type key */
    public static final TokenType KEY = new TokenType("key");

    /** The token type unique */
    public static final TokenType UNIQUE = new TokenType("unique");

    /** The token type check */
    public static final TokenType CHECK = new TokenType("check");

    /** The token type in */
    public static final TokenType IN = new TokenType("in");

    /** The token type eof */
    public static final TokenType EOF = new TokenType("eof");

    /** The token type foreign */
    public static final TokenType FOREIGN = new TokenType("foreign");

    /** The token type default */
    public static final TokenType DEFAULT = new TokenType("default");

    /** The token type references */
    public static final TokenType REFERENCES = new TokenType("references");

    /** The token type on */
    public static final TokenType ON = new TokenType("on");

    /** The token type delete */
    public static final TokenType DELETE = new TokenType("delete");

    /** The token type set */
    public static final TokenType SET = new TokenType("set");

    /** The token type cascade */
    public static final TokenType CASCADE = new TokenType("cascade");

    /** The token type enable */
    public static final TokenType ENABLE = new TokenType("enable");

    /** The token type disable */
    public static final TokenType DISABLE = new TokenType("disable");

    /** The token type alter */
    public static final TokenType ALTER = new TokenType("alter");

    /** The token type drop */
    public static final TokenType DROP = new TokenType("drop");

    /** The token type insert */
    public static final TokenType INSERT = new TokenType("insert");

    /** The token type delete */
    public static final TokenType SELECT = new TokenType("select");

    /** The token type index */
    public static final TokenType INDEX = new TokenType("index");

    /** The token type bitmap */
    public static final TokenType BITMAP = new TokenType("bitmap");

    /** The token type sequence */
    public static final TokenType SEQUENCE = new TokenType("sequence");

    /** The token type increment */
    public static final TokenType INCREMENT = new TokenType("increment");

    public static final TokenType BY = new TokenType("by");

    public static final TokenType START = new TokenType("start");

    public static final TokenType WITH = new TokenType("with");

    public static final TokenType MAXVALUE = new TokenType("maxvalue");

    public static final TokenType NOMAXVALUE = new TokenType("nomaxvalue");

    public static final TokenType MINVALUE = new TokenType("minvalue");

    public static final TokenType NOMINVALUE = new TokenType("nominvalue");

    public static final TokenType CYCLE = new TokenType("cycle");

    public static final TokenType NOCYCLE = new TokenType("nocycle");

    public static final TokenType CACHE = new TokenType("cache");

    public static final TokenType NOCACHE = new TokenType("nocache");

    public static final TokenType ORDER = new TokenType("order");

    public static final TokenType NOORDER = new TokenType("noorder");

    public static final TokenType OPERATOR = new TokenType("operator");

    /** Internal list of all available token types */
    private static final TokenType[] PRIVATE_VALUES = {TokenType.CREATE,
            TokenType.TABLE, TokenType.OPEN_PAREN, TokenType.CLOSE_PAREN,
            TokenType.IDENTIFIER, TokenType.COMMA, TokenType.SEMICOLON,
            TokenType.SLASH, TokenType.WHITESPACE, TokenType.NEWLINE,
            TokenType.STRING_LITERAL, TokenType.NUMERIC_LITERAL,
            TokenType.COMMENT, TokenType.NOT, TokenType.NULL,
            TokenType.CONSTRAINT, TokenType.PRIMARY, TokenType.KEY,
            TokenType.UNIQUE, TokenType.CHECK, TokenType.IN, TokenType.EOF,
            TokenType.FOREIGN, TokenType.DEFAULT, TokenType.REFERENCES,
            TokenType.ON, TokenType.DELETE, TokenType.SET, TokenType.CASCADE,
            TokenType.ENABLE, TokenType.DISABLE, TokenType.ALTER,
            TokenType.DROP, TokenType.INSERT, TokenType.SELECT,
            TokenType.INDEX, TokenType.BITMAP, TokenType.SEQUENCE,
            TokenType.INCREMENT, TokenType.BY, TokenType.START, TokenType.WITH,
            TokenType.MAXVALUE, TokenType.NOMAXVALUE, TokenType.MINVALUE,
            TokenType.NOMINVALUE, TokenType.CYCLE, TokenType.NOCYCLE,
            TokenType.CACHE, TokenType.NOCACHE, TokenType.ORDER,
            TokenType.NOORDER, TokenType.OPERATOR};

    /** Immutable list of the token types */
    public static final List VALUES = Collections.unmodifiableList(Arrays
            .asList(PRIVATE_VALUES));

    /** Private Constructor */
    private TokenType (String name)
    {
        mName = name;
        FROM_STRING.put(mName, this);
    }

    /**
     * Creates a TokenType object from its int representation.
     *
     * @param i the int representation of the token type to be returned.
     * @return the TokenType object represented by this int.
     * @throws IllegalArgumentException If the assigned int value isn't 
     *      listed in the internal token type table
     */
    public static TokenType fromInt (int i)
            throws IllegalArgumentException
    {
        try
        {
            return PRIVATE_VALUES[i];
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new IllegalArgumentException(
                    "Illegal int representation of TokenType");
        }
    }

    /**
     * Creates a TokenType object from its String representation.
     *
     * @param str the str representation of the token type to be returned.
     * @return the TokenType object represented by this str.
     * @throws IllegalArgumentException If the given str value isn't listed in the
     *         internal token type table
     */
    public static TokenType fromString (String str)
            throws IllegalArgumentException
    {
        final TokenType result = (TokenType) FROM_STRING.get(str);
        if (result == null)
        {
            throw new IllegalArgumentException(
                    "Illegal string representation of TokenType");
        }
        return result;
    }

    /**
     * Returns the int representation of this token type.
     *
     * @return the int representation of this token type.
     */
    public int toInt ()
    {
        return mOrdinal;
    }

    /**
     * Returns the String representation of this token type.
     *
     * @return the String representation of this token type.
     */
    public String toString ()
    {
        return mName;
    }

    /**
     * Resolves instances being deserialized to a single instance
     * per token type.
     */
    private Object readResolve ()
            throws ObjectStreamException
    {
        return PRIVATE_VALUES[mOrdinal];
    }
}
