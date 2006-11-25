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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.jcoderz.phoenix.jcoverage.jaxb.Clazz;
import org.jcoderz.phoenix.jcoverage.jaxb.Coverage;
import org.jcoderz.phoenix.jcoverage.jaxb.LineType;
import org.jcoderz.phoenix.report.jaxb.Item;
import org.jcoderz.phoenix.report.jaxb.ObjectFactory;

/**
 * @author Michael Griffel
 */
public class JCoverageReportReader
      extends AbstractReportReader
{
   private static final transient String CLASSNAME
      = JCoverageReportReader.class.getName();

   private static final transient Logger logger
      = Logger.getLogger(CLASSNAME);

   private Coverage mReportDocument;

   /** JAXB context path */
   public static final String JCOVERAGE_JAXB_CONTEXT_PATH
      = "org.jcoderz.phoenix.jcoverage.jaxb";


   JCoverageReportReader ()
      throws JAXBException
   {
      super(JCOVERAGE_JAXB_CONTEXT_PATH);
   }

   /**
    * @see org.jcoderz.phoenix.report.ReportReader#parse(File)
    */
   public final void parse (File f)
      throws JAXBException
   {
      try
      {
         mReportDocument = (Coverage) getUnmarshaller().unmarshal(
                 new FileInputStream(f));
      }
      catch (IOException e)
      {
         throw new JAXBException("Cannot read JCoverage report", e);
      }
   }

   /**
    * @see org.jcoderz.phoenix.report.AbstractReportReader#getItems()
    */
   public final Map getItems ()
      throws JAXBException
   {
      Map itemMap = new HashMap();

      List files = mReportDocument.getClazzes();

      final String baseDir = mReportDocument.getSrc() + File.separator;

      for (Iterator iterator = files.iterator(); iterator.hasNext(); )
      {
         Clazz clazz = (Clazz) iterator.next();
         logger.finer("Processing class '" + clazz.getName() + "'");
         String javaFile = clazzname2Filename (clazz.getName());
         List itemList = new ArrayList();

         for (Iterator i = clazz.getCoveredLines().iterator(); i.hasNext(); )
         {
            LineType line = (LineType) i.next();
            Item item = new ObjectFactory().createItem();
            item.setOrigin(Origin.COVERAGE);
            item.setCounter(line.getHits());
            item.setLine(line.getNumber());
            item.setSeverity(Severity.COVERAGE);
            item.setFindingType("coverage"); // FIXME: use type

            itemList.add(item);
         }
         final ResourceInfo info
               = ResourceInfo.lookup(normalizeFileName(baseDir + javaFile));

         if (info != null)
         {
            if (itemMap.containsKey(info))
            {
               List l = (List) itemMap.get(info);
               l.addAll(itemList);
            }
            else
            {
               itemMap.put(info, itemList);
            }
         }
         else
         {
            logger.finer("Ignoring findings for resource " + baseDir + javaFile);
         }
      }

      return itemMap;
   }

   /**
    * @param string
    * @return
    */
   private final String clazzname2Filename (String c)
   {
      return c.replaceAll("\\.", "/") + ".java";
   }

}
