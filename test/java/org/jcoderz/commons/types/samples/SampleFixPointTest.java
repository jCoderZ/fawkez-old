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
package org.jcoderz.commons.types.samples;

import java.math.BigDecimal;

import org.jcoderz.commons.ArgumentFractionDigitsViolationException;
import org.jcoderz.commons.ArgumentMalformedException;
import org.jcoderz.commons.ArgumentMaxValueViolationException;
import org.jcoderz.commons.ArgumentMinValueViolationException;

import junit.framework.TestCase;

/**
 * Test the FixPoint class generator.
 *
 * @author Andreas Mandel
 */
public class SampleFixPointTest
    extends TestCase
{
    private static final int INT_TEST_VALUE = -100;
    private static final long LONG_TEST_VALUE = 40;

    /**
     * Test method for {@link org.jcoderz.commons.types.samples.SampleFixPoint#hashCode()}.
     */
    public void testHashCode ()
    {
        assertFalse("Different values should have different hashCode.",
            SampleFixPoint.fromString("1").hashCode()
            == SampleFixPoint.fromString("2").hashCode());
    }

    /**
     * Test method for {@link org.jcoderz.commons.types.samples.SampleFixPoint#hashCode()}.
     */
    public void testHashCodeSame ()
    {
        assertEquals("Same values should have different hashCode.",
            SampleFixPoint.fromString("1").hashCode(),
            SampleFixPoint.fromString("1").hashCode());
    }
    /**
     * Test method for {@link org.jcoderz.commons.types.samples.SampleFixPoint#intValue()}.
     */
    public void testIntValue ()
    {
        assertEquals("Int value should not change.", INT_TEST_VALUE,
            SampleFixPoint.valueOf(INT_TEST_VALUE).intValue());
    }

    /**
     * Test method for {@link org.jcoderz.commons.types.samples.SampleFixPoint#longValue()}.
     */
    public void testLongValue ()
    {
        assertEquals("Long value should not change.", LONG_TEST_VALUE,
            SampleFixPoint.valueOf(LONG_TEST_VALUE).longValue());
    }

    /**
     * Test method for {@link org.jcoderz.commons.types.samples.SampleFixPoint#fromString(java.lang.String)}.
     */
    public void testFromString ()
    {
        assertEquals("From string should not change value.", LONG_TEST_VALUE,
            SampleFixPoint.fromString(
                String.valueOf(LONG_TEST_VALUE)).longValue());
    }

    /**
     * Test method for {@link org.jcoderz.commons.types.samples.SampleFixPoint#fromString(java.lang.String)}.
     */
    public void testFromStringDecimal ()
    {
        final BigDecimal test = new BigDecimal("-1.23");
        assertEquals("From string should not change value.", test,
            SampleFixPoint.fromString(String.valueOf(test)).toBigDecimal());
    }

    /**
     * Test method for {@link org.jcoderz.commons.types.samples.SampleFixPoint#valueOf(java.math.BigDecimal)}.
     */
    public void testValueOfBigDecimal ()
    {
        final BigDecimal test = new BigDecimal("-1.23");
        assertEquals("From big decimal should not change value.", test,
            SampleFixPoint.valueOf(test).toBigDecimal());
    }

    /**
     * Test method for fraction digit overflow.
     */
    public void testFractionDigits ()
    {
        try
        {
            SampleFixPoint s = SampleFixPoint.fromString("1.234");
            fail("Expected exception but got " + s + ".");
        }
        catch (ArgumentFractionDigitsViolationException ex)
        {
            // Expected...
        }
    }

    /**
     * Test method for max value overflow.
     */
    public void testMaxValue ()
    {
        try
        {
            SampleFixPoint s = SampleFixPoint.fromString("12345.67");
            fail("Expected exception but got " + s + ".");
        }
        catch (ArgumentMaxValueViolationException ex)
        {
            // Expected...
        }
    }

    /**
     * Test method for min value overflow.
     */
    public void testMinValue ()
    {
        try
        {
            SampleFixPoint s = SampleFixPoint.fromString("-345.67");
            fail("Expected exception but got " + s + ".");
        }
        catch (ArgumentMinValueViolationException ex)
        {
            // Expected...
        }
    }

    /**
     * Test method for malformed string.
     */
    public void testMalformedString ()
    {
        try
        {
            SampleFixPoint s = SampleFixPoint.fromString("0xFF12");
            fail("Expected exception but got " + s + ".");
        }
        catch (ArgumentMalformedException ex)
        {
            // Expected...
        }
    }

    /**
     * Test method for comparison.
     */
    public void testComparison ()
    {
        assertEquals("Comparing equal values", 0,
            SampleFixPoint.fromString("0.01")
            .compareTo(SampleFixPoint.fromString("0.01")));
        assertTrue("Comparing different values",
            SampleFixPoint.fromString("0.01")
                .compareTo(SampleFixPoint.fromString("0.02")) < 0);
        assertTrue("Comparing different values",
            SampleFixPoint.fromString("0.02")
                .compareTo(SampleFixPoint.fromString("0.01")) > 0);
    }

    /**
     * Test value of static members.
     */
    public void testStatics ()
    {
        assertEquals("SampleFixPoint.MAX_VALUE_SCALED",
            999, SampleFixPoint.MAX_VALUE_SCALED);
        assertEquals("SampleFixPoint.MIN_VALUE_SCALED",
            -100, SampleFixPoint.MIN_VALUE_SCALED);
        assertEquals("SampleFixPoint.MAX_VALUE",
            "999.99", SampleFixPoint.MAX_VALUE.toString());
        assertEquals("SampleFixPoint.MIN_VALUE",
            "-100.50", SampleFixPoint.MIN_VALUE.toString());
        assertEquals("SampleFixPoint.SCALE",
            2, SampleFixPoint.SCALE);
        assertEquals("SampleFixPoint.DECIMAL_SCALE",
            100, SampleFixPoint.DECIMAL_SCALE);
        assertEquals("SampleFixPoint.TOTAL_DIGITS",
            5, SampleFixPoint.TOTAL_DIGITS);
    }
}
