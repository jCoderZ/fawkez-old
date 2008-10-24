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

import com.vladium.emma.data.ClassDescriptor;
import com.vladium.emma.data.DataFactory;
import com.vladium.emma.data.ICoverageData;
import com.vladium.emma.data.IMergeable;
import com.vladium.emma.data.IMetaData;
import com.vladium.emma.data.MethodDescriptor;
import com.vladium.emma.data.ICoverageData.DataHolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.jcoderz.phoenix.report.jaxb.Item;
import org.jcoderz.phoenix.report.jaxb.ObjectFactory;

/**
 * Reads the coverage report generated by emma (http://emma.sourceforge.net/).
 *
 * @author Andreas Mandel
 */
public class EmmaReportReader
        extends AbstractReportReader
{
    /** JAXB context path. */
    public static final String JCOVERAGE_JAXB_CONTEXT_PATH
            = "org.jcoderz.phoenix.coverage.jaxb";

    private static final String CLASSNAME
            = EmmaReportReader.class.getName();

    private static final Logger logger = Logger.getLogger(CLASSNAME);

    /**
     * Used if all branches of a line are covered. If there are 2
     * branches in a line 1/2 of this value is assigned as hit
     * counter.
     */
    private static final int EMMA_FULL_PERCENTAGE = 100;

    private IMetaData mEmmaMetaData;
    private ICoverageData mEmmaCoverageData;

    EmmaReportReader ()
            throws JAXBException
    {
        super(JCOVERAGE_JAXB_CONTEXT_PATH);
    }

    /** {@inheritDoc} */
    public final void parse (File f)
            throws JAXBException
    {
        try
        {
            final IMergeable[] emmaReport = DataFactory.load(f);
            mEmmaMetaData = (IMetaData) emmaReport[DataFactory.TYPE_METADATA];
            mEmmaCoverageData = (ICoverageData)
                emmaReport[DataFactory.TYPE_COVERAGEDATA];
            if (mEmmaMetaData == null)
            {
                logger.warning(
                    "Read no meta data from emma in file '" + f + "'." );
            }
            if (mEmmaCoverageData == null)
            {
                logger.warning(
                    "Read no coverage info from emma in file '" + f + "'." );
            }
        }
        catch (IOException e)
        {
            throw new JAXBException("Cannot read Emma report at '"
                +  f + "'.", e);
        }
    }

    /** {@inheritDoc} */
    public final Map getItems ()
        throws JAXBException
    {
        final Map itemMap = new HashMap();

        final Iterator i = mEmmaMetaData.iterator();
        while (i.hasNext())
        {
            ClassDescriptor clazz = (ClassDescriptor) i.next();
            final String srcFileName = clazz.getSrcFileName();
            final String fileName;
            if (srcFileName != null)
            {
                fileName = srcFileName.substring(
                    0, srcFileName.lastIndexOf('.'));
            }
            else
            {   // fallback if data is not available.
                fileName = clazz.getClassVMName().substring(
                    clazz.getClassVMName().lastIndexOf('/') + 1);
            }
            final String classname
                = clazz.getClassVMName().substring(
                    clazz.getClassVMName().lastIndexOf('/') + 1);
            final ResourceInfo source
                = ResourceInfo.lookup(
                    clazz.getPackageVMName().replaceAll("/", "."), fileName);
            if (source != null)
            {
                if (logger.isLoggable(Level.FINER))
                {
                    logger.finer(
                        "Processing coverage info for resource " + source);
                }
                processClazz(itemMap, source, clazz,
                   mEmmaCoverageData == null
                       ? null : mEmmaCoverageData.getCoverage(clazz));
            }
            else
            {
                if (logger.isLoggable(Level.FINER))
                {
                    logger.finer(
                        "Ignoring coverage info for class "
                        + clazz.getPackageVMName().replaceAll("/", ".") + "."
                        + classname + "@" + clazz.getSrcFileName());
                }
            }
        }
        return itemMap;
    }

    private void processClazz (Map itemMap, ResourceInfo source,
        final ClassDescriptor clazz, DataHolder coverage)
            throws JAXBException
    {
        logger.fine("Processing class '" + clazz.getName() + "'");

        final Map<Integer, CoverageDetail> lineCoverage
            = collectLineCoverage(clazz, coverage);

        final List<Item> itemList
            = createItemEntries(lineCoverage);

        if (!itemList.isEmpty())
        {
            if (itemMap.containsKey(source))
            {
               final List l = (List) itemMap.get(source);
               l.addAll(itemList);
            }
            else
            {
               itemMap.put(source, itemList);
            }
        }
    }

    /**
     * Collect all counters per line number mapping.
     * Emma has a block view on the source but we need a line
     * by line info.
     * @param clazz the static info from emma
     * @param coverage the dynamic coverage data
     * @return a map mapping from line number to coverage data.
     */
    private Map<Integer, CoverageDetail> collectLineCoverage (
        final ClassDescriptor clazz, DataHolder coverage)
    {
        MethodDescriptor[] methods = clazz.getMethods();
        final Map<Integer,CoverageDetail> lineCoverage
            = new HashMap<Integer,CoverageDetail>();
        for (int methodNr = 0; methodNr < methods.length; methodNr++)
        {
            final MethodDescriptor method = methods[methodNr];
            if (method.getBlockSizes() != null
                && method.getBlockMap() != null)
            {
                boolean[] methodCoverage = null;
                if (coverage!= null && coverage.m_coverage.length > methodNr)
                {
                    methodCoverage = coverage.m_coverage[methodNr];
                }
                final int[][] map = method.getBlockMap();
                for (int blockNr = 0;
                    blockNr < map.length; blockNr++)
                {
                    int[] blockLines = map[blockNr];
                    if (methodCoverage != null
                        && methodCoverage.length > blockNr
                        && methodCoverage[blockNr])
                    {
                        markCovered(lineCoverage, blockLines);
                    }
                    else
                    {
                        markNotCovered(lineCoverage, blockLines);
                    }
                }
            }
        }
        return lineCoverage;
    }

    /**
     * Creates finding report entries out of the line info.
     * @param lineCoverage map from line number to its coverage info.
     * @return a list of finding items.
     * @throws JAXBException the the object creation for the jaxb
     *  objects fails.
     */
    private List<Item> createItemEntries (
        final Map<Integer, CoverageDetail> lineCoverage)
        throws JAXBException
    {
        final List<Item> itemList = new ArrayList();
        for (Entry<Integer, CoverageDetail> entry : lineCoverage.entrySet())
        {
           final CoverageDetail c = entry.getValue();
           final Item item = new ObjectFactory().createItem();
           item.setOrigin(Origin.COVERAGE);
           if (c.mNotVisitedBranches > 0)
           {
               final int branches
                   = c.mVisitedBranches + c.mNotVisitedBranches;
               item.setCounter(
                   (EMMA_FULL_PERCENTAGE * c.mVisitedBranches)
                       / branches);
           }
           else
           {
               item.setCounter(EMMA_FULL_PERCENTAGE);
           }
           item.setLine(entry.getKey());
           item.setSeverity(Severity.COVERAGE);
           item.setFindingType("coverage"); // FIXME: use type
           itemList.add(item);
       }
       return itemList;
    }

    private void markCovered (Map<Integer, CoverageDetail> lineCoverage,
        int[] lines)
    {
        for (int line : lines)
        {
            getLine(lineCoverage, line).mVisitedBranches++;
        }
    }

    private void markNotCovered (Map<Integer, CoverageDetail> lineCoverage,
        int[] lines)
    {
        for (int line : lines)
        {
            getLine(lineCoverage, line).mNotVisitedBranches++;
        }
    }

    private CoverageDetail getLine (
        Map<Integer, CoverageDetail> lineCoverage, int line)
    {
        CoverageDetail result = lineCoverage.get(line);
        if (result == null)
        {
            result = new CoverageDetail();
            lineCoverage.put(line, result);
        }
        return result;
    }


    private class CoverageDetail
    {
        int mNotVisitedBranches;
        int mVisitedBranches;
    }
}
