// BEGIN SNIPPET: JCoderZJavaExample.xml
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
package org.jcoderz.guidelines.snippets;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;


/**
 * This util class represents an Transaction Id with all
 * of its features and restrictions.
 * Instances of this class are immutable.
 *
 * @author SWAG
 */
public final class TransactionId
    implements Comparable, Serializable
{
    /** Name of this type. */
    public static final String TYPE_NAME = "TX_ID";

    /** Bit mask used for hashcode generation. */
    private static final int NUMBER_OF_BITS_PER_INT = 32;

    private static final int BUFFER_SIZE = 4096;

    private static final int BUFFER_MULTIPLIER = 2;

    private static final long serialVersionUID = -7064645359225861305L;

    /** Holds the transaction id */
    private final long mTransactionId;


    /**
     * Creates a new instance of TransactionId.
     *
     * @param transactionId the transaction to be represented by the
     *       <code>TransactionId</code>.
     * @throws IllegalArgumentException if the long does not fit
     *       into a transaction id.
     */
    private TransactionId (long transactionId)
        throws IllegalArgumentException
    {
        if (transactionId < 0)
        {
            throw new IllegalArgumentException(TYPE_NAME + " "
                    + String.valueOf(transactionId) 
                    + "Value must be positive.");
        }
        mTransactionId = transactionId;
    }


    /**
     * Parses the string argument as a transaction id.
     *
     * @param s the <code>String</code> containing the transaction id.
     * @return the transaction id represented by the string argument.
     * @throws IllegalArgumentException if the string does not contain a
     *       parseable transaction id.
     */
    public static TransactionId fromString (String s)
        throws IllegalArgumentException
    {
        final TransactionId result;
        try
        {
            result = new TransactionId(Long.parseLong(s));
        }
        catch (NumberFormatException ex)
        {
            final IllegalArgumentException iaex 
                    = new IllegalArgumentException(
                        TYPE_NAME + " Failed to parse the value.");
            iaex.initCause(ex);
            throw iaex;
        }
        catch (NullPointerException ex)
        {
            final IllegalArgumentException iaex 
                    = new IllegalArgumentException(
                        TYPE_NAME + " Value must not be null.");
            iaex.initCause(ex);
            throw iaex;
        }
        return result;
    }

    /**
     * Returns a transaction id from the given long <code>l</code>.
     *
     * @param l the <code>long</code> containing the transaction id.
     * @return the transaction id represented by the argument.
     * @throws IllegalArgumentException if the long does not fit into a
     *       transaction id.
     */
    public static TransactionId fromLong (long l)
        throws IllegalArgumentException
    {
        return new TransactionId(l);
    }

    /**
     * Returns the transaction id as String.
     *
     * @return the transaction id as String.
     */
    public String toString ()
    {
        return Long.toString(mTransactionId);
    }

    /**
     * Returns the transaction id as long.
     *
     * @return the transaction id as long.
     */
    public long toLong ()
    {
        return mTransactionId;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the object to compare this <code>TransactionId</code>
     *       against.
     * @return true if this object is the same as the obj argument; false
     *       otherwise.
     */
    public boolean equals (Object obj)
    {
        boolean result = false;

        if (obj instanceof TransactionId)
        {
            result = (mTransactionId 
                    == (((TransactionId) obj).mTransactionId));
        }

        return result;
    }

    /**
     * Compare two transaction IDs.
     * This implementation is consistent with {@link #equals(Object)}.
     *
     * @param o object with which to compare this TransactionId
     * @return a result less than zero if this object is less than
     *       <code>o</code>, exactly zero if they are equal, and a result
     *       greater than zero otherwise.
     */
    public int compareTo (Object o)
    {
        final int result;
        // Can't simply return the difference, because that difference
        // might not fit in an int.
        if (mTransactionId < ((TransactionId) o).mTransactionId)
        {
            result = -1;
        }
        else if (mTransactionId > ((TransactionId) o).mTransactionId)
        {
            result = 1;
        }
        else
        {
            result = 0;
        }
        return result;
    }

    /**
     * Compute hash code.
     *
     * @return hash code for this transaction ID
     */
    public int hashCode ()
    {
        return (int) (mTransactionId
                ^ (mTransactionId >>> NUMBER_OF_BITS_PER_INT));
    }

    /**
     * Helper function to read the full content of the file.
     *
     * @param file the file to read.
     * @return the content of the given file as byte array.
     * @throws IOException if a IOException occurs.
     */
    private static byte[] readFully (File file)
        throws IOException
    {
        final FileInputStream in = new FileInputStream(file);
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        int pos = 0;

        while ((read = in.read(buffer, pos, buffer.length - pos)) > 0)
        {
            pos += read;
            if (pos == buffer.length)
            {
                byte[] newBuffer 
                = new byte[buffer.length * BUFFER_MULTIPLIER];

                System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
                buffer = newBuffer;
            }
        }

        if (pos != buffer.length)
        {
            byte[] newBuffer = new byte[pos];

            System.arraycopy(buffer, 0, newBuffer, 0, pos);
            buffer = newBuffer;
        }

        return buffer;
    }
}
// END SNIPPET
