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
package org.jcoderz.commons.util;

import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

import org.jaxen.JaxenException;
import org.jcoderz.commons.ArgumentMalformedException;
import org.jcoderz.commons.Loggable;
import org.xml.sax.SAXException;

/**
 * Tests the ThrowableUtil class.
 * @author Andreas Mandel
 */
public class ThrowableUtilTest
extends TestCase
{
    /** Test the fixChaining method. */
    public void testFixChaining ()
    {
        final RuntimeException in = new RuntimeException("in");
        final SAXException sax
        = new org.xml.sax.SAXParseException("SAX", null, in);
        final JAXBException jaxb = new JAXBException(sax);
        final RuntimeException out = new RuntimeException("Outer", jaxb);

        assertEquals("1st nesting level unexpected", jaxb, out.getCause());
        assertEquals("2nd nesting level unexpected (pre chain)",
                null, jaxb.getCause());
        assertEquals("3rd nesting level unexpected (pre chain)",
                null, sax.getCause());

        ThrowableUtil.fixChaining(out);

        assertEquals("2nd nesting level unexpected", sax, jaxb.getCause());
        assertEquals("3rd nesting level unexpected", in, sax.getCause());
    }

    /** Test the magic get cause detection code for certain Throwables. */
    public void testGetCauseDetection ()
    {
        getCauseDetectionTestHelper(SAXException.class, "getException");
        getCauseDetectionTestHelper(ServletException.class, "getRootCause");
        getCauseDetectionTestHelper(UnavailableException.class, "getRootCause");
        getCauseDetectionTestHelper(
                javax.resource.spi.UnavailableException.class,
                "getLinkedException");
        getCauseDetectionTestHelper(JaxenException.class, "getCause");
    }

    /** Test {@link ThrowableUtil#collectNestedData(Loggable)}. */
    public void testCollectNestedData ()
    {
        final SAXException sax
            = new org.xml.sax.SAXParseException(
                "SAX", "public id", "system id", 5, 4);
        final ArgumentMalformedException ex
            = new ArgumentMalformedException("TEST", "VALUE", "Hint",
                sax);
        assertEquals("Parameter from nested Exception not found in list. "
            + ex.getParameterNames(), "public id",
            ex.getParameter(
                "CAUSE_1_org.xml.sax.SAXParseException#PublicId").get(0));
    }

    private void getCauseDetectionTestHelper (Class ex, String methodName)
    {
        final Method m
            = ThrowableUtil.findGetCauseMethod(ex.getMethods());
        assertNotNull("Could not get getCause method for " + ex.getName(), m);
        assertEquals("Differen method expected for getCause in " + ex.getName(),
                methodName, m.getName());
    }
}
