/*
 * $Id$
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

import junit.framework.TestCase;

/**
 * Test use of base class in the value objects.
 *
 *
 * @author Andreas Mandel
 */
public class BaseClassTest
    extends TestCase
{
    /**
     * Test the inheritance should call super.equals();
     */
    public void testInheritanceEquals()
    {
        final SampleValueObject value = new SampleValueObject();
        value.setTestValue(1);
        value.setTestValueBase(1);

        final SampleValueObject value2 = new SampleValueObject();
        value2.setTestValue(1);
        value2.setTestValueBase(0);

        assertFalse("Base class fields not honored in equals.",
            value.equals(value2));
    }

    /**
     * Test the inheritance should call super.equals();
     */
    public void testInheritanceHashCode()
    {
        final SampleValueObject value = new SampleValueObject();
        value.setTestValue(1);
        value.setTestValueBase(1);

        final SampleValueObject value2 = new SampleValueObject();
        value2.setTestValue(1);
        value2.setTestValueBase(0);

        assertTrue("Base class fields not honored in hash code.",
            value.hashCode() != value2.hashCode());
    }

    /**
     * Test the inheritance should call super.equals();
     */
    public void testInheritanceCopyConstructor()
    {
        final SampleValueObject value = new SampleValueObject();
        value.setTestValue(1);
        value.setTestValueBase(1);

        final SampleValueObject value2 = new SampleValueObject(value);

        assertEquals("Base class fields not honored in copy constructor.",
            value.getTestValueBase(), value2.getTestValueBase());
        assertEquals("Not equal after copy.",
            value, value2);
    }

    /**
     * Test the inheritance should call super.equals();
     */
    public void testInheritanceToString()
    {
        final SampleValueObject value = new SampleValueObject();
        value.setTestValue(1);
        value.setTestValueBase(0);

        assertTrue("Can't see base class fields in string output.",
            value.toString().indexOf("0") > -1);
    }
}
