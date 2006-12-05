/*
 * $Id: FileUtils.java 204 2006-06-22 10:54:21Z amandel $
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

import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.chart2d.Chart2DProperties;
import net.sourceforge.chart2d.Dataset;
import net.sourceforge.chart2d.GraphChart2DProperties;
import net.sourceforge.chart2d.GraphProperties;
import net.sourceforge.chart2d.LBChart2D;
import net.sourceforge.chart2d.LegendProperties;
import net.sourceforge.chart2d.MultiColorsProperties;
import net.sourceforge.chart2d.Object2DProperties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jcoderz.commons.util.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * This is the Ant task for the Jcoderz Summary Report.
 * This task uses a summary database file to create progress diagrams
 * showing the historical changes of the number of findings.
 *
 * TODO: Separate into Command Line tool and Ant Task so that the
 *       java.awt.headless property can be set by the Ant Task.
 * <ol>
 *   <li>Read the summary db file or create a new one if none exists.</li>
 *   <li>Check whether new folders have been created in the basedir.</li>
 *   <li>Update the summary db file with the information from new folders.</li>
 *   <li>Generate the diagrams based on the summary information.</li>
 * </ol>
 *
 * @author Michael Rumpf
 */
public class JcSummaryReportAntTask
   extends Task
{
   /** The number of columns in the CSV file. */
   private static final int COLUMN_COUNT = 8;
   private static final String[] MONTHS = new String[] {"January", "February",
         "March", "April", "Mai", "June", "July", "August", "September",
         "October", "November", "December"};
   private static final int YEAR_LEN = 4;
   private static final int MONTH_LEN = 2;
   private static final int DAY_LEN = 2;
   private static final int HOUR_LEN = 2;
   private static final int MINUTE_LEN = 2;

   private static final int ROW_ERROR = 0;
   private static final int ROW_WARNING = 1;
   private static final int ROW_INFO = 2;
   private static final int ROW_COVERAGE = 3;

   private static final int ROW_LOC = 0;
   private static final int ROW_CODELOC = 1;

   private static final int ROW_QUALITY = 0;

   private static final int LARGE_IMAGE_WIDTH = 800;
   private static final int LARGE_IMAGE_HEIGHT = 600;
   private static final int SMALL_IMAGE_WIDTH = 400;
   private static final int SMALL_IMAGE_HEIGHT = 300;
   private static final Dimension LARGE_SIZE
         = new Dimension(LARGE_IMAGE_WIDTH, LARGE_IMAGE_HEIGHT);
   private static final Dimension SMALL_SIZE
         = new Dimension(SMALL_IMAGE_WIDTH, SMALL_IMAGE_HEIGHT);

   private String mName = null;
   private File mDestDir = null;
   private File mBaseDir = null;
   private File mSummaryDbFile = null;

   /**
    * Sets the name of the summary report.
    *
    * @param name the report name.
    */
   public void setName (String name)
   {
      mName = name;
   }


   /**
    * Sets the destination of the report.
    *
    * @param dest the report destination.
    */
   public void setDest (String dest)
   {
      mDestDir = new File(dest);
      if (!mDestDir.exists())
      {
         mDestDir.mkdirs();
      }
   }


   /**
    * Sets the base directory of the reports.
    *
    * @param base the base directory.
    */
   public void setBaseDir (String base)
   {
      mBaseDir = new File(base);
      if (!mBaseDir.exists())
      {
         mBaseDir.mkdirs();
      }
   }


   /**
    * Sets the historic database file.
    *
    * @param summary the historic database file.
    */
   public void setSummary (String summary)
   {
      mSummaryDbFile = new File(summary);
   }


   /**
    * This method is called by Ant for executing this task.
    *
    * @throws BuildException whenver a problem occurs.
    */
   public void execute ()
      throws BuildException
   {
      try
      {
         // Always show this line
         log("Executing JcSummaryReportAntTask...");

         if (mSummaryDbFile == null)
         {
            throw new BuildException("Summary database must be specified!");
         }

         if (mDestDir == null)
         {
            throw new BuildException("Destination folder must be specified!");
         }
         if (mDestDir.exists())
         {
            if (!mDestDir.isDirectory())
            {
               throw new BuildException("The destination directory '" + mDestDir
                     + "' must be folder!");
            }
         }
         else
         {
            mDestDir.mkdirs();
         }

         if (mBaseDir == null)
         {
            throw new BuildException("The base directory must be specified!");
         }
         if (!mBaseDir.exists() || !mBaseDir.isDirectory())
         {
            throw new BuildException("The base directory '"
                  + mBaseDir + "' must exists and must be a folder!");
         }

         
         // Stores a mapping between a timestamp and a Summary instance. This
         // reads all lines in the summary db file and checks for new folders
         final Map summaryMap = readSummaryDb(mSummaryDbFile, mBaseDir);
         // Write the summary file
         writeSummaryDb(mSummaryDbFile, summaryMap);

         // Create a complex map hierarchy: year -> month -> day -> List
         // The leaf list is a list of reports that might have been created
         // on one day.
         final Map ymd2SummaryMap = createSummaryDbMap(summaryMap);

         createFindingsChart(ymd2SummaryMap, summaryMap, "Findings");
         createQualityChart(ymd2SummaryMap, summaryMap, "Quality");
         createLocChart(ymd2SummaryMap, summaryMap, "LOC");

         renderHtmlView(ymd2SummaryMap, summaryMap);
      }
      catch (IOException ex)
      {
         throw new BuildException("An unexpected IO exception occured!", ex);
      }
   }


   /**
    * Writes the summary database file.
    *
    * @param summaryDbFile the summary database file.
    */
   private void writeSummaryDb (File summaryDbFile, Map summaryMap)
   {
      try
      {
         log("Writing summary file " + summaryDbFile);
         summaryDbFile.createNewFile();
         final FileOutputStream fos = new FileOutputStream(summaryDbFile);
         final PrintWriter pw = new PrintWriter(fos);
         pw.println("Timestamp;Error;Warning;Info;"
               + "Coverage;Loc;CodeLoc;Quality");
   
         // Sort the keySet before writing the CSV file
         final Set keySet = summaryMap.keySet();
         final List keyList = new ArrayList(keySet);
         Collections.sort(keyList);
         final Iterator iter = keyList.iterator();
         while (iter.hasNext())
         {
            Long ts = (Long) iter.next();
            final Summary sum = (Summary) summaryMap.get(ts);
            pw.print(sum.getTimestamp() + ";");
            pw.print(sum.getError() + ";");
            pw.print(sum.getWarning() + ";");
            pw.print(sum.getInfo() + ";");
            pw.print(sum.getCoverage() + ";");
            pw.print(sum.getLoc() + ";");
            pw.print(sum.getCodeLoc() + ";");
            pw.println(FileSummary.calculateQuality(sum.getLoc(),
                  sum.getInfo(), sum.getWarning(),
                  sum.getError(), sum.getCoverage()));
         }

         fos.flush();
         FileUtils.safeClose(pw);
         FileUtils.safeClose(fos);
      }
      catch (IOException ex)
      {
         throw new BuildException("Could not write summary database '"
               + summaryDbFile + "'!");
      }
   }


   /**
    * This method creates a mapping between timestamp (e.g. 20061122103015)
    * and the summary information.
    *
    * @param summaryDbFile the summary file.
    * @param baseDir the basedir with the report folders.
    * @return The map of timestamps and summary instances.
    */
   private Map readSummaryDb (File summaryDbFile, File baseDir)
   {
      final Map files = new HashMap();
      if (summaryDbFile.exists())
      {
         log("Reading summary database...");
         FileReader fr = null;
         BufferedReader br = null;
         try
         {
            fr = new FileReader(summaryDbFile);
            br = new BufferedReader(fr);
            String line = br.readLine();
            while (line != null)
            {
               if (line.indexOf("Error") == -1
                     && line.indexOf("Warning") == -1
                     && line.indexOf("Info") == -1
                     && line.indexOf("Coverage") == -1)
               {
                  final Summary sum = createSummary(line, baseDir);
                  log("Summary information from database: " + sum);
                  if (sum != null)
                  {
                    files.put(new Long(sum.getTimestamp()), sum);
                  }
               }
               line = br.readLine();
            }
         }
         catch (IOException ex)
         {
            throw new BuildException("An IO exception occured while reading '"
                  + summaryDbFile + "'!", ex);
         }
         finally
         {
            FileUtils.safeClose(br);
            FileUtils.safeClose(fr);
         }
      }

      // Now check whether more folders can be found
      final String[] folders = baseDir.list(new JcoderReportFilter());
      for (int i = 0; i < folders.length; i++)
      {
         if (!files.containsKey(Long.valueOf(folders[i])))
         {
            log("New report sub-folder found: " + folders[i]);
            final File folder = new File(baseDir, folders[i]);
            Long ts = null;
            try
            {
               ts = Long.valueOf(folders[i]);
            }
            catch (NumberFormatException ex)
            {
               log("The folder '" + folders[i] + "' is not a timestamp folder!");
            }
            final Summary sum = readSummaryXml(new File(folder, "summary.xml"), ts);
            files.put(new Long(sum.getTimestamp()), sum);
         }
         else
         {
            log("Report sub-folder '" + folders[i] + "' already in database!");
         }
      }
      return files;
   }


   private Summary readSummaryXml (File summaryXml, Long folderTimestamp)
   {
      Summary sum = null;
      try
      {
         final DocumentBuilderFactory factory =
               DocumentBuilderFactory.newInstance();
         final Document doc = factory.newDocumentBuilder().parse(summaryXml);
         // There is only one root node
         final Node root = doc.getDocumentElement();
         final NamedNodeMap attrs = root.getAttributes();
         // Ignore the timestamp as the external script might choose another
         // timestamp for the folder than the jcoderz-report has chosen
         // for the XML file.
         long ts;
         if (folderTimestamp == null)
         {
             ts = Long.parseLong(
                     attrs.getNamedItem("timestamp").getNodeValue().trim());
         }
         else
         {
             ts = folderTimestamp.longValue();
         }
         final int error = Integer.parseInt(
               attrs.getNamedItem("error").getNodeValue().trim());
         final int warning = Integer.parseInt(
               attrs.getNamedItem("warning").getNodeValue().trim());
         final int info = Integer.parseInt(
               attrs.getNamedItem("info").getNodeValue().trim());
         final int coverage = Integer.parseInt(
               attrs.getNamedItem("coverage").getNodeValue().trim());
         final int loc = Integer.parseInt(
               attrs.getNamedItem("loc").getNodeValue().trim());
         final int codeloc = Integer.parseInt(
               attrs.getNamedItem("codeLoc").getNodeValue().trim());
         final double quality = Double.parseDouble(
               attrs.getNamedItem("quality").getNodeValue());
         sum = new Summary(ts, error, warning, info, coverage,
               loc, codeloc, quality, summaryXml);
      }
      catch (ParserConfigurationException ex)
      {
         throw new BuildException("A parser configuration error occured!", ex);
      }
      catch (SAXException ex)
      {
         throw new BuildException("A SAX exception occured while parsing '"
               + summaryXml + "'!", ex);
      }
      catch (IOException ex)
      {
         throw new BuildException("An IO exception occured while parsing '"
               + summaryXml + "'!", ex);
      }

      return sum;
   }


   private void storeInMap (Map files, long timestamp, Summary sum)
   {
      log("Storing summmary: " + sum);
      final Long ts = new Long(timestamp);
      final String tsStr = ts.toString();
      final Integer y = Integer.valueOf(tsStr.substring(0, 4));
      final Integer m = Integer.valueOf(tsStr.substring(4, 6));
      final Integer d = Integer.valueOf(tsStr.substring(6, 8));
      if (!files.containsKey(y))
      {
         log("Adding hash map for year " + y);
         files.put(y, new HashMap());
      }
      final Map year = (Map) files.get(y);
      if (!year.containsKey(m))
      {
         log("Adding hash map for month " + m);
         year.put(m, new HashMap());
      }
      final Map month = (Map) year.get(m);
      if (!month.containsKey(d))
      {
         log("Adding report list for day " + d);
         month.put(d, new ArrayList());
      }
      final List reports = (List) month.get(d);
      reports.add(sum);
   }


   private Map createSummaryDbMap (Map ts2SumMap)
   {
      final Map files = new HashMap();
      final Iterator iter = ts2SumMap.keySet().iterator();
      while (iter.hasNext())
      {
         final Long ts = (Long) iter.next();
         final Summary sum = (Summary) ts2SumMap.get(ts);
         storeInMap(files, ts.longValue(), sum);
      }
      return files;
   }


   private Summary createSummary (String sum, File baseDir)
   {
      final StringTokenizer strtok = new StringTokenizer(sum, ";");
      if (strtok.countTokens() != COLUMN_COUNT)
      {
         throw new BuildException(
               "Number of columns in summary database file '"
               + strtok.countTokens()
               + "' does not match the expected number of '"
               + COLUMN_COUNT + "'!");
      }
      Summary summary = null;
      final long timestamp = Long.parseLong(strtok.nextToken());
      final int error = Integer.parseInt(strtok.nextToken());
      final int warning = Integer.parseInt(strtok.nextToken());
      final int info = Integer.parseInt(strtok.nextToken());
      final int coverage = Integer.parseInt(strtok.nextToken());
      final int loc = Integer.parseInt(strtok.nextToken());
      final int codeloc = Integer.parseInt(strtok.nextToken());
      final double quality = Double.parseDouble(strtok.nextToken());
      final File summaryFile = new File(baseDir, "" + timestamp
            + File.separator + "summary.xml");
      if (summaryFile.exists() && summaryFile.isFile())
      {
         summary = new Summary(timestamp, error, warning, info, coverage, loc,
            codeloc, quality, summaryFile);
      }
      else
      {
         log("Summary file does not exist: " + summaryFile);
      }
      return summary;
   }


   /**
    * Creates the quality chart.
    * The output is a "quality.png" and a "quality_small.png" image.
    *
    * @throws IOException if the image can not be written.
    */
   private void createQualityChart (Map ymd2SummaryMap, Map summaryMap, String title)
         throws IOException
   {
      final Set labels = new TreeSet(summaryMap.keySet());
      if (labels.size() == 0)
      {
         throw new RuntimeException("No reports found for chart!");
      }

      final String[] legendLabels = {"Quality"};
      
      // Configure dataset
      final Dataset dataset = new Dataset(legendLabels.length,
         summaryMap.size(), 1);

      final List labelsAxisLabels = new ArrayList();
      final Iterator iter = summaryMap.keySet().iterator();
      int i = 0;
      while (iter.hasNext())
      {
         final Long ts = (Long) iter.next();
         labelsAxisLabels.add(ts.toString());
         final Summary sum = (Summary) summaryMap.get(ts);

         dataset.set(ROW_QUALITY, i, 0, (float) sum.getQuality());
         i++;
      }

      final GraphChart2DProperties graphChart2DProps
            = createGraphChart2DProperties(labelsAxisLabels, title);
      final MultiColorsProperties multiColorsProps
            = createMultiColorsProperties(new Color[] {Color.GREEN});
      createChart(title, legendLabels, dataset, graphChart2DProps, multiColorsProps);
   }


   /**
    * Creates the lines-of-code chart.
    * The output is a "loc.png" and a "loc_small.png" image.
    *
    * @throws IOException if the image can not be written.
    */
   private void createLocChart (Map ymd2SummaryMap, Map summaryMap, String title)
         throws IOException
   {
      final Set labels = new TreeSet(summaryMap.keySet());
      if (labels.size() == 0)
      {
         throw new RuntimeException("No reports found for chart!");
      }

      final String[] legendLabels = {"Loc", "CodeLoc"};
      
      // Configure dataset
      final Dataset dataset = new Dataset(legendLabels.length,
         summaryMap.size(), 1);
      final List labelsAxisLabels = new ArrayList();
      final Iterator iter = summaryMap.keySet().iterator();
      int i = 0;
      while (iter.hasNext())
      {
         final Long ts = (Long) iter.next();
         labelsAxisLabels.add(ts.toString());
         final Summary sum = (Summary) summaryMap.get(ts);

         dataset.set(ROW_LOC, i, 0, sum.getLoc());
         dataset.set(ROW_CODELOC, i, 0, sum.getCodeLoc());
         i++;
      }

      final GraphChart2DProperties graphChart2DProps
            = createGraphChart2DProperties(labelsAxisLabels, title);
      final MultiColorsProperties multiColorsProps
            = createMultiColorsProperties(new Color[] {Color.BLUE, Color.PINK});
      createChart(title, legendLabels, dataset, graphChart2DProps, multiColorsProps);
   }


   /**
    * Creates the findings chart.
    * The output is a "findings.png" and a "findings_small.png" image.
    *
    * @throws IOException if the image can not be written.
    */
   private void createFindingsChart (Map ymd2SummaryMap, Map summaryMap, String title)
         throws IOException
   {
     final Set labels = new TreeSet(summaryMap.keySet());
     if (labels.size() == 0)
     {
        throw new RuntimeException("No reports found for chart!");
     }

     final String[] legendLabels = {"Error", "Warning", "Info", "Coverage"};
     
     // Configure dataset
     final Dataset dataset = new Dataset(legendLabels.length,
        summaryMap.size(), 1);
     final List labelsAxisLabels = new ArrayList();
     final Iterator iter = summaryMap.keySet().iterator();
     int i = 0;
     while (iter.hasNext())
     {
        final Long ts = (Long) iter.next();
        labelsAxisLabels.add(ts.toString());
        final Summary sum = (Summary) summaryMap.get(ts);

        dataset.set(ROW_ERROR, i, 0, sum.getError());
        dataset.set(ROW_WARNING, i, 0, sum.getWarning());
        dataset.set(ROW_INFO, i, 0, sum.getInfo());
        dataset.set(ROW_COVERAGE, i, 0, sum.getCoverage());
        i++;
     }

     final GraphChart2DProperties graphChart2DProps
           = createGraphChart2DProperties(labelsAxisLabels, title);
     final MultiColorsProperties multiColorsProps
           = createMultiColorsProperties(new Color[] {Color.RED, Color.YELLOW,
              Color.CYAN, Color.MAGENTA});
     createChart(title, legendLabels, dataset, graphChart2DProps, 
             multiColorsProps);
   }


   private void createChart (final String title, final String[] legendLabels, 
           final Dataset dataset, 
           final GraphChart2DProperties graphChart2DProps, 
           final MultiColorsProperties multiColorsProps) throws IOException
   {
      // Configure chart
      final LBChart2D chart2D = new LBChart2D();
      chart2D.setObject2DProperties(createObject2DProps());
      chart2D.setChart2DProperties(createChart2DProperties());
      chart2D.setLegendProperties(createLegendProperties(legendLabels));

      chart2D.setGraphChart2DProperties(graphChart2DProps);
      chart2D.addGraphProperties(createGraphProperties());
      chart2D.addDataset(dataset);
      chart2D.addMultiColorsProperties(multiColorsProps);

      chart2D.setMaximumSize(LARGE_SIZE);
      chart2D.setPreferredSize(LARGE_SIZE);

      final String titleSmall = title.toLowerCase();
      if (chart2D.validate(false))
      {
         java.io.File file = new java.io.File(mDestDir, titleSmall + ".png");
         javax.imageio.ImageIO.write(chart2D.getImage(), "PNG", file);
         chart2D.setMaximumSize(SMALL_SIZE);
         chart2D.setPreferredSize(SMALL_SIZE);
         chart2D.pack();
         file = new java.io.File(mDestDir, titleSmall + "_small.png");
         javax.imageio.ImageIO.write(chart2D.getImage(), "PNG", file);
      }
      else
      {
         chart2D.validate(true);
      }
   }


   private MultiColorsProperties createMultiColorsProperties (Color[] colors)
   {
      // Configure graph component colors
      final MultiColorsProperties multiColorsProps 
          = new MultiColorsProperties();
      multiColorsProps.setColorsCustomize(true);
      multiColorsProps.setColorsCustom(colors);
      return multiColorsProps;
   }


   private Object2DProperties createObject2DProps ()
   {
      final Object2DProperties object2DProps = new Object2DProperties();
      object2DProps.setObjectBackgroundLightSource(Object2DProperties.NONE);
      object2DProps.setObjectBackgroundColor(Color.LIGHT_GRAY);
      return object2DProps;
   }

   
   private GraphProperties createGraphProperties ()
   {
      // Configure graph properties
      final GraphProperties graphProps = new GraphProperties();
      graphProps.setGraphBarsExistence(false);
      graphProps.setGraphLinesExistence(true);
      graphProps.setGraphLinesThicknessModel(1);
      return graphProps;
   }


   private Chart2DProperties createChart2DProperties ()
   {
      // Configure chart properties
      final Chart2DProperties chart2DProps = new Chart2DProperties();
      chart2DProps.setChartDataLabelsPrecision(0);
      return chart2DProps;
   }

   
   private LegendProperties createLegendProperties (String[] legendLabels)
   {
      // Configure legend properties
      final LegendProperties legendProps = new LegendProperties();
      legendProps.setLegendLabelsTexts(legendLabels);
      return legendProps;
   }


   private GraphChart2DProperties createGraphChart2DProperties (
           List labelsAxisLabels, String title)
   {
      // Configure graph chart properties
      final GraphChart2DProperties graphChart2DProps =
            new GraphChart2DProperties();
      final String[] labelsTexts = new String[labelsAxisLabels.size()];
      graphChart2DProps.setLabelsAxisLabelsTexts((String[])
            labelsAxisLabels.toArray(labelsTexts));
      // The name of the Y-Axis
      graphChart2DProps.setNumbersAxisTitleText(title);
      graphChart2DProps.setLabelsAxisTicksAlignment(
            GraphChart2DProperties.CENTERED);
      return graphChart2DProps;
   }

   
   private void renderHtmlView (Map summaryDbMap, Map ts2BaseFolder)
   {
      try
      {
         // TODO: Refactor to reduce complexity
         final FileOutputStream fos = new FileOutputStream(
               new File(mDestDir, "index.html"));
         final PrintWriter pw = new PrintWriter(fos);
         pw.println("<html><head>\n<title>");
         pw.println(mName);
         pw.println("</title>");
         pw.println("<style type=\"text/css\">");
         pw.println("<!--");
         
         pw.println("hr { width: 100%; height: 1px; border-style: none; "
               + "background-color: black; }"); 
         pw.println("a { text-decoration: underline; }"); 
         pw.println("a:hover { text-decoration: none; }"); 
         pw.println("table { border-collapse: collapse; }"); 
         pw.println("table.month { border: 1px solid black; margin: 1em; "
               + "background-color: #eeeeee; }");
         pw.println("td.day_even { background-color: #bbbbbb; width: 1.5em; "
               + "text-align: center; padding: 0.2em; }");
         pw.println("td.day_odd { background-color: #cccccc; width: 1.5em; "
               + "text-align: center; padding: 0.2em; }");
         pw.println("img { border:none; }");
         pw.println("div.datelink { text-align:left; color: black; }"); 
         pw.println("div.error { text-align:left; color: red; }"); 
         pw.println("div.warning { text-align:left; color: yellow; }"); 
         pw.println("div.info { text-align:left; color: cyan; }"); 
         pw.println("div.coverage { text-align:left; color: magenta; }"); 

         pw.println("-->");
         pw.println("</style>");
         pw.println("<body>");
         pw.println("<table><tr><td>");
         pw.println("<a href=\"findings.png\"><img src=\"findings_small.png\">"
               + "</a>");
         pw.println("</td><td>");
         pw.println("<a href=\"quality.png\"><img src=\"quality_small.png\">"
               + "</a>");
         pw.println("</td><td>");
         pw.println("<a href=\"loc.png\"><img src=\"loc_small.png\">"
               + "</a>");
         pw.println("</td></tr></table>");

         pw.println("<table><tr><td>");

         final Map yearMap = (Map) summaryDbMap;
         // TODO: Move to method
         final List sortedYearList = new ArrayList();
         sortedYearList.addAll(yearMap.keySet());
         Collections.sort(sortedYearList);
         Collections.reverse(sortedYearList);
         final Iterator yearIter = sortedYearList.iterator();
         while (yearIter.hasNext())
         {
            final Integer year = (Integer) yearIter.next();
            log("Rendering detail tables for year " + year);

            final Map monthMap = (Map) summaryDbMap.get(year);
            // TODO: Move to method
            final List sortedMonthList = new ArrayList();
            sortedMonthList.addAll(monthMap.keySet());
            Collections.sort(sortedMonthList);
            Collections.reverse(sortedMonthList);
            final Iterator monthIter = sortedMonthList.iterator();
            while (monthIter.hasNext())
            {
               final Integer month = (Integer) monthIter.next();
               log("Rendering detail table for month " + month);

               pw.println("<table class=\"month\">");
               pw.println("<tr><td>"
                     + MONTHS[month.intValue() - 1]
                     + " " + year +  "</td></tr>");
               pw.println("<tr class=\"daytable\">");

               int i = 0;
               final Map dayMap = (Map) monthMap.get(month);
               final List sortedDayList = new ArrayList();
               sortedDayList.addAll(dayMap.keySet());
               Collections.sort(sortedDayList);
               final Iterator dayIter = sortedDayList.iterator();
               int counter = 0;
               long errors = 0;
               long warnings = 0;
               long info = 0;
               long coverage = 0;
               long startErrors = 0;
               long startWarnings = 0;
               long startInfo = 0;
               long startCoverage = 0;
               long endErrors = 0;
               long endWarnings = 0;
               long endInfo = 0;
               long endCoverage = 0;
               while (dayIter.hasNext())
               {
                  final Integer day = (Integer) dayIter.next();
                  final List reportsList = (List) dayMap.get(day);

                  final Iterator reportsIter = reportsList.iterator();
                  while (reportsIter.hasNext())
                  {
                     final Summary sum = (Summary) reportsIter.next();
                     if ((i & 1) == 0)
                     {
                        pw.print("<td class=\"day_even\">");
                     }
                     else
                     {
                        pw.print("<td class=\"day_odd\">");
                     }
                     pw.print("<div class=\"datelink\">");
                     if (sum.getSummaryXml() != null)
                     {
                        pw.print("<a href=\"" + sum.getTimestamp()
                              + "/index.html\">");
                     }
                     pw.print(extractDay(sum.getTimestamp()) + "."
                           + month + ".<br/>"
                           + extractTime(sum.getTimestamp()));
                     if (sum.getSummaryXml() != null)
                     {
                        pw.print("</a>");
                     }
                     pw.print("</div><hr/>");
                     pw.print("<div class=\"error\">" + sum.getError()
                           + "</div>");
                     pw.print("<div class=\"warning\">" + sum.getWarning()
                           + "</div>");
                     pw.print("<div class=\"info\">" + sum.getInfo()
                           + "</div>");
                     pw.print("<div class=\"coverage\">" + sum.getCoverage()
                           + "</div></td>");
                     i++;
                     if (startErrors == 0)
                     {
                        startErrors = sum.getError();
                     }
                     if (startWarnings == 0)
                     {
                        startWarnings = sum.getWarning();
                     }
                     if (startInfo == 0)
                     {
                        startInfo = sum.getInfo();
                     }
                     if (startCoverage == 0)
                     {
                        startCoverage = sum.getCoverage();
                     }
                     endErrors = sum.getError();
                     endWarnings = sum.getWarning();
                     endInfo = sum.getInfo();
                     endCoverage = sum.getCoverage();
                     errors += sum.getError();
                     warnings += sum.getWarning();
                     info += sum.getInfo();
                     coverage += sum.getCoverage();
                     counter++;
                  }
               }
               pw.println("</tr>");
               pw.println("<tr><td style=\"vertical-align:top;\">");
               int errorsAvg = (int) errors / counter;
               int warningsAvg = (int) warnings / counter;
               int infoAvg = (int) info / counter;
               int coverageAvg = (int) coverage / counter;
               pw.println("<b>Average</b>"
                     + "<div class=\"error\">Errors: " + errorsAvg
                     + "</div><div class=\"warning\">Warnings: " + warningsAvg
                     + "</div><div class=\"info\">Info: " + infoAvg
                     + "</div><div class=\"coverage\">Coverage: " + coverageAvg + "</div>");
               pw.println("</td>");
               pw.println("<td style=\"vertical-align:top;\">");
               
               long deltaErrors = endErrors - startErrors;
               long deltaWarnings = endWarnings - startWarnings;
               long deltaInfo = endInfo - startInfo;
               long deltaCoverage = endCoverage - startCoverage;
               pw.println("<b>Tendency</b>"
                  + "<div class=\"error\">Errors: " + (deltaErrors >= 0 ? "+" : "-") + deltaErrors
                  + "</div><div class=\"warning\">Warnings: " + (deltaWarnings >= 0 ? "+" : "-") + deltaWarnings
                  + "</div><div class=\"info\">Info: " +  (deltaInfo >= 0 ? "+" : "-") + deltaInfo
                  + "</div><div class=\"coverage\">Coverage: " +  (deltaCoverage >= 0 ? "+" : "-") + deltaCoverage + "</div>");

               pw.println("</td>");
               pw.println("</tr>");
               pw.println("</table>");
            }
         }

         pw.println("</td></tr></table>");
         pw.println("</body>");
         pw.println("</html>");
         pw.close();
         fos.flush();
         fos.close();
      }
      catch (IOException ex)
      {
         throw new BuildException("An IO exception occured!", ex);
      }
   }

   
   private String extractDay (long timestamp)
   {
      final String ts = "" + timestamp;
      return ts.substring(YEAR_LEN + MONTH_LEN, YEAR_LEN + MONTH_LEN + DAY_LEN);
   }


   private String extractTime (long timestamp)
   {
      final String ts = "" + timestamp;
      final int start_pos = YEAR_LEN + MONTH_LEN + DAY_LEN;
      return ts.substring(start_pos, start_pos + HOUR_LEN) + ":"
            + ts.substring(start_pos + HOUR_LEN, start_pos + HOUR_LEN
               + MINUTE_LEN);
   }


   /**
    * Class holds data from the summary database file.
    *
    * @author Michael Rumpf
    */
   private static final class Summary
   {
      private final long mTimestamp;
      private final int mError;
      private final int mWarning;
      private final int mInfo;
      private final int mCoverage;
      private final int mLoc;
      /** Lines of code reported by coverage test. */
      private final int mCodeLoc;
      private final double mQuality;
      private final File mSummaryXml;

      public Summary (long timestamp, int error, int warning, int info,
            int coverage, int loc, int codeloc, double quality)
      {
         mTimestamp = timestamp;
         mError = error;
         mWarning = warning;
         mInfo = info;
         mCoverage = coverage;
         mLoc = loc;
         mCodeLoc = codeloc;
         mQuality = quality;
         mSummaryXml = null;
      }

      public Summary (long timestamp, int error, int warning, int info,
            int coverage, int loc, int codeloc, double quality, File summaryXml)
      {
         mTimestamp = timestamp;
         mError = error;
         mWarning = warning;
         mInfo = info;
         mCoverage = coverage;
         mLoc = loc;
         mCodeLoc = codeloc;
         mQuality = quality;
         mSummaryXml = summaryXml;
      }

      public long getTimestamp ()
      {
         return mTimestamp;
      }

      public int getError ()
      {
         return mError;
      }

      public int getWarning ()
      {
         return mWarning;
      }

      public int getInfo ()
      {
         return mInfo;
      }

      public int getCoverage ()
      {
         return mCoverage;
      }

      public int getLoc ()
      {
         return mLoc;
      }

      public int getCodeLoc ()
      {
         return mCodeLoc;
      }

      public double getQuality ()
      {
         return mQuality;
      }

      public File getSummaryXml ()
      {
         return mSummaryXml;
      }

      public int hashCode ()
      {
         // MAXINT = 4294967296
         // ts 20061122001122
         // value to extract mmddHHMMSS from YYYY
         final long div = 10000000000L;
         final int mul = 1000000;
         // max 1231235959
         final long rest = mTimestamp % div;
         // year 2006
         final long year = mTimestamp / div;
         // hash 2006000000 + 1231235959 = 3237235959 (Overflow in year 3063)
         return (int) (year * mul + rest);
      }

      public boolean equals (Object sum)
      {
         boolean result = false;
         if (sum != null)
         {
            if (sum == this)
            {
               result = true;
            }
            else
            {
               result = hashCode() == sum.hashCode();
            }
         }
         return result;
      }

      public String toString ()
      {
         return "[" + mTimestamp + ", " + mError + ", " + mWarning + ", "
               + mInfo + ", " + mCoverage + ", " + mLoc + ", " + mCodeLoc
               + ", " + mQuality + ", " + mSummaryXml + "]";
      }
   }

   
   /**
    * Filter for folders that contain a 'summary.xml' file.
    *
    * @author Michael Rumpf
    */
   public static class JcoderReportFilter
      implements FilenameFilter
   {
      /**
       * Only accept folder that contain a 'summary.xml' file.
       *
       * @param dir the directory where the file is located.
       * @param name the name of the file.
       */
      public boolean accept (File dir, String name)
      {
         boolean result = false;
         final File folder = new File(dir, name);
         if (folder.exists() && folder.isDirectory())
         {
            final File summaryXml = new File(folder, "summary.xml");
            result = summaryXml.exists() && summaryXml.isFile();
         }
         
         return result;
      }   
   }

   
   //
   // test-code
   //

   public void log (String msg)
   {
      boolean stdout = false;
      try
      {
        super.log(msg);
      }
      catch (NullPointerException ex)
      {
         stdout = true;
      }
      if (stdout)
      {
         System.out.println(msg);
      }
   }

   public static void main (String[] args)
   {
      final JcSummaryReportAntTask jcSum = new JcSummaryReportAntTask();
      jcSum.setName("fawkez-summary");
      jcSum.setDest("c:/temp/findings-report/xxx");
      jcSum.setSummary("c:/temp/findings-report/history.csv");  
      jcSum.setBaseDir("c:/temp/findings-report/");
      jcSum.execute();
   }

}
