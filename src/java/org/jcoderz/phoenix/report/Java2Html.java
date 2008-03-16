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
/*
 * Java2HTML v0.1 alpha converts a java source code into HTML with
 * syntax highlighting for keywords, comments, strings and chars
 *
 * The contents of this file are subject to the
 * Mozilla Public License Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights
 * and limitations under the License.
 *
 * The Original Code is Java2HTML Converter v0.1 alpha.
 * The Initial Developer of the Original Code is Borislav Manolov.
 * Portions created by Borislav Manolov are Copyright (C) 2003,
 * Borislav Manolov. All Rights Reserved.
 *
 * Submit bugs and comments at manfear@web.de
 * 03/03/03
 */
package org.jcoderz.phoenix.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.jcoderz.commons.util.Assert;
import org.jcoderz.commons.util.Constants;
import org.jcoderz.commons.util.EmptyIterator;
import org.jcoderz.commons.util.FileUtils;
import org.jcoderz.commons.util.IoUtil;
import org.jcoderz.commons.util.LoggingUtils;
import org.jcoderz.commons.util.XmlUtil;
import org.jcoderz.phoenix.report.jaxb.Item;
import org.jcoderz.phoenix.report.jaxb.Report;

/**
 * TODO: Fix CVS links
 * TODO: Refactor!
 * TODO: Link to current build
 * TODO: Link to CC home
 * TODO: Add @media printer???
 * TODO: Add doku
 * TODO: Codestyle
 * TODO: Generate error group view
 *
 * @author Andreas Mandel
 */
public final class Java2Html
{
   /** property name for the wiki url prefix. */
   public static final String WIKI_BASE_PROPERTY = "report.wiki-prefix";

   /** Pattern helper to generate css style names of the listings. */
   private static final String[] PATTERN = {"odd", "even"};

   /** Size of the pattern. */
   private static final int PATTERN_SIZE = PATTERN.length;

   /** Name of this class. */
   private static final String CLASSNAME = Java2Html.class.getName();

   /** The logger used for technical logging inside this class. */
   private static final Logger logger = Logger.getLogger(CLASSNAME);

   /** String used as line separator in the output html. */
   private static final String NEWLINE = "\n";

   /** Name of the index page with content sorted by package name. */
   private static final String SORT_BY_PACKAGE_INDEX = "index.html";
   /** Name of the index page with content sorted by quality. */
   private static final String SORT_BY_QUALITY_INDEX = "index_q.html";
   /** Name of the index page with content sorted by coverage. */
   private static final String SORT_BY_COVERAGE_INDEX = "index_c.html";

   /** There is no such thing before JDK1.5. */
   private static final Integer INTEGER_ZERO = new Integer(0);

   /** Marker for ccs stypes used as the last row in a table. */
   private static final String LAST_MARKER = "_last";

   private static final String DEFAULT_STYLESHEET = "reportstyle.css";

   /** Collects a List of all <code>FileSummary</code>s of the report. */
   private final List mAllFiles = new ArrayList();


   /** Map of package name + FileSummary for this package. */
   private final Map mPackageSummary = new HashMap();
   private final Map mAllPackages = new HashMap();
   /**
    * Collects findings in the current file.
    * Maps from the line number (Integer) to a List of Item objects.
    */
   private final Map mFindingsInFile = new HashMap();
   private final Map mFindingsInCurrentLine = new HashMap();
   private final List mCurrentFindings = new ArrayList();
   private final List mHandledFindings = new ArrayList();
   /** List of findings with no (available) file assignment */
   private final List mGlobalFindings = new ArrayList();
   /** file summary for all files */
   private FileSummary mGlobalSummary;


   /** HtmlView object used to render input source into html code. */
   private final HtmlView mHtmlView = new HtmlView();
   private String mProjectName = "";
   private String mCvsBase = null;
   private String mProjectHome;
   private String mTimestamp = null;
   private String mStyle = DEFAULT_STYLESHEET; // the CSS stuff to use
   private String mClassname;
   private String mPackage;
   private String mPackageBase;
   private final StringBuffer mStringBuffer = new StringBuffer();
   /** String buffer to be used by the getIcons method. */
   private final StringBuffer mGetIconsStringBuffer = new StringBuffer();

   private java.io.File mInputData;
   private java.io.File mOutDir;

   private boolean mCoverageData = true;

   private Level mLogLevel = Level.INFO;

   /**
    * Holds a list of items that are currently active for the
    * current file and line.
    */
   private final List mActiveItems = new LinkedList();

   /**
    * Constructor.
    *
    * @throws IOException In case the current working directory cannot be
    *       determined.
    */
   public Java2Html ()
         throws IOException
   {
      mProjectHome = new java.io.File(".").getCanonicalPath();
   }

   /**
    * Main entry point.
    *
    * @param args The command line arguments.
    * @throws IOException an io exception occurs.
    * @throws JAXBException if the xml can not be parsed.
    */
   public static void main (String[] args)
         throws IOException, JAXBException
   {
      final Java2Html engine = new Java2Html();

      engine.parseArguments(args);
      // Turn on logging
      Logger.getLogger("org.jcoderz.phoenix.report").setLevel(Level.FINEST);
      engine.process();
   }

   /**
    * Returns the string "odd" or "even", depending on the number given.
    * @param number the number to check if it'S odd or even.
    * @return the string "odd" if the given number is odd, "even"
    *     otherwise.
    */
   public static String toOddEvenString (int number)
   {
       return PATTERN[number % PATTERN_SIZE];
   }

   /**
    * Set the name of the package that should be treated as project "root"
    * package.
    * @param packageBase the project base package name.
    */
   public void setPackageBase (String packageBase)
   {
      mPackageBase = packageBase;
      logger.config("Package base set to '" + mPackageBase + "'.");
   }

   /**
    * Set the timestamp when the report has been initiated.
    *
    * @param timestamp the report creation timestamp.
    */
   public void setTimestamp (String timestamp)
   {
      mTimestamp = timestamp;
      logger.config("Timestamp set to '" + mTimestamp + "'.");
   }

   /**
    * Sets the flag if coverage data is available and should be taken
    * into account.
    * @param coverageDataAvailable true, if coverage data is available and
    * should be taken into account.
    */
   public void setCoverageData (boolean coverageDataAvailable)
   {
      mCoverageData = coverageDataAvailable;
      logger.config("Coverage data set to '" + mCoverageData + "'.");
   }

   /**
    * Sets the css file to be used can be relative to report path or
    * absolute.
    * @param style the css file to be used can be relative to report path or
    * absolute.
    */
   public void setStyle (String style)
   {
      mStyle = style;
      logger.config("Style set to '" + mStyle + "'.");
   }

   /**
    * Base url to the wiki to use.
    * Finding pages link to a wiki page combined of this url and the
    * finding type.
    * @param wikiBase url to the wiki to use.
    */
   public void setWikiBase (String wikiBase)
   {
      logger.config("Wiki base set to '" + wikiBase + "'.");
      System.getProperties().setProperty(WIKI_BASE_PROPERTY,
            wikiBase);
   }

   /**
    * Base path (url) to the cvs repository used to create links to
    * the cvs.
    * @param cvsBase url that points to a web cvs of the project.
    */
   public void setCvsBase (String cvsBase)
   {
      mCvsBase = cvsBase;
      logger.config("CVS base set to '" + mCvsBase + "'.");
   }

   /**
    * Returns the name of the project.
    * @return the name of the project.
    */
   public String getProjectName ()
   {
      return mProjectName;
   }

   /**
    * Sets the name of the project used as readable string at several
    * places in the report.
    * @param name the name of the project used as readable string at several
    *    places in the report.
    */
   public void setProjectName (String name)
   {
      logger.config("Project name set to '" + getProjectName() + "'.");
      Assert.notNull(name, "name");
      mProjectName = name;
   }

   /**
    * The base path where the source files can be found.
    * @param file base path where the source files can be found.
    * @throws IOException if access to the path fails.
    */
   public void setProjectHome (java.io.File file) throws IOException
   {
     final java.io.File projectHomeFile = file.getCanonicalFile();
     mProjectHome = projectHomeFile.getCanonicalPath();
     if (!projectHomeFile.isDirectory())
     {
        throw new RuntimeException("'projectHome' must be a directory '"
              + projectHomeFile + "'.");
     }
     logger.config("Using project home " + mProjectHome + ".");
   }

   /**
    * The input file containing the jcoderz report.
    * @param file input file containing the jcoderz report.
    * @throws IOException if access to the file fails.
    */
   public void setInputFile (java.io.File file) throws IOException
   {
     mInputData = file.getCanonicalFile();
     if (!mInputData.canRead())
     {
        throw new RuntimeException("Can not read report file '"
              + mInputData + "'.");
     }
     logger.config("Using report file " + mInputData + ".");
   }

   /**
    * The output directory where the report should be written to.
    * If the directory does not exist it is created.
    * @param dir the output directory where the report should be written to.
    * @throws IOException if access or creation of the directory fails.
    */
   public void setOutDir (java.io.File dir) throws IOException
   {
     mOutDir = dir.getCanonicalFile();
     if (!mOutDir.exists())
     {
        if (!mOutDir.mkdir())
        {
           throw new RuntimeException("Could not create 'outDir' '"
                 + mOutDir + "'.");
        }
     }
     if (!mOutDir.isDirectory())
     {
        throw new RuntimeException("'outDir' must be a directory '"
              + mOutDir + "'.");
     }
     logger.config("Using out dir " + mOutDir + ".");
   }

   /**
    * Starts the actual generation process.
    * @throws JAXBException if the xmp parsing fails.
    * @throws IOException if a IO problem occurs.
    */
   public void process ()
         throws JAXBException, IOException
   {
      final JAXBContext jaxbContext
            = JAXBContext.newInstance("org.jcoderz.phoenix.report.jaxb",
                  this.getClass().getClassLoader());
      final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      unmarshaller.setValidating(true);
      final Report report = (Report) unmarshaller.unmarshal(mInputData);
      mGlobalSummary = new FileSummary();

      final Iterator files = report.getFile().iterator();

      while (files.hasNext())
      {  // TODO: Handle none java / source files
         final org.jcoderz.phoenix.report.jaxb.File file
               = (org.jcoderz.phoenix.report.jaxb.File) files.next();

         try
         {
            if (file.getName().endsWith(".java"))
            {
               // TODO parentDir = project-home (from jcoderz report xml)
               java2html(new java.io.File(file.getName()), file);
            }
            else
            {
               mGlobalFindings.add(file);
            }
         }
         catch (Exception ex)
         {
            logger.log(Level.SEVERE,
                  "Failed to generate report for '" + file.getName() + "'.",
                  ex);
            mGlobalFindings.add(file);
         }
      }

      // create package summary
      final Iterator packages = mAllPackages.values().iterator();
      while (packages.hasNext())
      {
         final List pkg = (List) packages.next();
         createPackageSummary(pkg);
      }
      createFullSummary();

      createFindingsSummary();
      createPerFindingSummary();

      logger.fine("Charts.");

      final StatisticCollector sc;
      if (mPackageBase == null)
      {
        sc = new StatisticCollector(report, mOutDir, mTimestamp);
      }
      else
      {
        sc = new StatisticCollector(report, mPackageBase, mOutDir, mTimestamp);
      }
      sc.createCharts();

      // copy the stylesheet and the icons
      copyStylesheet();
      copyIcons();

      logger.fine("Done.");
   }

   private void copyIcons ()
   {
      // create images sub-folder
      final File outDir = new File(mOutDir, "images");
      outDir.mkdirs();

      for (int i = 0; i < Severity.VALUES.size(); i++)
      {
          final Severity s = Severity.fromInt(i);
          if (s.equals(Severity.OK) || s.equals(Severity.COVERAGE))
          {
              continue;
          }
          final String name = "icon_" + s.toString() + ".gif";
          final InputStream in
                  = this.getClass().getResourceAsStream(name);
          if (in != null)
          {
              copyResource(in, name, outDir);
          }
          else
          {
              logger.warning("Could not find resource '" + name + "'!");
          }
      }
   }

   private void copyResource (InputStream in, String resource, File outDir)
   {
      // Copy it to the output folder
      OutputStream out = null;
      try
      {
         out = new FileOutputStream(new File(outDir, resource));
         FileUtils.copy(in, out);
      }
      catch (FileNotFoundException ex)
      {
         throw new RuntimeException("Can not find output folder '"
            + mOutDir + "'.", ex);
      }
      catch (IOException ex)
      {
         throw new RuntimeException("Could not copy resource '"
            + resource + "'.", ex);
      }
      finally
      {
         IoUtil.close(in);
         IoUtil.close(out);
      }
   }

   private void copyStylesheet ()
   {
      // 1. Try to read the stylesheet from the jar (default stylesheet)
      // 2. Try to open it from a user-defined location
      // 3. Use the default one if the user-defined is not found
      InputStream in = this.getClass().getResourceAsStream(mStyle);
      if (in == null)
      {
         try
         {
            final File style = new File(mStyle);
            in = new FileInputStream(style);
         }
         catch (FileNotFoundException ex)
         {
            in = this.getClass().getResourceAsStream(DEFAULT_STYLESHEET);
            if (in == null)
            {
               throw new RuntimeException("Can not find stylesheet file '"
                     + mStyle + "'.", ex);
            }
         }
      }

      copyResource(in, DEFAULT_STYLESHEET, mOutDir);
   }

   private void parseArguments (String[] args)
   {
      try
      {
         for (int i = 0; i < args.length; )
         {
            if ("-outDir".equals(args[i]))
            {
               setOutDir(new java.io.File(args[i + 1]));
            }
            else if ("-report".equals(args[i]))
            {
               setInputFile(new java.io.File(args[i + 1]));
            }
            else if ("-projectHome".equals(args[i]))
            {
               setProjectHome(new java.io.File(args[i + 1]));
            }
            else if ("-projectName".equals(args[i]))
            {
               setProjectName(args[i + 1]);
            }
            else if ("-cvsBase".equals(args[i]))
            {
               setCvsBase(args[i + 1]);
            }
            else if ("-timestamp".equals(args[i]))
            {
               setTimestamp(args[i + 1]);
            }
            else if ("-wikiBase".equals(args[i]))
            {
               setWikiBase(args[i + 1]);
            }
            else if ("-reportStyle".equals(args[i]))
            {
               setStyle(args[i + 1]);
            }
            else if ("-noCoverage".equals(args[i]))
            {
               setCoverageData(false);
               i -= 1;
            }
            else if ("-packageBase".equals(args[i]))
            {
              setPackageBase(args[i + 1]);
            }
            else if ("-loglevel".equals(args[i]))
            {
               setLoglevel(args[i + 1]);
            }
            else
            {
               throw new IllegalArgumentException(
                       "Invalid argument '" + args[i] + "'");
            }
            i += 1 /* command */ + 1 /* argument */;
         }
      }
      catch (IndexOutOfBoundsException e)
      {
         final IllegalArgumentException ex
               = new IllegalArgumentException("Missing value for "
                  + args[args.length - 1]);
         ex.initCause(e);
         throw ex;
      }
      catch (Exception e)
      {
         final IllegalArgumentException ex = new IllegalArgumentException(
               "Problem with arument value for " + args[args.length - 1]);
         ex.initCause(e);
         throw ex;
      }
   }

   private void setLoglevel (String loglevel)
   {
       mLogLevel = Level.parse(loglevel);
       LoggingUtils.setGlobalHandlerLogLevel(Level.ALL);
       logger.fine("Setting log level: " + mLogLevel);
       logger.setLevel(mLogLevel);
   }

   /**
    *
    */
   private void createPerFindingSummary () throws IOException
   {
      final Iterator i = FindingsSummary.getFindingsSummary().getFindings().
         values().iterator();

      while (i.hasNext())
      {
         final FindingsSummary.FindingSummary summary
              = (FindingsSummary.FindingSummary) i.next();
         final String filename = summary.createFindingDetailFilename();
         final BufferedWriter out = openWriter(filename);
         try
         {
             htmlHeader(out, "Finding-" + summary.getFindingType().getSymbol()
                      + "-report " + mProjectName, "");
             summary.createFindingTypeContent(out);
             out.write("</body></html>");
         }
         finally
         {
             IoUtil.close(out);
         }
      }
   }

   private void createFindingsSummary () throws IOException
   {
      final BufferedWriter out = openWriter("findings.html");
      try
      {
          htmlHeader(out, "Finding report " + mProjectName, "");
          FindingsSummary.createOverallContent(out);
          out.write("</body></html>");
      }
      finally
      {
          IoUtil.close(out);
      }
   }

   /**
    * converts a java source to HTML
    * with syntax highlighting for the
    * comments, keywords, strings and chars
    */
   private void java2html (java.io.File inFile,
         org.jcoderz.phoenix.report.jaxb.File data)
   {

      mCurrentFindings.clear();
      mHandledFindings.clear();
      mFindingsInFile.clear();
      mActiveItems.clear();
      mCurrentFindings.addAll(data.getItem());
      fillFindingsInFile(data);
      logger.finest("Processing file " + inFile);

      BufferedWriter bw = null;
      String file = null;
      try
      {
         mHtmlView.reset(inFile);
         mPackage = mHtmlView.getPackage();
         mClassname = mHtmlView.getClassname();

         final String subdir = mPackage.replaceAll("\\.", "/");
         final java.io.File dir = new java.io.File(mOutDir, subdir);
         dir.mkdirs();

         bw = openWriter(dir, mClassname + ".html");

         final FileSummary summary
               = createFileSummary(mHtmlView.getNumberOfLines(), subdir);
         addSummary(summary);

         file = mPackage + "." + mClassname;

         htmlHeader(bw, mClassname, mPackage);

         bw.write("<h1><a href='");
         bw.write(relativeRoot(mPackage));
         bw.write("'>Project Report: ");
         bw.write(mProjectName);
         bw.write("</a></h1>" + NEWLINE);
         bw.write("<h2><a href ='index.html'>Packagesummary ");
         bw.write(mPackage);
         bw.write("</a></h2>" + NEWLINE);

         final String cvsLink = getCvsLink(inFile.getAbsolutePath());
         if (cvsLink != null)
         {
            bw.write("<h3><a href='" + cvsLink
               + "' class='cvs' title='cvs version'>" + file
               + "</a></h3>" + NEWLINE);
         }
         else
         {
            bw.write("<h3>" + file + "</h3>" + NEWLINE);
         }

         // create header!!!!
         bw.write("<table border='0' cellpadding='2' cellspacing='0' "
                  + "width='95%'>");
         bw.write("<thead><tr><th>Line</th><th>Hits</th><th>Note</th>"
                  + "<th class='remainder'>Source</th></tr></thead>");
         bw.write("<tbody>");

         final int lastLine = mHtmlView.getNumberOfLines();
         // PASS 2
         String line;
         for (int currentLine = 1; currentLine <= lastLine; currentLine++)
         {
            line = mHtmlView.getLine(currentLine);
            bw.write("<tr class='"
               + errorLevel(currentLine)
               + Java2Html.toOddEvenString(currentLine) + "'>");
            bw.write("<td align='right' class='lineno");
            final boolean isLast = currentLine == lastLine;
            appendIf(bw, currentLine == lastLine, LAST_MARKER);
            bw.write("'><a name='LINE" + currentLine + "' />");
            bw.write(String.valueOf(currentLine));
            bw.write("</td>");
            hitsCell(bw, String.valueOf(getHits(currentLine)), isLast);
            bw.write("<td class='note");
            appendIf(bw, isLast, LAST_MARKER);
            bw.write("'>");
            bw.write(getIcons(currentLine));
            bw.write("</td><td class='code");
            appendIf(bw, isLast, LAST_MARKER);
            bw.write("'>");
            bw.write(replaceLeadingSpaces(addLineItems(line)));
            bw.write("</td></tr>\n");
         }
         bw.write("</tbody>");
         bw.write("</table>\n");


         // findings table
         bw.write("<h2 class='findings-header'>Findings in this File</h2>");
         bw.write("<table width='95%' cellpadding='0' cellspacing='0' "
               + "border='0'>\n");
         int rowCounter = 0;

         final String relativeRoot = relativeRoot(mPackage, "");

         // findings with no line number or uncovered jet
         final Iterator remainder = mFindingsInFile.values().iterator();
         int pos = mHandledFindings.size();
         while (remainder.hasNext())
         {
            final List lineFindings = (List) remainder.next();
            final Iterator items = lineFindings.iterator();
            while (items.hasNext())
            {
               final Item item = (Item) items.next();
               if (!item.getOrigin().equals(Origin.COVERAGE))
               {
                  pos++;
                  rowCounter++;

                  bw.write("<tr class='findings-");
                  bw.write(Java2Html.toOddEvenString(rowCounter));
                  bw.write("row'>\n");
                  bw.write("   <td class='findings-image'>\n");
                  appendSeverityImage(bw, item, relativeRoot);
                  bw.write("   </td>\n");
                  bw.write("   <td class='findings-id'>\n");
                  bw.write("      <a name='FINDING" + pos + "' />\n");
                  bw.write("         (" + pos + ")\n");
                  bw.write("   </td>\n");
                  bw.write("   <td></td><td></td><td></td>\n"); // linenumber
                  bw.write("   <td width='100%' class='findings-data'>\n");
                  bw.write(XmlUtil.escape(item.getMessage()));
                  if (item.getSeverityReason() != null)
                  {
                     bw.write(XmlUtil.escape(item.getSeverityReason()));
                  }
                  bw.write("   </td>\n");
                  bw.write("</tr>\n");
               }
            }
         }

         // Findings as marked in the code.
         final Iterator findings = mHandledFindings.iterator();
         pos = 0;


         while (findings.hasNext())
         {
            final Item item = (Item) findings.next();
            pos++;
            rowCounter++;

            final String link = "#LINE" + item.getLine();

            bw.write("<tr class='findings-");
            bw.write(Java2Html.toOddEvenString(rowCounter));
            bw.write("row'>\n");
            bw.write("   <td class='findings-image'>\n");
            appendSeverityImage(bw, item, relativeRoot);
            bw.write("   </td>\n");
            bw.write("   <td class='findings-id'>\n");
            bw.write("      <a name='FINDING" + pos + "' />\n");
            bw.write("      <a href='" + link + "' title='" + item.getOrigin()
                  + "' >\n");
            bw.write("            (" + pos + ")\n");
            bw.write("      </a>\n");
            bw.write("   </td>\n");
            bw.write("   <td class='findings-line-number' align='right'>\n");
            bw.write("      <a href='" + link + "' >\n");
            bw.write(String.valueOf(item.getLine()));
            bw.write("      </a>\n");
            bw.write("   </td>\n");
            bw.write("   <td class='findings-line-number' align='center'>\n");
            bw.write("      <a href='" + link + "' >:</a>\n");
            bw.write("   </td>\n");
            bw.write("   <td class='findings-line-number' align='left'>\n");
            bw.write("      <a href='" + link + "' >\n");
            bw.write(String.valueOf(item.getColumn()));
            bw.write("      </a>\n");
            bw.write("   </td>\n");
            bw.write("   <td width='100%' class='findings-data'>\n");
            bw.write("      <a href='" + link + "' >\n");
            bw.write(XmlUtil.escape(item.getMessage()));
            bw.write("\n");
            bw.write("      </a>\n");
            bw.write("   </td>\n");
            bw.write("</tr>\n");

         }

         bw.write("</table>\n");

         bw.write("\n</body>\n</html>");
      }
      catch (FileNotFoundException fnfe)
      {
         logger.log(Level.WARNING, "Source file '" + file + "' not found.",
               fnfe);
      }
      catch (IOException ioe)
      {
         logger.log(Level.WARNING, "Problem with '" + file + "'.", ioe);
      }
      finally
      {
         IoUtil.close(bw);
      }
   }

   /**
    * Generates a image related to the severity of the given Item.
    * The image links back to the general finding page of the item.
    * @param w the writer where to write the output to.
    * @param item the item to be documented.
    * @param root the relative path from the page generated to the root dir.
    * @throws IOException if the datas could not be written to the given
    *     writer.
    */
   private void appendSeverityImage (Writer w, Item item, String root)
       throws IOException
   {
       w.write("<a href='");
       w.write(root);
       w.write(FindingsSummary.getFindingsSummary()
               .getFindingSummary(item).createFindingDetailFilename());
       w.write("'><img border='0' title='");
       w.write(String.valueOf(item.getSeverity()));
       w.write(" [");
       w.write(String.valueOf(item.getOrigin()));
       w.write("]' alt='");
       w.write(item.getSeverity().toString().substring(0, 1));
       w.write("' src='");
       w.write(root);
       w.write(getImage(item.getSeverity()));
       w.write("' /></a>\n");
   }

   private String getImage (Severity severity)
   {
      return "images/icon_" + severity.toString() + ".gif";
   }

   private FileSummary createFileSummary (final int linesCount,
                                          final String subdir)
   {
      // Create file summary info
      final FileSummary summary = new FileSummary(mClassname, mPackage,
            subdir + "/" + mClassname + ".html", linesCount, mCoverageData);
      final Iterator items = mCurrentFindings.iterator();
      while (items.hasNext())
      {
         final Item item = (Item) items.next();
         FindingsSummary.addFinding(item, summary);
         if (item.getOrigin().equals(Origin.COVERAGE))
         {
            if (item.getCounter() != 0)
            {
               summary.addCoveredLine();
            }
            else
            {
               summary.addViolation(Severity.COVERAGE);
            }
         }
         else
         {
            summary.addViolation(item.getSeverity());
         }
      }
      return summary;
   }

   private void fillFindingsInFile (
         org.jcoderz.phoenix.report.jaxb.File data)
   {
      final Iterator i = data.getItem().iterator();
      while (i.hasNext())
      {
         final Item item = (Item) i.next();
         final Integer lineNumber = new Integer(item.getLine());
         List itemsInLine = (List) mFindingsInFile.get(lineNumber);
         if (itemsInLine == null)
         {
            itemsInLine = new ArrayList();
            mFindingsInFile.put(lineNumber, itemsInLine);
         }
         itemsInLine.add(item);
      }
   }

   /**
    * Generates the stylesheet link for html output.
    * @param packageName the package of the current generated file. Used
    *                    to generate a relative link.
    * @param style the style to use (relative to report path or absolute)
    * @return the style link as to be placed in the head of the generated
    *         html file.
    */
   private static String createStyle (String packageName, String style)
   {
      // TODO: use the default stylesheet if not explicitly specified
      String result = "";
      if (style != null)
      {
         final String styleLink;
         if (style.indexOf("//") != -1)
         {  // absolute style
            styleLink = style;
         }
         else
         {
            styleLink = relativeRoot(packageName, style);
         }
         result = "<link rel='stylesheet' type='text/css' href='"
            + styleLink + "' />";
      }
      return result;
   }

   private Severity errorLevel (int line)
   {
      Severity severity = Severity.OK;

      final Iterator active = mActiveItems.iterator();

      while (active.hasNext())
      {
         final Item item = (Item) active.next();
         if (item.getEndLine() < line)
         {
            active.remove();
         }
         else
         {
            severity = severity.max(item.getSeverity());
         }
      }


      final Iterator items = findingsInLine(line);
      while (items.hasNext())
      {
         final Item item = (Item) items.next();
         if (item.getOrigin() == Origin.COVERAGE)
         {
            if (item.getCounter() == 0)
            {
               severity = severity.max(Severity.COVERAGE);
            }
         }
         else
         {
            severity = severity.max(item.getSeverity());
            if (item.getEndLine() > line)
            {
               mActiveItems.add(item);
            }
         }
      }
      return severity;
   }

   private Iterator findingsInLine (int line)
   {
      final List findingsInLine
            = (List) mFindingsInFile.get(new Integer(line));
      final Iterator result;
      if (findingsInLine == null)
      {
         result = EmptyIterator.EMPTY_ITERATOR;
      }
      else
      {
         result = findingsInLine.iterator();
      }
      return result;
   }

   private String getHits (int line)
   {
      String hits = "&nbsp;";

      final Iterator items = findingsInLine(line);

      while (items.hasNext())
      {
         final Item item = (Item) items.next();
         if (item.getOrigin() == Origin.COVERAGE)
         {
            hits = String.valueOf(item.getCounter());
            break;
         }
      }
      return hits;
   }

   /**
    * Fills the 'Note' column for the given line.
    * @param line the line under inspection.
    * @return the content to be put in the notes column for the given line.
    */
   private String getIcons (int line)
   {
      final StringBuilder icons = new StringBuilder();
      mFindingsInCurrentLine.clear();

      // collect relevant findings
      final Iterator items = findingsInLine(line);
      while (items.hasNext())
      {
         final Item item = (Item) items.next();
         if (item.getOrigin() == Origin.COVERAGE)
         {
            if (item.getCounter() == 0)
            {
               items.remove(); // will never see this again!
            }
         }
         else if (item.getSeverity() == Severity.FILTERED)
         {
            // not listen with the code but in the global section below the
            // code.
         }
         else
         {
            mHandledFindings.add(item);
            // create the magic icon string with a hyperlink
            mGetIconsStringBuffer.setLength(0);
            mGetIconsStringBuffer.append("<a href='#FINDING");
            mGetIconsStringBuffer.append(mHandledFindings.size());
            mGetIconsStringBuffer.append("' title='");
            mGetIconsStringBuffer.append(
                  XmlUtil.attributeEscape(item.getMessage()));
            mGetIconsStringBuffer.append("'><span class='");
            mGetIconsStringBuffer.append(item.getOrigin());
            mGetIconsStringBuffer.append("note'>(");
            mGetIconsStringBuffer.append(mHandledFindings.size());
            mGetIconsStringBuffer.append(")</span></a>");
            icons.append(mGetIconsStringBuffer);
            if (item.isSetColumn())
            {
               final Integer pos = new Integer(item.getColumn());
               final String last = (String) mFindingsInCurrentLine.get(pos);
               if (last != null)
               {
                  mGetIconsStringBuffer.insert(0, last);
               }
               mFindingsInCurrentLine.put(pos,
                     mGetIconsStringBuffer.toString());
            }
            items.remove(); // item was handled fully
         }
      }
      if (icons.length() == 0)
      {
         icons.append("&nbsp;");
      }
      return icons.toString();
   }


   /**
    * Replaces leading whitespace by a none breakable html string
    * (entity).
    * Uses <code>mStringBuffer</code> as temporary string buffer.
    * @param in The string to modify.
    * @return The string with leading white spaces replaced.
    */
   private String replaceLeadingSpaces (String in)
   {
      final String result;
      if (in == null || in.length() == 0)
      {
         result = "&nbsp;";
      }
      else if (in.charAt(0) == ' ')
      {
         mStringBuffer.setLength(0);
         int i;

         for (i = 0; i < in.length() && in.charAt(i) == ' ';  i++)
         {
            mStringBuffer.append("&nbsp;");
         }
         mStringBuffer.append(in.substring(i));
         result = mStringBuffer.toString();
      }
      else
      {
         result = in;
      }
      return result;
   }

   /**
    * Adds the file summary to all summary lists.
    */
   private void addSummary (FileSummary summary)
   {
      mAllFiles.add(summary);

      List packageList = (List) mAllPackages.get(summary.getPackage());

      if (packageList == null)
      {
         packageList = new ArrayList();
         mAllPackages.put(summary.getPackage(), packageList);
      }
      packageList.add(summary);

      FileSummary packageSummary
         = (FileSummary) mPackageSummary.get(summary.getPackage());

      if (packageSummary == null)
      {
         packageSummary = new FileSummary(mPackage);
         mPackageSummary.put(summary.getPackage(), packageSummary);
      }
      packageSummary.add(summary);
      mGlobalSummary.add(summary);
   }

   private void createPackageSummary (List pkg)
         throws IOException
   {
      createPackageSummary(new FileSummary.SortByPackage(), pkg);
      createPackageSummary(new FileSummary.SortByQuality(), pkg);
      createPackageSummary(new FileSummary.SortByCoverage(), pkg);
   }

   private void createPackageSummary (Comparator order, List pkg)
      throws IOException
   {
      final String filename = fileNameForOrder(order);
      final String packageName = ((FileSummary) pkg.get(0)).getPackage();
      final String subdir = packageName.replaceAll("\\.", "/");
      final java.io.File dir = new java.io.File(mOutDir, subdir);
      dir.mkdirs();

      final BufferedWriter bw = openWriter(dir, filename);
      try
      {
          htmlHeader(bw, packageName, packageName);
          bw.write("<h1><a href='" + relativeRoot(packageName, filename)
             + "'>Project-Report "
             + mProjectName + "</a></h1>");
          bw.write("<h2>Packagesummary " + packageName + "</h2>");
          createClassListTable(bw, pkg, false, order);
          bw.write("</body></html>");
      }
      finally
      {
          IoUtil.close(bw);
      }
   }

   private void createFullSummary ()
         throws IOException
   {
      createFullSummary(new FileSummary.SortByPackage());
      createFullSummary(new FileSummary.SortByQuality());
      createFullSummary(new FileSummary.SortByCoverage());
   }

   private void createFullSummary (Comparator order)
         throws IOException
   {
      final String filename = fileNameForOrder(order);
      final BufferedWriter bw = openWriter(filename);
      try
      {
          htmlHeader(bw, "Project Report " + mProjectName, "");

          bw.write("<h1>Project Report " + mProjectName + "</h1>");

          bw.write("<table border='0' cellpadding='2' cellspacing='0' "
                + "width='95%'>");
          bw.write("<thead><tr><th>");
          if (filename != SORT_BY_PACKAGE_INDEX)
          {
             bw.write("<a href='" + SORT_BY_PACKAGE_INDEX
                   + "' title='Sort by name'>");
          }
          bw.write("Package");
          if (filename != SORT_BY_PACKAGE_INDEX)
          {
             bw.write("</a>");
          }
          bw.write("</th><th>findings</th>");
          bw.write("<th>files</th><th>lines</th>");
          if (mCoverageData)
          {
             bw.write("<th>%</th><th>");
             if (filename != SORT_BY_COVERAGE_INDEX)
             {
                bw.write("<a href='" + SORT_BY_COVERAGE_INDEX
                      + "' title='Sort by coverage'>");
             }
             bw.write("Coverage");
             if (filename != SORT_BY_COVERAGE_INDEX)
             {
                bw.write("</a>");
             }
             bw.write("</th>");
          }
          bw.write("<th>%</th><th class='remainder'>");
          if (filename != SORT_BY_QUALITY_INDEX)
          {
             bw.write("<a href='" + SORT_BY_QUALITY_INDEX
                   + "' title='Sort by quality'>");
          }
          bw.write("Quality");
          if (filename != SORT_BY_QUALITY_INDEX)
          {
             bw.write("</a>");
          }
          bw.write("</th></tr></thead>");
          bw.write("<tbody>");
          bw.write(NEWLINE);
          bw.write("<tr class='odd'><td class='classname" + LAST_MARKER + "'>");
          bw.write("Overall summary");
          bw.write("</td>");
          hitsCell(bw, String.valueOf(mGlobalSummary.getNumberOfFindings()),
                  true);
          hitsCell(bw, String.valueOf(mGlobalSummary.getNumberOfFiles()), true);
          hitsCell(bw, String.valueOf(mGlobalSummary.getLinesOfCode()), true);
          if (mCoverageData)
          {
             hitsCell(bw, String.valueOf(mGlobalSummary.getCoverage()) + "%",
                     true);
             bw.write("<td valign='middle' class='hits" + LAST_MARKER
                   + "' width='100'>");
             bw.write(mGlobalSummary.getCoverageBar());
             bw.write("</td>");
          }
          hitsCell(bw, String.valueOf(mGlobalSummary.getQuality()) + "%", true);
          bw.write("<td valign='middle' class='code" + LAST_MARKER
                + "' width='100'>");
          bw.write(mGlobalSummary.getPercentBar());
          bw.write("</td></tr>");
          bw.write(NEWLINE);
          bw.write("</tbody>");
          bw.write("</table>");

          bw.write("<h1><a href='findings.html'>View by Finding</a></h1>");

          bw.write("<h1>Packages</h1>");
          bw.write("<table border='0' cellpadding='2' cellspacing='0' "
                + "width='95%'>");
          bw.write("<thead><tr><th>Package</th>"
             + "<th>findings</th><th>files</th><th>lines</th>");
          if (mCoverageData)
          {
             bw.write("<th>%</th><th>Coverage</th>");
          }
          bw.write("<th>%</th><th class='remainder'>Quality</th></tr></thead>");
          bw.write("<tbody>");
          bw.write(NEWLINE);

          final Set packages = new TreeSet(order);
          packages.addAll(mPackageSummary.values());

          final Iterator i = packages.iterator();
          int pos = 0;
          while (i.hasNext())
          {
             pos++;
             final FileSummary pkg = (FileSummary) i.next();
             final boolean isLast = !i.hasNext();
             appendPackageLink(bw, pkg, filename, pos, isLast);
          }
          bw.write("</tbody></table>\n");

          // findings with no line number...
          createUnassignedFindingsTable(bw);

          bw.write("<h1>Java Files</h1>");
          createClassListTable(bw, mAllFiles, true, order);

          bw.write("</body></html>");
      }
      finally
      {
          IoUtil.close(bw);
      }
   }

    private void createUnassignedFindingsTable (final BufferedWriter bw)
        throws IOException
    {
        boolean tableOpened = false;
        final Iterator i = mGlobalFindings.iterator();
        int row = 0;
        while (i.hasNext())
        {
            final org.jcoderz.phoenix.report.jaxb.File file
                = (org.jcoderz.phoenix.report.jaxb.File) i.next();
            final Iterator j = file.getItem().iterator();
            while (j.hasNext())
             {
                row++;
                final Item item = (Item) j.next();
                if (!tableOpened)
                {
                    bw.write("<h1>Unassigned findings</h1>");
                    bw.write("<table border='0' cellpadding='0' "
                        + "cellspacing='0' width='95%'>");
                    tableOpened = true;
                }
                bw.write("<tr class='");
                bw.write(item.getSeverity().toString());
                bw.write(Java2Html.toOddEvenString(row));
                bw.write("'><td class='unassigned-filename'>");
                bw.write(cutPath(file.getName()));
                bw.write("</td><td class='unassigned-data' width='100%'>");
                bw.write(item.getMessage());
                bw.write("</td></tr>");

                FindingsSummary.addFinding(item, mGlobalSummary);
             }
         }
        if (tableOpened)
        {
            bw.write("</table>");
        }
    }

    private void appendPackageLink (final BufferedWriter bw,
            final FileSummary pkg, final String filename, final int pos,
            final boolean isLast)
        throws IOException
    {
         final String name = pkg.getPackage();
         final String subdir = name.replaceAll("\\.", "/");
         bw.write("<tr class='" + Java2Html.toOddEvenString(pos)
               + "'><td class='classname");
         appendIf(bw, isLast, LAST_MARKER);
         bw.write("'><a href='" + subdir + "/" + filename + "'>");
         bw.write(pkg.getPackage());
         bw.write("</a></td>");
         hitsCell(bw, String.valueOf(pkg.getNumberOfFindings()), isLast);
         hitsCell(bw, String.valueOf(pkg.getNumberOfFiles()), isLast);
         hitsCell(bw, String.valueOf(pkg.getLinesOfCode()), isLast);
         if (mCoverageData)
         {
            hitsCell(bw, String.valueOf(pkg.getCoverage()) + "%", isLast);
            bw.write("<td valign='middle' class='hits");
            appendIf(bw, isLast, LAST_MARKER);
            bw.write("' width='100'>");
            bw.write(pkg.getCoverageBar());
            bw.write("</td>");
         }
         hitsCell(bw, String.valueOf(pkg.getQuality()) + "%", isLast);
         bw.write("<td valign='middle' class='code");
         appendIf(bw, isLast, LAST_MARKER);
         bw.write("' width='100'>");
         bw.write(pkg.getPercentBar());
         bw.write("</td></tr>" + NEWLINE);
    }

   /**
    * Writes a html header to the given output stream.
    * @param bw the stream to use for output
    * @param title the title of the page. Should be the package name for
    *        sub packages.
    */
   private void htmlHeader (BufferedWriter bw, String title, String packageName)
        throws IOException
   {
       bw.write("<?xml version='1.0' encoding='UTF-8'?>" + NEWLINE
         + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" "
         + NEWLINE
         + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
         + NEWLINE
         + "<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en'"
         + " lang='en'>" + NEWLINE
         + "<head>" + NEWLINE
         + "\t<title>");
      bw.write(title);
      bw.write("</title>" + NEWLINE
         + "\t<meta name='author' content='jCoderZ java2html' />" + NEWLINE);
      bw.write(createStyle(packageName, mStyle));
      bw.write(NEWLINE + "</head>" + NEWLINE + "<body>" + NEWLINE);
   }

   private String relativeRoot (String currentPackage)
   {
      return relativeRoot(currentPackage, "index.html");
   }

   private static String relativeRoot (String currentPackage, String page)
   {
      final StringBuilder rootDir = new StringBuilder();

      if (currentPackage.length() != 0)
      {
         rootDir.append("../");
      }

      for (int i = 0; i < currentPackage.length(); i++)
      {
         if (currentPackage.charAt(i) == '.')
         {
            rootDir.append("../");
         }
      }
      rootDir.append(page);

      return rootDir.toString().replaceAll("//", "/");
   }

   private String cutPath (String fileName)
   {
      String result = fileName;
      if (fileName.toLowerCase(Constants.SYSTEM_LOCALE)
              .startsWith(mProjectHome.toLowerCase(Constants.SYSTEM_LOCALE)))
      {
         result = fileName.substring(mProjectHome.length());
      }
      return result;
   }

   private String getCvsLink (String absFile)
   {
      String result;
      if (mCvsBase == null || mProjectHome == null)
      {
         result = null;
      }
      else if (absFile.toLowerCase(Constants.SYSTEM_LOCALE)
              .startsWith(mProjectHome.toLowerCase(Constants.SYSTEM_LOCALE)))
      {
         result = absFile.substring(mProjectHome.length());
      }
      else
      {
         absFile = (new java.io.File(absFile)).getAbsolutePath();
         if (absFile.toLowerCase(Constants.SYSTEM_LOCALE)
                 .startsWith(mProjectHome.toLowerCase(Constants.SYSTEM_LOCALE)))
         {
            result = absFile.substring(mProjectHome.length());
         }
         else
         {
            result = null;
         }

      }
      if (result != null)
      {
         result = mCvsBase + result;
         result = result.replaceAll("\\\\", "/");
      }
      return result;
   }

   private String addLineItems (String line)
   {
      if (mFindingsInCurrentLine.isEmpty())
      {
         return line;
      }
      final StringBuffer result = mStringBuffer;
      result.setLength(0);

      int pos = 1;  // counting starts with 1
      mFindingsInCurrentLine.remove(INTEGER_ZERO); // global findings
      boolean quote = false;
      boolean entity = false;
      for (int i = 0; i < line.length(); i++)
      {
         final char current = line.charAt(i);
         if (current == '<')
         {
            quote = true;
         }
         else
         {
            if (quote)
            {
               quote = (current != '>');
               entity &= (current != ';');
            }
            else if (entity)
            {
               entity = (current != ';');
            }
            else
            {
               final String finding
                     = (String) mFindingsInCurrentLine.remove(
                         new Integer(pos));
               if (finding != null)
               {
                  result.append(finding);
               }
               pos++;
            }
         }
         result.append(current);
         if (current == '&')
         {
            entity = true;
         }
      }

      final Iterator i = mFindingsInCurrentLine.keySet().iterator();
      while (i.hasNext())
      {
         final Integer xx = (Integer) i.next();
         final String finding = (String) mFindingsInCurrentLine.get(xx);
         i.remove();
         if (finding != null)
         {
            result.append(finding);
         }
      }
      return result.toString();
   }

   /**
    * Creates a list of all classes as html table and appends it to the
    * given bw.
    */
   private void createClassListTable (BufferedWriter bw, Collection files,
      boolean fullPackageNames, Comparator order)
         throws IOException
   {
      // Do not create a table if no classes are here.
      if (!files.isEmpty())
      {
         final String filename = fileNameForOrder(order);
         final FileSummary[] summaries =
            (FileSummary[]) files.toArray(new FileSummary[files.size()]);

         Arrays.sort(summaries, order);

         bw.write("<table border='0' cellpadding='2' cellspacing='0' "
               + "width='95%'>");
         bw.write("<thead><tr><th>");
         if (filename != SORT_BY_PACKAGE_INDEX)
         {
            bw.write("<a href='");
            bw.write(SORT_BY_PACKAGE_INDEX);
            bw.write("' title='Sort by name'>");
         }
         bw.write("Classfile");
         if (filename != SORT_BY_PACKAGE_INDEX)
         {
            bw.write("</a>");
         }
         bw.write("</th><th>findings</th><th>lines</th>");
         if (mCoverageData)
         {
            bw.write("<th>%</th><th>");
            if (filename != SORT_BY_COVERAGE_INDEX)
            {
               bw.write("<a href='");
               bw.write(SORT_BY_COVERAGE_INDEX);
               bw.write("' title='Sort by coverage'>");
            }
            bw.write("Coverage");
            if (filename != SORT_BY_COVERAGE_INDEX)
            {
               bw.write("</a>");
            }
            bw.write("</th>");
         }
         bw.write("<th>%</th><th class='remainder'>");
         if (filename != SORT_BY_QUALITY_INDEX)
         {
            bw.write("<a href='");
            bw.write(SORT_BY_QUALITY_INDEX);
            bw.write("' title='Sort by quality'>");
         }
         bw.write("Quality");
         if (filename != SORT_BY_QUALITY_INDEX)
         {
            bw.write("</a>");
         }
         bw.write("</th></tr></thead>");
         bw.write("<tbody>");
         bw.write(NEWLINE);

         int pos = 0;
         final Iterator i = Arrays.asList(summaries).iterator();
         while (i.hasNext())
         {
            pos++;
            final FileSummary file = (FileSummary) i.next();
            final boolean isLast = !i.hasNext();
            appendClassLink(bw, file, fullPackageNames, pos, isLast);
         }
         bw.write("</tbody>");
         bw.write("</table>");
      }
      else
      {
         bw.write("EMPTY!?");
      }
   }

    private void appendClassLink (BufferedWriter bw, final FileSummary file,
            boolean fullPackageNames, int pos, final boolean isLast)
        throws IOException
    {
        final String name;
        final String link;
        if (fullPackageNames)
        {
           name = file.getPackage() + '.' + file.getClassName();
           link = file.getPackage().replaceAll("\\.", "/") + "/"
                   + file.getClassName() + ".html";
        }
        else
        {
           name = file.getClassName();
           link = file.getClassName() + ".html";
        }
        bw.write("<tr class='");
        bw.write(Java2Html.toOddEvenString(pos));
        bw.write("'><td class='classname");
        appendIf(bw, isLast, LAST_MARKER);
        bw.write("'><a href='");
        bw.write(link);
        bw.write("'>");
        bw.write(name);
        bw.write("</a></td>");
        hitsCell(bw, String.valueOf(file.getNumberOfFindings()), isLast);
        hitsCell(bw, String.valueOf(file.getLinesOfCode()), isLast);
        if (mCoverageData)
        {
           hitsCell(bw, String.valueOf(file.getCoverage()) + "%", isLast);
           bw.write("<td valign='middle' class='hits");
           appendIf(bw, isLast, LAST_MARKER);
           bw.write("' width='100'>");
           bw.write(file.getCoverageBar());
           bw.write("</td>");
        }
        hitsCell(bw, String.valueOf(file.getQuality()) + "%", isLast);
        bw.write("<td valign='middle' class='code");
        appendIf(bw, isLast, LAST_MARKER);
        bw.write("' width='100'>");
        bw.write(file.getPercentBar());
        bw.write("</td></tr>");
        bw.write(NEWLINE);
    }

   private static String fileNameForOrder (Comparator order)
   {
      String filename;
      if (order instanceof FileSummary.SortByPackage)
      {
         filename = SORT_BY_PACKAGE_INDEX;
      }
      else if (order instanceof FileSummary.SortByQuality)
      {
         filename = SORT_BY_QUALITY_INDEX;
      }
      else if (order instanceof FileSummary.SortByCoverage)
      {
         filename = SORT_BY_COVERAGE_INDEX;
      }
      else
      {
         throw new RuntimeException("Order not expected " + order);
      }
      return filename;
   }

   /**
    * Generates the content (including surounding elements) of a cell
    * of hits type.
    * @param bw the writer to write to.
    * @param content the cell contend
    * @param isLast true if this sell is in the last row.
    * @throws IOException if the write fails.
    */
   private static void hitsCell (BufferedWriter bw,
         String content, boolean isLast)
         throws IOException
   {
      bw.write("<td valign='middle' class='hits");
      appendIf(bw, isLast, LAST_MARKER);
      bw.write("'>");
      bw.write(content);
      bw.write("</td>");
   }

   private static void appendIf (BufferedWriter bw, boolean condition,
         String str)
         throws IOException
   {
      if (condition)
      {
         bw.write(str);
      }
   }

   private BufferedWriter openWriter (String filename)
       throws IOException
   {
       return openWriter(mOutDir, filename);
   }

   private BufferedWriter openWriter (File dir, String filename)
       throws IOException
   {
       FileOutputStream fos = null;
       OutputStreamWriter osw = null;
       BufferedWriter result = null;
       try
       {
           fos = new FileOutputStream(new java.io.File(dir, filename));
           osw = new OutputStreamWriter(fos, Constants.ENCODING_UTF8);
           result = new BufferedWriter(osw);
       }
       catch (RuntimeException ex)
       {
           IoUtil.close(result);
           IoUtil.close(osw);
           IoUtil.close(fos);
           throw ex;
       }
       catch (IOException ex)
       {
           IoUtil.close(result);
           IoUtil.close(osw);
           IoUtil.close(fos);
           throw ex;
       }
       return result;
   }
}
