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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jcoderz.commons.ArgumentMalformedException;
import org.jcoderz.commons.util.Assert;
import org.jcoderz.commons.util.FileUtils;
import org.jcoderz.commons.util.IoUtil;
import org.jcoderz.commons.util.LoggingUtils;
import org.jcoderz.commons.util.ObjectUtil;
import org.jcoderz.commons.util.StringUtil;
import org.jcoderz.phoenix.report.jaxb.Item;
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

   /** The Constant CLASSNAME. */
   private static final String CLASSNAME = ReportNormalizer.class.getName();

   /** The Constant logger. */
   private static final Logger logger = Logger.getLogger(CLASSNAME);

   /** The log level. */
   private Level mLogLevel;

   /** The out file. */
   private File mOutFile;

   /** The reports. */
   private final List<File> mReports = new ArrayList<File>();

   /** The filters. */
   private final List<File> mFilters = new ArrayList<File>();

   /** The old Report. */
   private File mOldReport;


   /**
    * Merge input reports.
    * @throws JAXBException if a xml handling error occurs.
    * @throws FileNotFoundException in case of an IO issue.
    */
   public void merge ()
       throws JAXBException, FileNotFoundException
   {
     logger.log(Level.FINE, "Merging jcoderz-report.xml files...");
     // prepare JAXB
     final JAXBContext mJaxbContext
         = JAXBContext.newInstance("org.jcoderz.phoenix.report.jaxb",
           this.getClass().getClassLoader());
     final Marshaller mMarshaller = mJaxbContext.createMarshaller();
     mMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
             Boolean.TRUE);

     // merge the reports
     final Report mergedReport = new ObjectFactory().createReport();
     for (final File reportFile : mReports)
     {
        logger.log(Level.FINE, "Report: " + reportFile);
        try
        {
           final Report report = (Report) new ObjectFactory()
                 .createUnmarshaller().unmarshal(reportFile);
           mergedReport.getFile().addAll(report.getFile());
        }
        catch (JAXBException ex)
        {
           // TODO: ADD ISSUE AS system ITEM TO THE REPORT
           ex.printStackTrace();
        }
     }
     // create the file
     mMarshaller.marshal(mergedReport, new FileOutputStream(mOutFile));
   }


   /**
    * Filters the report XML file using the JDK XSL processor.
    */
   public void filter () throws TransformerException, IOException
   {
       logger.log(Level.FINE, "Filtering jcoderz-report.xml files...");
       for (final File filterFile : mFilters)
       {
           logger.log(Level.FINE, "Filter: " + filterFile);
           final TransformerFactory tFactory
               = TransformerFactory.newInstance();

           final Transformer transformer 
               = tFactory.newTransformer(new StreamSource(filterFile));

           final File tempOutputFile 
               = new File(mOutFile.getCanonicalPath() + ".tmp");
           tempOutputFile.createNewFile();

           final FileOutputStream out = new FileOutputStream(tempOutputFile);
           transformer.transform(new StreamSource(mOutFile),
               new StreamResult(out));
           IoUtil.close(out);
           FileUtils.copyFile(tempOutputFile, mOutFile);
           FileUtils.delete(tempOutputFile);
       }
   }

   /**
    * Searches for new findings based on the old jcReport and increases the
    * severity of such findings to NEW. 
    */
   public void flagNewFindings () 
       throws JAXBException, FileNotFoundException
   {
       logger.log(Level.FINE, "Searching for NEW findings...");
       final Report currentReport 
           = (Report) new ObjectFactory().createUnmarshaller().unmarshal(mOutFile);
       final Report oldReport 
           = (Report) new ObjectFactory().createUnmarshaller().unmarshal(mOldReport);
       for(org.jcoderz.phoenix.report.jaxb.File newFile : 
           (List<org.jcoderz.phoenix.report.jaxb.File>) currentReport.getFile())
       {
           final org.jcoderz.phoenix.report.jaxb.File oldFile 
               = findFile(newFile, oldReport);
           for(Item item:(List<Item>) newFile.getItem())
           {
               if (oldFile == null 
                   || (item.getSeverity().getPenalty() > 0 
                       && findItem(item, oldFile) == null))
               {
                   flagAsNew(item);
               }
           }
       }
       
       final FileOutputStream out = new FileOutputStream(mOutFile);
       try
       {
           new ObjectFactory().createMarshaller().marshal(currentReport, out);
       }
       finally
       {
           IoUtil.close(out);
       }
   }

    // be more smart in finding matching items... (eg if the file was edited)
    private Item findItem (
        Item newItem, org.jcoderz.phoenix.report.jaxb.File oldFile)
    {
        Item result = null;
        for(Item item:(List<Item>) oldFile.getItem())
        {
            if (item.getLine() == newItem.getLine()
                && item.getColumn() == newItem.getColumn()
                && item.getFindingType().equals(newItem.getFindingType())
                && item.getCounter() <= newItem.getCounter())
            {
                result = item;
                break;
            }
        }
        return result;
    }


    private void flagAsNew (Item item)
    {
        final Severity oldSeverity = item.getSeverity();
        item.setSeverity(Severity.NEW);
        final String severityReason = item.getSeverityReason();
        item.setSeverityReason(
            (severityReason == null ? "" : (severityReason + " ")) 
            + "Increased severity from '" + oldSeverity
            + "' for new Finding.");
            
    }


// This could be done faster, might be restructure the data first for 
   // faster lookup.
   private org.jcoderz.phoenix.report.jaxb.File findFile (
       org.jcoderz.phoenix.report.jaxb.File newFile, Report oldReport)
   {
       final String className = newFile.getClassname();
       final String packageName = newFile.getPackage();
       final String fileName = newFile.getName();
       org.jcoderz.phoenix.report.jaxb.File result = null;
       for(org.jcoderz.phoenix.report.jaxb.File file : 
           (List<org.jcoderz.phoenix.report.jaxb.File>) oldReport.getFile())
       {
           if (file.getName().equals(fileName) 
               || (file.getClassname().equals(className) 
                   && file.getPackage().equals(packageName)))
           {
               result = file;
               break;
           }
       }
       return result;
   }


/**
    * Parses the arguments.
    *
    * @param args the args
    */
   private void parseArguments (String[] args)
   {
      try
      {
         for (int i = 0; i < args.length; )
         {
            logger.fine("Parsing argument '" + args[i] + "' = '"
                  + args[i + 1] + "'");

            if ("-jcreport".equals(args[i]))
            {
               addReport(new File(args[i + 1]));
            }
            else if ("-filter".equals(args[i]))
            {
               addFilter(new File(args[i + 1]));
            }
            else if ("-old".equals(args[i]))
            {
               setOldFile(new File(args[i + 1]));
            }
            else if ("-loglevel".equals(args[i]))
            {
                setLogLevel(Level.parse(args[i + 1]));
            }
            else if ("-out".equals(args[i]))
            {
                setOutFile(new File(args[i + 1]));
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
         final IllegalArgumentException ex = new IllegalArgumentException(
            "Missing value for " + args[args.length - 1]);
         ex.initCause(e);
         throw ex;
      }
      catch (IOException e)
      {
         final IllegalArgumentException ex = new IllegalArgumentException(
            "Wrong out folder " + args[args.length - 1]);
         ex.initCause(e);
         throw ex;
      }
   }

   /**
    * The main method.
    *
    * @param args the arguments
    */
   public static void main (String[] args)
       throws Exception
   {
      final ReportMerger rm = new ReportMerger();
      rm.parseArguments(args);
      rm.merge();
      rm.filter();
   }

    /**
     * Adds the report.
     * @param report the report
     */
    public void addReport (File report)
    {
        mReports.add(report);
    }

    /**
     * Adds the filter.
     * @param filter the filter
     */
    public void addFilter (File filter)
    {
        mFilters.add(filter);
    }

    /**
     * Gets the log level.
     *
     * @return the log level
     */
    public Level getLogLevel ()
    {
        return mLogLevel;
    }


    /**
     * Sets the log level.
     *
     * @param logLevel the new log level
     */
    public void setLogLevel (Level logLevel)
    {
        mLogLevel = logLevel;
        LoggingUtils.setGlobalHandlerLogLevel(mLogLevel);
        logger.fine("Setting log level: " + mLogLevel);
        logger.setLevel(mLogLevel);
    }


    /**
     * Gets the out file.
     *
     * @return the out file
     */
    public File getOutFile ()
    {
        return mOutFile;
    }


    /**
     * Set the old report to compare with.
     * @param file old report file.
     */
    public void setOldFile (File file)
        throws IOException
    {
        Assert.notNull(file, "file");
        if (mOldReport != null)
        {
            throw new ArgumentMalformedException("old", file,
                "Old Report File has already set to '" + mOldReport + "'.");
        }
        mOldReport = file.getCanonicalFile();
    }
    
    /**
     * Sets the out file.
     *
     * @param outFile the new out file
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void setOutFile (File outFile)
        throws IOException
    {
        if (mOutFile != null)
        {
            throw new ArgumentMalformedException("outFile", outFile,
                "Out File already set to '" + mOutFile + "'.");
        }
        mOutFile = outFile;
        if (mOutFile.isDirectory())
        {
            mOutFile.mkdirs();
            mOutFile = new File(mOutFile,
                ReportNormalizer.JCODERZ_REPORT_XML).getCanonicalFile();
        }
        else
        {
           mOutFile = mOutFile.getCanonicalFile();
        }

    }
}
