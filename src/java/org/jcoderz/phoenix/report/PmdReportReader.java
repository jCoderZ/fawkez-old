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
package org.jcoderz.phoenix.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.jcoderz.phoenix.pmd.jaxb.FileType;
import org.jcoderz.phoenix.pmd.jaxb.Pmd;
import org.jcoderz.phoenix.pmd.jaxb.Violation;
import org.jcoderz.phoenix.report.jaxb.Item;
import org.jcoderz.phoenix.report.jaxb.ObjectFactory;

/**
 * PMD Report Reader.
 *
 * @author Michael Griffel
 */
public final class PmdReportReader
      extends AbstractReportReader
{
    /** JAXB context path. */
    public static final String PMD_JAXB_CONTEXT_PATH
            = "org.jcoderz.phoenix.pmd.jaxb";

    private static final transient String CLASSNAME
            = PmdReportReader.class.getName();

    private static final transient Logger logger = Logger.getLogger(CLASSNAME);

    private static final int PRIORITY_HIGH = 1;
    private static final int PRIORITY_MEDIUM_HIGH = 2;
    private static final int PRIORITY_MEDIUM = 3;
    private static final int PRIORITY_MEDIUM_LOW = 4;
    private static final int PRIORITY_LOW = 5;

    private Pmd mReportDocument;

    /**
     * Constructor.
     *
     * @throws JAXBException
     */
    public PmdReportReader ()
            throws JAXBException
    {
        super(PMD_JAXB_CONTEXT_PATH);
    }

    /**
     * @see org.jcoderz.phoenix.report.ReportReader#parse(java.io.File)
     */
    public void parse (File f)
            throws JAXBException, FileNotFoundException
    {
        mReportDocument = (Pmd) getUnmarshaller().unmarshal(
                new FileInputStream(f));
    }

    /**
     * @see org.jcoderz.phoenix.report.AbstractReportReader#getItems()
     */
    protected Map getItems ()
            throws JAXBException
    {
        final Map result = new HashMap();

        for (final Iterator iterator = mReportDocument.getFile().iterator();
                iterator.hasNext();)
        {
            final FileType file
                    = (org.jcoderz.phoenix.pmd.jaxb.FileType) iterator.next();

            final String key = normalizeFileName(file.getName());
            final List items = createItemMap(file);
            final ResourceInfo info = ResourceInfo.lookup(key);
            if (info != null)
            {
                result.put(info, items);
            }
            else
            {
                logger.finer("Ingoring findings for resource " + key);
            }
        }
        return result;
    }

    private List createItemMap (org.jcoderz.phoenix.pmd.jaxb.FileType file)
            throws JAXBException
    {
        final List items = new ArrayList();
        for (final Iterator iterator = file.getViolation().iterator(); iterator
                .hasNext();)
        {
            final Violation violation = (Violation) iterator.next();

            final Item item = new ObjectFactory().createItem();
            item.setMessage(violation.getValue().trim());
            item.setOrigin(Origin.PMD);
            item.setSeverity(mapPriority(violation.getPriority()));
            item.setFindingType(violation.getRule());
            item.setLine(violation.getLine());

            items.add(item);
        }
        return items;
    }

    private Severity mapPriority (int pmdPriority)
    {
        final Severity ret;

        switch (pmdPriority)
        {
            case PRIORITY_HIGH:
                ret = Severity.ERROR;
                break;

            case PRIORITY_MEDIUM_HIGH:
            case PRIORITY_MEDIUM:
                ret = Severity.WARNING;
                break;

            case PRIORITY_MEDIUM_LOW:
            case PRIORITY_LOW:
                ret = Severity.INFO;
                break;

            default:
                ret = Severity.WARNING;
        }
        return ret;
    }
}
