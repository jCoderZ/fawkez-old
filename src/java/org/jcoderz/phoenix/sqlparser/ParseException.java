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
 * SQL ParseException.
 * 
 * @author Michael Griffel
 */
public final class ParseException
        extends Exception
{
    private static final long serialVersionUID = 1L;
    final int mLine;
    final int mColumn;

    /**
     * Create a new ParseException
     * 
     * @param line the source line where the exception occured
     * @param column the source column where the exception occured
     */
    public ParseException (int line, int column)
    {
        super();
        mLine = line;
        mColumn = column;
    }

    /**
     * Create a new ParseException
     * 
     * @param message a message describing the problem
     * @param line the source line where the exception occured
     * @param column the source column where the exception occured
     */
    public ParseException (String message, int line, int column)
    {
        super(message);
        mLine = line;
        mColumn = column;
    }

    /**
     * Create a new ParseException
     * 
     * @param cause the throwable that initiated this exception
     * @param line the source line where the exception occured
     * @param column the source column where the exception occured
     */
    public ParseException (Throwable cause, int line, int column)
    {
        super(cause);
        mLine = line;
        mColumn = column;
    }

    /**
     * Create a new ParseException
     * 
     * @param message a message describing the problem
     * @param cause the throwable that initiated this exception
     * @param line the source line where the exception occured
     * @param column the source column where the exception occured
     */
    public ParseException (String message, Throwable cause, 
            int line, int column)
    {
        super(message, cause);
        mLine = line;
        mColumn = column;
    }

    /**
     * Returns the column.
     * @return the column.
     */
    public int getColumn ()
    {
        return mColumn;
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
     * @see Throwable#getMessage()
     */
    public String getMessage ()
    {
        return "Parse error at line:" + getLine() + ", column:" + getColumn()
                + ": " + super.getMessage();
    }
}
