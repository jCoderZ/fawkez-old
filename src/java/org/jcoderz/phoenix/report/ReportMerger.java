/*
 * $Id: ReportNormalizer.java 413 2006-10-07 19:22:43Z mrumpf $
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jcoderz.commons.util.FileUtils;
import org.jcoderz.commons.util.IoUtil;
import org.jcoderz.phoenix.report.jaxb.ObjectFactory;
import org.jcoderz.phoenix.report.jaxb.Report;

/**
 * Provides merging and filtering of various jcoderz-report.xml files.
 * It combines parts of the functions from ReportNormalizer and XmlMergeAntTask.
 *
 * @author Michael Rumpf
 */
public class ReportMerger
{
   private static final transient String CLASSNAME
      = ReportNormalizer.class.getName();
   private static final transient Logger logger
      = Logger.getLogger(CLASSNAME);
   
   private Level mLogLevel;
   private File mOutFile;
   private List mReports = new ArrayList();
   private List mFilters = new ArrayList();

   
   public void merge ()
   {
      // create the final report
      try
      {
         logger.log(Level.FINE, "Merging jcoderz-report.xml files...");
         // prepare JAXB
         final JAXBContext mJaxbContext = JAXBContext.newInstance(
               "org.jcoderz.phoenix.report.jaxb",
               this.getClass().getClassLoader());
         final Marshaller mMarshaller = mJaxbContext.createMarshaller();
         mMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                 Boolean.TRUE);

         // merge the reports
         final Report mergedReport = new ObjectFactory().createReport();
         final Iterator iter = mReports.iterator();
         while (iter.hasNext())
         {
            final File reportFile = (File) iter.next();
            logger.log(Level.FINE, "Report: " + reportFile);
            try
            {
               final Report report = (Report) new ObjectFactory()
                     .createUnmarshaller().unmarshal(reportFile);
               mergedReport.getFile().addAll(report.getFile());
            }
            catch (JAXBException ex)
            {
               // TODO
               ex.printStackTrace();
            }
         }

         // create the file
         mMarshaller.marshal(mergedReport, new FileOutputStream(mOutFile));
      }
      catch (Exception ex)
      {
         // TODO
         ex.printStackTrace();
      }
   }


   /**
    * Filters the report XML file using the JDK XSL processor.
    */
   private void filter ()
   {
      try
      {
         logger.log(Level.FINE, "Filtering jcoderz-report.xml files...");
         final Iterator iter = mFilters.iterator();
         while (iter.hasNext())
         {
            final File filterFile = (File) iter.next();
            logger.log(Level.FINE, "Filter: " + filterFile);
            final TransformerFactory tFactory 
                = TransformerFactory.newInstance();
      
            final Transformer transformer = tFactory.newTransformer(
                  new StreamSource(filterFile));
      
            final File tempOutputFile = new File(
                  mOutFile.getCanonicalPath() + ".tmp");
            tempOutputFile.createNewFile();
      
            final FileOutputStream out = new FileOutputStream(tempOutputFile);
            transformer.transform(new StreamSource(mOutFile),
                 new StreamResult(out));
            IoUtil.close(out);
            FileUtils.copyFile(tempOutputFile, mOutFile); 
            FileUtils.delete(tempOutputFile);
         }
      }
      catch (Exception ex)
      {
         // TODO
         ex.printStackTrace();
      }
   }


   private void parseArguments (String[] args)
   {
      try
      {
         for (int i = 0; i < args.length; )
         {
            logger.fine("Parsing argument '" + args[i] + "' = '"
                  + args[i + 1] + "'");
      
            if (args[i].equals("-jcreport"))
            {
               mReports.add(new File(args[i + 1]));
            }
            else if (args[i].equals("-filter"))
            {
               mFilters.add(new File(args[i + 1]));
            }
            else if (args[i].equals("-loglevel"))
            {
               mLogLevel = Level.parse(args[i + 1]);
               final Handler[] handlers = Logger.getLogger("").getHandlers();
               for (int index = 0; index < handlers.length; index++)
               {
                  handlers[index].setLevel(mLogLevel);
               }
               logger.fine("Setting log level: " + mLogLevel);
               logger.setLevel(mLogLevel);
            }
            else if (args[i].equals("-out"))
            {
               final File out = new File(args[i + 1]);
               if (out.isDirectory())
               {
                  out.mkdirs();
                  mOutFile = new File(out,
                     ReportNormalizer.JCODERZ_REPORT_XML).getCanonicalFile();
               }
               else
               {
                  mOutFile = out.getCanonicalFile();
               }
            }
            else
            {
               throw new IllegalArgumentException(
                       "Invalid argument '" + args[i]  + "'");
            }
      
            ++i;
            ++i;
         }
      }
      catch (IndexOutOfBoundsException e)
      {
         throw new IllegalArgumentException("Missing value for "
            + args[args.length - 1]);
      }
      catch (IOException e)
      {
         throw new IllegalArgumentException("Wrong out folder "
            + args[args.length - 1]);
      }
   }

   
   public static void main (String[] args)
   { 
      final ReportMerger rm = new ReportMerger();
      rm.parseArguments(args);
      rm.merge();
      rm.filter();
   }
}
