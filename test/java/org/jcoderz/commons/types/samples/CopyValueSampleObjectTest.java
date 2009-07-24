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

import java.util.Date;

import junit.framework.TestCase;

/**
 * Test class for {@link CopyValueSampleObject}.
 * @author Andreas Mandel
 */
public class CopyValueSampleObjectTest
    extends TestCase
{
    /** Check value modifications on a cloned object. */
    public void testClonedValue ()
    {
        final Date date = new Date();
        final BarValueObject bar
            = new BarValueObject(org.jcoderz.commons.types.Date.now());
        final CopyValueSampleObject test = new CopyValueSampleObject(date, bar);

        assertNotSame("Mutable object should have been cloned.",
            date, test.getModificationDate());

        date.setTime(0);
        assertFalse("Modification of date must not be propagated.",
            test.getModificationDate().getTime() == date.getTime());
    }

    /** Check value modifications on a object copied via copy constructor. */
    public void testCopyConstructorValue ()
    {
        final Date date = new Date();
        final BarValueObject bar
            = new BarValueObject(org.jcoderz.commons.types.Date.now());
        bar.setId(0);
        final CopyValueSampleObject test = new CopyValueSampleObject(date, bar);

        assertNotSame("Mutable object should have been cloned.",
            bar, test.getSampleBar());

        bar.setId(1);
        assertFalse("Modification of bar must not be propagated.",
            test.getSampleBar().getId() == bar.getId());
    }

    /** Check value modifications on a cloned object which is initialized with null. */
    public void testClonedValueNullInit ()
    {
        final Date date = new Date();
        final BarValueObject bar
            = new BarValueObject(org.jcoderz.commons.types.Date.now());
        final CopyValueSampleObject test = new CopyValueSampleObject(null, bar);
        test.setModificationDate(date);
        assertEquals("Object should be set and equal.", date, test.getModificationDate());
    }
}
