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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.jcoderz.commons.util.FileUtils;
import org.jcoderz.commons.util.IoUtil;

/**
 * Provides merging of findbugs, pmd, checkstyle, cpd, and cobertura
 * XML files into a single XML representation.
 *
 * @author Michael Griffel
 * @author Michael Rumpf
 */
public final class ReportNormalizer
{
   public static final String JCODERZ_REPORT_XML
      = "jcoderz-report.xml";
   private static final transient String CLASSNAME
      = ReportNormalizer.class.getName();
   private static final transient Logger logger
      = Logger.getLogger(CLASSNAME);

   private File mProjectHome;
   private String mProjectName = "Unknown Project";
   private File mOutFile;
   private Level mLogLevel = Level.INFO;
   private ReportLevel mLevel = ReportLevel.PROD;

   /**
    * The XSL stylesheet that can be used to filter
    * the jcoderz-report XML file.
    */
   private File mFilterFile = null;

   /**
    * Constructor.
    * @throws IOException in case of any error.
    */
   public ReportNormalizer ()
         throws IOException
   {
      mProjectHome = new File(".").getCanonicalFile();
      mOutFile = new File(JCODERZ_REPORT_XML);
   }

   /**
    * Main method.
    * @param args arguments.
    */
   public static void main (String[] args)
   {
      int rc = 0;
      try
      {
         final ReportNormalizer rn = new ReportNormalizer();
         rn.run(args);
      }
      catch (Throwable e)
      {
         if (logger.isLoggable(Level.FINE))
         {
            e.printStackTrace();
         }
         rc = -1;
      }
      System.exit(rc);
   }

   /**
    * Run ReportNormalizer.
    * @param args command line arguments.
    * @throws Exception in case of any error.
    */
   public void run (String[] args)
      throws Exception
   {
      final List reportList = parseArguments (args);
      final Map items = new HashMap();

      logger.fine("Running report normalizer on #" + reportList.size()
            + " reports ...");
      for (final Iterator iterator = reportList.iterator(); iterator.hasNext();)
      {
         try
         {
            final SourceReport report = (SourceReport) iterator.next();
            final ReportReader reportReader
               = ReportReaderFactory.createReader(report.getReportFormat());
            logger.fine("Processing report " + report.getReportFormat()
                  + " '" + report.getFilename() + "'");
            if (report.getFilename().length() != 0 
                    || report.getFilename().isDirectory())
            {
               reportReader.parse(report.getFilename());
               reportReader.merge(items);
            }
            else
            {
               logger.fine("Good job, no findings reported by "
                     + report.getReportFormat());
            }
         }
         catch (Exception e)
         {
            logger.log(Level.SEVERE, "Error while processing", e);
         }
      }

      final JcoderzReport myReport = new JcoderzReport();

      myReport.setProjectHome(mProjectHome.getAbsolutePath());
      myReport.setProjectName(mProjectName);
      myReport.setLevel(mLevel);

      // XML report
      final OutputStream out = new FileOutputStream(mOutFile);
      myReport.write(out, items);
      IoUtil.close(out);

      // apply filters to the report
      if (mFilterFile != null)
      {
         filter();
      }
   }

   /**
    * Filters the report XML file using the JDK XSL processor.
    *
    * @throws TransformerFactoryConfigurationError
    * @throws TransformerConfigurationException
    * @throws IOException
    * @throws TransformerException
    * @throws FileNotFoundException
    */
   private void filter ()
         throws TransformerFactoryConfigurationError,
         TransformerConfigurationException, IOException,
         TransformerException, FileNotFoundException
   {
      logger.log(Level.FINE, "Filter: " + mFilterFile);
      final TransformerFactory tFactory = TransformerFactory.newInstance();

      final Transformer transformer = tFactory.newTransformer(
            new StreamSource(mFilterFile));

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

   /**
    * The following parameters select the different reports
    * to combine into a single report.
    *
    * <ul>
    *   <li><code>-jcoverage jvoveragereport.xml</code> (http://???)</li>
    *   <li><code>-cobertura coberturareport.xml</code> (http://???)</li>
    *   <li><code>-checkstyle checkstylereport.xml</code> 
    *       (http://checkstyle.sf.net)</li>
    *   <li><code>-findbugs findbugsreport.xml</code> 
    *       (http://findbugs.sf.net)</li>
    *   <li><code>-pmd pmdreport.xml</code> (http://pmd.sf.net)</li>
    *   <li><code>-cpd cpdreport.xml</code> (http://))))</li>
    * </ul>
    *
    * <ul>
    *   <li><code>-projectHome</code></li>
    *   <li><code>-filter filter.xsl</code></li>
    *   <li><code>-srcDir</code></li>
    *   <li><code>-projectName</code></li>
    *   <li><code>-level PROD|TEST|MISC</code> The weight level</li>
    *   <li><code>-loglevel</code></li>
    *   <li><code>-out</code></li>
    * </ul>
    *
    * @param args The command line arguments
    * @return The list of reports to normalize
    * @throws IOException When the filter file cannot be found
    */
   private List parseArguments (String[] args)
         throws IOException
   {
      try
      {
         final List reportList = new ArrayList();

         for (int i = 0; i < args.length; )
         {
            logger.fine("Parsing argument '" + args[i] + "' = '"
                  + args[i + 1] + "'");

            if (args[i].equals("-jcoverage"))
            {
               reportList.add(new SourceReport(
                     ReportFormat.JCOVERAGE, args[i + 1]));
            }
            else if (args[i].equals("-cobertura"))
            {
               reportList.add(new SourceReport(
                     ReportFormat.COBERTURA, args[i + 1]));
            }
            else if (args[i].equals("-checkstyle"))
            {
               reportList.add(new SourceReport(
                     ReportFormat.CHECKSTYLE, args[i + 1]));
            }
            else if (args[i].equals("-findbugs"))
            {
               reportList.add(new SourceReport(
                     ReportFormat.FINDBUGS, args[i + 1]));
            }
            else if (args[i].equals("-pmd"))
            {
               reportList.add(new SourceReport(
                     ReportFormat.PMD, args[i + 1]));
            }
            else if (args[i].equals("-cpd"))
            {
               reportList.add(new SourceReport(
                     ReportFormat.CPD, args[i + 1]));
            }
            else if (args[i].equals("-projectHome"))
            {
               mProjectHome = new File(args[i + 1]);
            }
            else if (args[i].equals("-filter"))
            {
               mFilterFile = new File(args[i + 1]);
               if (!mFilterFile.exists())
               {
                  throw new IOException("Filter file '" + mFilterFile
                        + "' does not exists.");
               }
            }
            else if (args[i].equals("-srcDir"))
            {
               reportList.add(new SourceReport(
                     ReportFormat.SOURCE_DIRECTORY, args[i + 1]));
            }
            else if (args[i].equals("-projectName"))
            {
               mProjectName = args[i + 1];
            }
            else if (args[i].equals("-level"))
            {
               mLevel = ReportLevel.fromString(args[i + 1]);
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
                  mOutFile
                     = new File(out, JCODERZ_REPORT_XML).getCanonicalFile();
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
         return reportList;
      }
      catch (IndexOutOfBoundsException e)
      {
         final IllegalArgumentException ex = new IllegalArgumentException(
            "Missing value for " + args[args.length - 1]);
         ex.initCause(e);
         throw ex;
      }
   }

   private static final class SourceReport
   {
      private final ReportFormat mReportFormat;
      private final File mFilename;

      SourceReport (ReportFormat r, String f)
      {
         mReportFormat = r;
         mFilename = new File(f);
         if (! mFilename.exists())
         {
            throw new IllegalArgumentException(
                  "Input file/directory '" + f + "' does not exists.");
         }
      }

      /**
       * Returns the filename.
       * @return the filename.
       */
      File getFilename ()
      {
         return mFilename;
      }

      /**
       * Returns the reportFormat.
       * @return the reportFormat.
       */
      ReportFormat getReportFormat ()
      {
         return mReportFormat;
      }
   }
}
