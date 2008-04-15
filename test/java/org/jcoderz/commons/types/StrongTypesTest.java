/*
 * $Id: CommonsTestRunnerException.java 1 2006-11-25 14:41:52Z amandel $
 *
 * Copyright 2008, The jCoderZ.org Project. All rights reserved.
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
package org.jcoderz.commons.types;

import org.jcoderz.commons.ArgumentMaxLengthViolationException;
import org.jcoderz.commons.ArgumentMaxValueViolationException;
import org.jcoderz.commons.ArgumentMinLengthViolationException;
import org.jcoderz.commons.ArgumentMinValueViolationException;
import org.jcoderz.commons.config.ConfigurationKey;
import org.jcoderz.commons.test.RestrictedLong;

import junit.framework.TestCase;

/**
 * Test the generated Strong Types.
 * @author Andreas Mandel
 */
public class StrongTypesTest
    extends TestCase
{
    private static final int LONG_STRING_LENGTH = 2048;
    private static final String LONG_STRING;

    static
    {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < LONG_STRING_LENGTH; i++)
        {
           sb.append(' ');
        }
        LONG_STRING = sb.toString();
    }

    /**
     * Testing the max length restriction in generated String types.
     */
    public void testToLong ()
    {
        try
        {
            final ConfigurationKey key
                = ConfigurationKey.fromString(LONG_STRING.substring(0,
                    ConfigurationKey.MAX_LENGTH + 1));
            fail("Expected exception!");
        }
        catch (ArgumentMaxLengthViolationException ex)
        {
            // expected;
        }
    }

    /**
     * Testing the min length restriction in generated String types.
     */
    public void testToShort ()
    {
        try
        {
            final ConfigurationKey key
                = ConfigurationKey.fromString("");
            fail("Expected exception!");
        }
        catch (ArgumentMinLengthViolationException ex)
        {
            // expected;
        }
    }

    /**
     * Testing the max value restriction in generated Long types.
     */
    public void testToHigh ()
    {
        try
        {
            final RestrictedLong lg
                = RestrictedLong.fromLong(RestrictedLong.MAX_VALUE + 1);
            fail("Expected exception!");
        }
        catch (ArgumentMaxValueViolationException ex)
        {
            // expected;
        }
    }

    /**
     * Testing the min value restriction in generated Long types.
     */
    public void testToLow ()
    {
        try
        {
            final RestrictedLong lg
                = RestrictedLong.fromLong(RestrictedLong.MIN_VALUE - 1);
            fail("Expected exception!");
        }
        catch (ArgumentMinValueViolationException ex)
        {
            // expected;
        }
    }
}
