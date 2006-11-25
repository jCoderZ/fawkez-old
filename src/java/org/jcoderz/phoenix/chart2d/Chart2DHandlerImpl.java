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
package org.jcoderz.phoenix.chart2d;


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.chart2d.Chart2D;
import net.sourceforge.chart2d.Chart2DProperties;
import net.sourceforge.chart2d.Dataset;
import net.sourceforge.chart2d.GraphChart2D;
import net.sourceforge.chart2d.GraphChart2DProperties;
import net.sourceforge.chart2d.GraphProperties;
import net.sourceforge.chart2d.LBChart2D;
import net.sourceforge.chart2d.LLChart2D;
import net.sourceforge.chart2d.LegendProperties;
import net.sourceforge.chart2d.MultiColorsProperties;
import net.sourceforge.chart2d.Object2DProperties;
import net.sourceforge.chart2d.PieChart2D;
import net.sourceforge.chart2d.PieChart2DProperties;
import net.sourceforge.chart2d.WarningRegionProperties;

import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.jcoderz.commons.types.Date;
import org.jcoderz.commons.util.IoUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Generated implementation for documents valid against
 * the chart2d dtd.
 * 
 * Also give the ability to collect input data from a certain set of 
 * xml files.
 * The format is currently quite tailored to the needs of a cruise 
 * control log file analysis. This might be opened up if further
 * requirements come up.
 * 
 *  The format is given by the chart2d api. Look there 
 *  http://chart2d.sourceforge.net/ for details.
 *  
 *  The special data collection is always started with a ! do 
 *  mark the special format. 
 *  
 *  For the <code>AxisLabelText</code> there are 2 additional 
 *  attributes:
 *   
 *  <code>count</code>: The number of labels on the x-Axis. 
 *  If the dataset contains <code>count * 2</code> or more 
 *  entries, n (= <i>number of datasets</i> / count) are put
 *  together in one label. 
 *    
 *  <code>max</code>: The maximum number of datasets that are
 *  allowed to be put together under one label. If the number of 
 *  available datasets is more than <code>count * max</code> the
 *  elder datasets are discarded.
 *  
 *  The element of <code>AxisLabelText</code> can contain a regular 
 *  expression, that is used to search for the logfiles used as 
 *  input datasets. The search is started recursive from the 
 *  given <i>context path</i>. Path separators must be given
 *  as '/' characters if needed. The first group on the regular
 *  expression is used as label for the dataset. An example could
 *  be <code>!.*log.*BUILD_([0-9]*)\.xml</code> with would use the 
 *  number after the <code>BUILD_</code> as labeltext.
 *  
 *  The element of <code>LegendLabelsTexts</code> is the z-Axis.
 *  If the element starts with a <code>!</code> character the
 *  rest must be a xpath expression returning a node set. Each
 *  node of the nodeset will build a element (and label) 
 *  on the z-Axis. A valid expression could be:
 *  <code>!/cruisecontrol/findingsummary/servicelevelxml/service/@name</code>. 
 *  
 *  The element of <code>Data</code> could also be an xpath
 *  expression. The handler then iterates over this for each 
 *  file (dataset) and each value available for the z-Axis. The string
 *  $z is replaced by the current z-Axis value before the xpath 
 *  expression is evaluated against the current file (dataset). The
 *  handler takes care for the <code>Set</code>, <code>Category</code>,
 *  <code>Data</code> element nesting. A valid expression could be:
 *  <code>!/cruisecontrol/findingsummary/servicelevelxml/service[@name = '$z']/@quality</code>.
 *  The expression should evaluate to a node that can be parsed as a 
 *  float using <code>Float.parseFloat</code> or to a ant timestring 
 *  matching the regular pattern 
 *  <code>"(([0-9]+) minutes? )?([0-9]+) seconds?"</code> the timestring 
 *  will be converted into the number of seconds. Other formats might be
 *  added.
 * 
 * @author Andreas Mandel
 * @author Netbeans Code generator.
 */
public class Chart2DHandlerImpl implements Chart2DHandler
{
   private static final int DEFAULT_WIDTH = 768;
   private static final int DEFAULT_HEIGHT = 1024;
   private static final int DEBUG_ARG_NUM = 3;

   private static final String TIME_PATTERN 
       = "(([0-9]+) minutes? )?([0-9]+) seconds?";
   private static final int TIME_PATTERN_MINUTES_GROUP = 2;
   private static final int TIME_PATTERN_SECONDS_GROUP = 3;
   private static boolean sDebug = false;
   private static boolean sMultible = false;
   private Object2DProperties mObject2DProperties;
   private Chart2DProperties mChart2DProperties;
   private GraphChart2DProperties mGraphChart2DProperties;
   private List mCurrentLabelText;
   private PieChart2DProperties mPieChart2DProperties;
   private LegendProperties mLegendProperties;
   private final List mDataSets = new ArrayList();
   private final List mMultiColorsProperties = new ArrayList();
   private final List mGraphProperties = new ArrayList();
   private final List mWarningRegionProperties = new ArrayList();
   private int mWidth = DEFAULT_WIDTH;
   private int mHeight = DEFAULT_HEIGHT;
   private String mFilename;
   private String mType;
   private List mCurrentDataSet;
   private List mCurrentSet;
   private List mCurrentCategory;
   private MultiColorsProperties mCurrentMultiColorsProperties;
   private List mCurrentMultiColors;
   private String mChartType;
   private List mCurrentLegendLabelsTexts;
   private boolean mDoConvertToStacked;
   private final File mDataRepository;
   private final Set mFilePatterns = new TreeSet();
   private final List mXaxis = new ArrayList();
   private int mXaxisModulus = 1;
   private final Map mXpathCache = new HashMap();
   private DocumentBuilder mDocumentBuilder; 

   /**
    * Commandline interface to this handler.
    * Parameters should be 
    * <input file or dir> [context path] [debug]
    * 
    * @param args args the commandline aruments.
    * @throws IOException if a io problem occures.
    */
   public static void main (String[] args) throws IOException
   {
      File[] in;
      File para;

      if ((args.length < 1) || (args.length > DEBUG_ARG_NUM))
      {
         String msg;

         msg = "Usage: java " + Chart2DHandlerImpl.class.getName()
               + " <input file or directory> [context path] [debug]";
         System.out.println(msg);
         throw new IllegalArgumentException(msg);
      }

      if (args.length == DEBUG_ARG_NUM)
      {
         sDebug = true;
      }
      
      File dataRepository = null;
      
      if (args.length > 1)
      {
          if (args[1].equals("debug"))
          {
              sDebug = true;
          }
          else
          {
              dataRepository = new File(args[1]);
          }
      }
      
      para = new File(args[0]);
      if (para.isFile())
      {
         in = new File[] { para };
      }
      else
      {
         final FilenameFilter filter = new FilenameFilter()
         {
            public boolean accept (File dir, String name)
            {
               return name.endsWith(".xml");
            }
         };

         in = para.listFiles(filter);
      }

      sMultible = in.length > 1;

      final Iterator i = Arrays.asList(in).iterator();

      while (i.hasNext())
      {
         final File current = (File) i.next();

         System.out.println("in: " + current.getAbsolutePath());
         try
         {
             final Chart2DHandlerImpl handler 
                 = new Chart2DHandlerImpl(dataRepository); 
             Chart2DParser.parse(new InputSource(new FileInputStream(current)),
                 handler);
            handler.writeCache();
         }
         catch (Exception ex)
         {
            System.out.println("Got exception " + ex);
            ex.printStackTrace(System.out);
         }
      }

      System.out.println("Done.");
   }

   /**
    * A new handler for cart2d files. 
    * The repository directory is used to read input xml files and to
    * read and store a cache file.
    * 
    * @param dataRepository the directory to the logfiles, 
    *           can be null.
    */
   public Chart2DHandlerImpl (File dataRepository)
   {
       mDataRepository = dataRepository;
       if (mDataRepository != null)
       {
           collectPatterns(mDataRepository, null);
           if (mFilePatterns.isEmpty())
           {
               throw new RuntimeException("No files found in repository path "
                       + mDataRepository + ".");
           }
           readCache();
       }
   }

   public void handleGraphLabelsLinesStyle (final Attributes meta)
         throws SAXException
   {
      if (sDebug)
      {
         System.err.println("handle_GraphLabelsLinesStyle: " + meta);
      }
   }

   /**
    * Handles opening of Category a new ArrayList is created to 
    * collect inner Data elements.
    * @param meta the attributes that come with the element.
    */
   public void startCategory (final Attributes meta) throws SAXException
   {
      if (sDebug)
      {
          if (mCurrentSet.size() < mCurrentLabelText.size())
          {
              System.err.println("startCategory x = " 
                      + mCurrentSet.size() + "/"
                      + mCurrentLabelText.get(mCurrentSet.size()));
          }
      }

      mCurrentCategory = new ArrayList();
   }

   /**
    * Handles the closing of Category. 
    * The Category data, containing the collected Data elements 
    * is added to the corrent Set. 
    * @param meta the attributes that come with the element.
    */
   public void endCategory () throws SAXException
   {
      if (sDebug)
      {
          if (mCurrentSet.size() < mCurrentLabelText.size())
          {
              System.err.println("endCategory x = " 
                      + mCurrentSet.size() + "/"
                      + mCurrentLabelText.get(mCurrentSet.size()));
          }
      }

      mCurrentSet.add(mCurrentCategory);
      mCurrentCategory = null; 
   }

   /**
    * Handles opening of MultiColorsProperties.
    * @param meta the attributes that come with the element.
    */
   public void startMultiColorsProperties (final Attributes meta)
         throws SAXException
   {
      if (sDebug)
      {
         System.err.println("startMultiColorsProperties: " + meta);
      }

      final MultiColorsProperties props = new MultiColorsProperties();

      props.setMultiColorsPropertiesToDefaults();

      setProperties(props, meta);

      mCurrentMultiColorsProperties = props;
      mCurrentMultiColors = new ArrayList();
   }

   public void endMultiColorsProperties () throws SAXException
   {
      if (sDebug)
      {
         System.err.println("endMultiColorsProperties()");
      }

      if (!mCurrentMultiColors.isEmpty())
      {
         mCurrentMultiColorsProperties
               .setColorsCustom((Color[]) mCurrentMultiColors
                     .toArray(new Color[] {}));
      }

      mMultiColorsProperties.add(mCurrentMultiColorsProperties);
   }

   /**
    * Handles opening of LBChart2D and registeres the chart type 
    * accordingly.
    * @param meta the attributes that come with the element.
    */
   public void startLBChart2D (final Attributes meta) throws SAXException
   {
      if (sDebug)
      {
         System.err.println("startLBChart2D: " + meta);
      }

      mChartType = "LBChart2D";
   }

   public void endLBChart2D () throws SAXException
   {
      if (sDebug)
      {
         System.err.println("endLBChart2D()");
      }
   }

   public void handleLegendLabelsTexts (final java.lang.String data,
         final Attributes meta) throws SAXException
   {
      if (sDebug)
      {
         System.err.println("handle_LegendLabelsTexts: " + data);
      }

      if (data.startsWith("!"))
      {
          try
          {
              final DocumentBuilder builder = getDocumentBuilder();
              final Document document = builder.parse(
                  new File(mDataRepository, 
                      ((Pair) mXaxis.get(mXaxis.size() - 1)).getFileName()));
              final NodeIterator i 
                  = XPathAPI.selectNodeIterator(
                      document, data.substring(1));
              Node node = i.nextNode();
              while (node != null)
              {
                  if (sDebug)
                  {
                      System.out.println("Legend Label: " 
                              + node.getNodeValue());
                  }
                  mCurrentLegendLabelsTexts.add(node.getNodeValue());
                  node = i.nextNode();
              }
          }
          catch (Exception ex)
          {
              final SAXException e = new SAXException(ex);
              e.initCause(ex);
              throw e;
          }
      }
      else
      {
          mCurrentLegendLabelsTexts.add(data);
      }
   }

   public void startDataset (final Attributes meta) throws SAXException
   {
      if (sDebug)
      {
         System.err.println("startDataset: " + meta);
      }
      mDoConvertToStacked = meta.getIndex("DoConvertToStacked") != -1;
      mCurrentDataSet = new ArrayList();
   }

   public void endDataset () throws SAXException
   {
      if (sDebug)
      {
         System.err.println("endDataset()");
      }

      final int zSize 
          = ((List) ((List) mCurrentDataSet.get(0)).get(0)).size();
      final int ySize 
          = ((List) mCurrentDataSet.get(0)).size();
      
      final Dataset dataset = new Dataset(mCurrentDataSet.size(), ySize, zSize);

      if (!sMultible)
      {
         System.out.println("Dimension: [" + mCurrentDataSet.size() + "]["
               + ySize + "][" + zSize + "]");
      }

      Iterator i;
      Iterator j;
      Iterator k;
      int x;
      int y;
      int z;

      x = 0;
      i = mCurrentDataSet.iterator();
      while (i.hasNext())
      {
         y = 0;
         j = ((List) i.next()).iterator();
         while (j.hasNext())
         {
            z = 0;
            k = ((List) j.next()).iterator();
            while (k.hasNext())
            {
               final float f = ((Float) k.next()).floatValue();

               if (!sMultible)
               {
                  System.out.println("point[" 
                          + x + "/" + mCurrentLegendLabelsTexts.get(x) + "][" 
                          + y + "/" + mCurrentLabelText.get(y) + "][" + z
                        + "] = " + f);
               }

               dataset.set(x, y, z, f);
               z++;
            }
            y++;
         }
         x++;
      }
      if (mDoConvertToStacked)
      {
         dataset.doConvertToStacked();
      }

      mDataSets.add(dataset);
      mCurrentDataSet = null;
   }

   public void handlePieChart2DProperties (final Attributes meta)
         throws SAXException
   {
      if (sDebug)
      {
         System.err.println("handle_PieChart2DProperties: " + meta);
      }

      mPieChart2DProperties = new PieChart2DProperties();

      mPieChart2DProperties.setPieChart2DPropertiesToDefaults();

      setProperties(mPieChart2DProperties, meta);
   }

   public void startGraphChart2DProperties (final Attributes meta)
         throws SAXException
   {
      if (sDebug)
      {
         System.err.println("startGraphChart2DProperties: " + meta);
      }

      mGraphChart2DProperties = new GraphChart2DProperties();
      mGraphChart2DProperties.setGraphChart2DPropertiesToDefaults();
      setProperties(mGraphChart2DProperties, meta);
      mCurrentLabelText = new ArrayList();
   }

   public void endGraphChart2DProperties () throws SAXException
   {
      if (sDebug)
      {
         System.err.println("endGraphChart2DProperties()");
      }

      if (!mCurrentLabelText.isEmpty())
      {
         mGraphChart2DProperties
               .setLabelsAxisLabelsTexts((String[]) mCurrentLabelText
                     .toArray(new String[] {}));
      }
   }

   public void handleAxisLabelText (final java.lang.String data,
         final Attributes meta) throws SAXException
   {
      if (sDebug)
      {
         System.err.println("handle_AxisLabelText: " + data);
      }
      if (data.startsWith("!"))
      {
          // Maximum number of builds aggregated in one label
          final int max = Integer.parseInt(meta.getValue("max"));
          // number of labels
          final int count = Integer.parseInt(meta.getValue("count"));
          
          Collection map = fileMatcher(data.substring(1));
          
          int size = map.size();
          if (size == 0)
          {
              throw new RuntimeException("No files found for pattern "
                      + data + " in " + mDataRepository + ".");
          }
          
          if (size > max * count)
          {
              final List newList = new ArrayList(max * count);
              final Iterator it = map.iterator();
              int remove = size - (max * count);
              while (remove > 0)
              {
                  it.next();
                  remove--;
              }
              while (it.hasNext())
              {
                  newList.add(it.next());
              }
              map = newList;
              size = map.size();
          }
          
          mXaxisModulus = Math.max(1, size / count);
          if (sDebug)
          {
              System.err.println("Will have " + mXaxisModulus 
                      + " enties per category.");
          }
          
          
          mXaxis.addAll(map);
          final Iterator i = map.iterator();
          int xPos = 0;
          while (i.hasNext())
          {
              final String label = ((Pair) i.next()).getLabel();
              if (xPos % mXaxisModulus == 0)
              {
                  if (sDebug)
                  {
                      System.out.println("X-Label: " + label);
                  }
                  mCurrentLabelText.add(label);
              }
              xPos++;
          }
      }
      else
      {
          mCurrentLabelText.add(data);
      }
   }

    private List fileMatcher (String filePattern)
    {
        final List result = new ArrayList();
           final Pattern pattern = Pattern.compile(filePattern);
           final Iterator i = mFilePatterns.iterator();
           while (i.hasNext())
           {
               final String fileName = (String) i.next();
               final Matcher m = pattern.matcher(fileName);
               if (m.matches())
               {
                  result.add(new Pair(fileName, m.group(1))); 
               }
           }
        return result;
    }

    public void startChart2D (final Attributes meta) throws SAXException
   {
      if (sDebug)
      {
         System.err.println("startChart2D: " + meta);
      }

      String value;

      value = meta.getValue("Width");
      if (value != null)
      {
         mWidth = Integer.decode(value).intValue();
      }

      value = meta.getValue("Height");
      if (value != null)
      {
         mHeight = Integer.decode(value).intValue();
      }

      value = meta.getValue("Type");
      if (value != null)
      {
         mType = value;
      }

      value = meta.getValue("Filename");
      if (value != null)
      {
         mFilename = value;
      }
   }

   public void endChart2D () throws SAXException
   {
      if (sDebug)
      {
         System.err.println("endChart2D()");
      }

      try
      {
         Chart2D chart2d = null;

         if (mChartType.equals("PieChart2D"))
         {
            final PieChart2D chart = new PieChart2D();

            chart.setObject2DProperties(mObject2DProperties);
            chart.setChart2DProperties(mChart2DProperties);
            chart.setPieChart2DProperties(mPieChart2DProperties);
            chart.setLegendProperties(mLegendProperties);
            chart.setDataset((Dataset) mDataSets.get(0));
            chart.setMultiColorsProperties(
                  (MultiColorsProperties) mMultiColorsProperties.get(0));
            chart2d = chart;
         }
         else if (mChartType.equals("LLChart2D"))
         {
            final LLChart2D chart = new LLChart2D();
            fillChartData(chart);
            chart2d = chart;
         }
         else if (mChartType.equals("LBChart2D"))
         {
            final LBChart2D chart = new LBChart2D();
            fillChartData(chart);
            chart2d = chart;
         }

         final Dimension size = new Dimension(mWidth, mHeight);

         chart2d.setMaximumSize(size);
         chart2d.setPreferredSize(size);

         if (chart2d.validate(false))
         {
            javax.imageio.ImageIO.write(chart2d.getImage(), mType, new File(
                  mFilename));
            System.out.println("out: " + new File(mFilename).getAbsolutePath());
         }
         else
         {
            chart2d.validate(true);
         }
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         throw new SAXException(ex);
      }
   }

   public void startLLChart2D (final Attributes meta) throws SAXException
   {
      if (sDebug)
      {
         System.err.println("startLLChart2D: " + meta);
      }

      mChartType = "LLChart2D";
   }

   public void endLLChart2D () throws SAXException
   {
      if (sDebug)
      {
         System.err.println("endLLChart2D()");
      }
   }

   public void handleGraphNumbersLinesStyle (final Attributes meta)
         throws SAXException
   {
      if (sDebug)
      {
         System.err.println("handle_GraphNumbersLinesStyle: " + meta);
      }
   }

   public void handleData (final java.lang.String data, final Attributes meta)
         throws SAXException
   {
      if (sDebug)
      {
         System.err.println("handle_Data: " + data);
      }

      if (data.startsWith("!"))
      {
          // LOOPIT
          final Iterator z = mCurrentLegendLabelsTexts.iterator();
          while (z.hasNext())
          {
              final String zValue = (String) z.next();
              
              int xPos = 0;
              final Iterator x = mXaxis.iterator();
              while (x.hasNext())
              {
                  final Pair pair = (Pair) x.next();
                  final String fileName = pair.getFileName();
                  
                  String query = data.substring(1);
                  query = query.replaceAll("\\$z", zValue);
                  
                  String value = resolveXpath(fileName, query);
                  if (value.length() == 0)
                  {
                      value = "0";
                  }
                  else if (value.indexOf("second") != -1)
                  {
                      value = parseAntTime(value);
                  }
                  mCurrentCategory.add(new Float(Float.parseFloat(value)));
                  
                  xPos++;
                  while (xPos % mXaxisModulus != 0 && !x.hasNext())
                  {
                      xPos++;
                      // continue chart with last value. 
                      mCurrentCategory.add(new Float(Float.parseFloat(value)));
                  }
                  if ((xPos % mXaxisModulus == 0 && x.hasNext())
                          || (!x.hasNext() && z.hasNext()))
                  {
                      endCategory();
                      // it is save to call startCategory without a entry...
                      startCategory(null);
                  }
              }
                  
              
              if (z.hasNext())
              {
                  endSet();
                  startSet(null);
                  startCategory(null);
              }
          }
      }
      else
      {
          mCurrentCategory.add(new Float(Float.parseFloat(data)));
      }
   }

    private String parseAntTime (String value)
    {
        final Pattern pat = Pattern.compile(TIME_PATTERN);
        final Matcher mat = pat.matcher(value);
        System.out.println("grops " + value);
        if (mat.matches())
        {
            int minutes = 0;
            int seconds = 0;
            if (mat.group(TIME_PATTERN_MINUTES_GROUP) != null)
            {
                minutes = Integer.parseInt(
                        mat.group(TIME_PATTERN_MINUTES_GROUP));
            }
            if (mat.group(TIME_PATTERN_SECONDS_GROUP) != null)
            {
                seconds = Integer.parseInt(
                    mat.group(TIME_PATTERN_SECONDS_GROUP));
            }
            value = String.valueOf(
                    Date.SECONDS_PER_MINUTE * minutes + seconds);
        }
        return value;
    }

   private String resolveXpath (String fileName, String xpath) 
           throws SAXException
   {
       if (sDebug)
       {
           System.out.print(xpath + " in "  + fileName);
       }
       String result = null;
       final String cacheKey = xpath + "@" + fileName;
       result = (String) mXpathCache.get(cacheKey);
       if (result == null)
       {
           try
           {
               final DocumentBuilder builder = getDocumentBuilder();
               final Document document = builder.parse(new File(mDataRepository,
                       fileName));
               final XObject object = XPathAPI.eval(document, xpath);
               result = object.str();
           }
           catch (Exception ex)
           {
               final SAXException e = new SAXException(ex);
               e.initCause(ex);
               throw e;
           }
           if (sDebug)
           {
               System.out.println(" = " + result);
           }
           mXpathCache.put(cacheKey, result);
       }
       else
       {
           if (sDebug)
           {
               System.out.println(" = " + result + " (cached)");
           }
       }
       return result;
   }

   public void startPieChart2D (final Attributes meta) throws SAXException
   {
      if (sDebug)
      {
         System.err.println("startPieChart2D: " + meta);
      }

      mChartType = "PieChart2D";
   }

   public void endPieChart2D () throws SAXException
   {
      if (sDebug)
      {
         System.err.println("endPieChart2D()");
      }
   }

   public void handleObject2DProperties (final Attributes meta)
         throws SAXException
   {
      if (sDebug)
      {
         System.err.println("handle_Object2DProperties: " + meta);
      }

      mObject2DProperties = new Object2DProperties();
      mObject2DProperties.setObject2DPropertiesToDefaults();
      setProperties(mObject2DProperties, meta);
   }

   public void handleWarningRegionProperties (final Attributes meta)
         throws SAXException
   {
      if (sDebug)
      {
         System.err.println("handle_WarningRegionProperties: " + meta);
      }

      final WarningRegionProperties props = new WarningRegionProperties();

      props.setToDefaults();
      setProperties(props, meta);
   }

   public void handleChart2DProperties (final Attributes meta)
         throws SAXException
   {
      if (sDebug)
      {
         System.err.println("handle_Chart2DProperties: " + meta);
      }

      mChart2DProperties = new Chart2DProperties();
      mChart2DProperties.setChart2DPropertiesToDefaults();
      setProperties(mChart2DProperties, meta);
   }

   public void startSet (final Attributes meta) throws SAXException
   {
      if (sDebug)
      {
          if (mCurrentDataSet.size() < mCurrentLegendLabelsTexts.size())
          {
              System.err.println("startSet z = " + mCurrentDataSet.size() + "/"
                      + mCurrentLegendLabelsTexts.get(mCurrentDataSet.size()));
          }
      }

      mCurrentSet = new ArrayList();
   }

   public void endSet () throws SAXException
   {
      if (sDebug)
      {
          if (mCurrentDataSet.size() < mCurrentLegendLabelsTexts.size())
          {
              System.err.println("endSet z = " + mCurrentDataSet.size() + "/"
                      + mCurrentLegendLabelsTexts.get(mCurrentDataSet.size()));
          }
      }

      mCurrentDataSet.add(mCurrentSet);
   }

   public void startGraphProperties (final Attributes meta) throws SAXException
   {
      if (sDebug)
      {
         System.err.println("startGraphProperties: " + meta);
      }

      final GraphProperties props = new GraphProperties();

      props.setGraphPropertiesToDefaults();
      setProperties(props, meta);
      mGraphProperties.add(props);
   }

   public void endGraphProperties () throws SAXException
   {
      if (sDebug)
      {
         System.err.println("endGraphProperties()");
      }
   }

   public void handleColorsCustom (final java.lang.String data,
         final Attributes meta) throws SAXException
   {
      if (sDebug)
      {
         System.err.println("handle_ColorsCustom: " + data);
      }

      mCurrentMultiColors.add(getColor(data));
   }

   public void startLegendProperties (final Attributes meta)
         throws SAXException
   {
      if (sDebug)
      {
         System.err.println("startLegendProperties: " + meta);
      }

      mLegendProperties = new LegendProperties();
      mLegendProperties.setLegendPropertiesToDefaults();
      mCurrentLegendLabelsTexts = new ArrayList();
   }

   public void endLegendProperties () throws SAXException
   {
      if (sDebug)
      {
         System.err.println("endLegendProperties()");
      }

      if (!mCurrentLegendLabelsTexts.isEmpty())
      {
         mLegendProperties
               .setLegendLabelsTexts((String[]) mCurrentLegendLabelsTexts
                  .toArray(new String[] {}));
      }
   }

   public void setProperties (Object props, final Attributes meta)
   {
      int i = meta.getLength() - 1;

      for (; i >= 0; i--)
      {
         final String key = meta.getQName(i);
         final String value = meta.getValue(i);
         Method m;
         final String setterName = "set" + key;

         try
         {
            m = props.getClass().getDeclaredMethod(setterName,
                  new Class[] {String.class});

            m.invoke(props, new Object[] { value });
         }
         catch (Exception ex)
         {
            m = null;
         }

         if (m == null)
         {
            try
            {
               m = props.getClass().getDeclaredMethod(setterName,
                     new Class[] {Boolean.TYPE});

               m.invoke(props, new Object[] {Boolean.valueOf(value)});
            }
            catch (Exception ex)
            {
               m = null;
            }
         }

         if (m == null)
         {
            try
            {
               m = props.getClass().getDeclaredMethod(setterName,
                     new Class[] {Integer.TYPE});

               m.invoke(props, new Object[] {getInteger(value, props)});
            }
            catch (Exception ex)
            {
               m = null;
            }
         }

         if (m == null)
         {
            try
            {
               m = props.getClass().getDeclaredMethod(setterName,
                     new Class[] {Color.class});

               m.invoke(props, new Object[] {getColor(value)});
            }
            catch (Exception ex)
            {
               m = null;
            }
         }

         if (m == null)
         {
            try
            {
               m = props.getClass().getDeclaredMethod(setterName,
                     new Class[] {Float.TYPE});

               m.invoke(props, new Object[] {Float.valueOf(value)});
            }
            catch (Exception ex)
            {
               m = null;
            }
         }

         if (m == null)
         {
            try
            {
               m = props.getClass().getDeclaredMethod(setterName,
                     new Class[] {Dimension.class});

               m.invoke(props, new Object[] {getDimension(value)});
            }
            catch (Exception ex)
            {
               m = null;
            }
         }

         if (m == null)
         {
            try
            {
               m = props.getClass().getDeclaredMethod(setterName,
                     new Class[] {java.awt.AlphaComposite.class});

               m.invoke(props, new Object[] {getAlphaComposite(value, props)});
            }
            catch (Exception ex)
            {
               m = null;
            }
         }

         if (m == null)
         {
            System.out.println("Could not set " + props.getClass().getName()
                  + "." + setterName + " = " + value);
         }
      }
   }

   public Color getColor (String col)
   {
      Color c = null;

      try
      {
         try
         {
            c = Color.decode(col);
         }
         catch (Exception ex)
         {
            final Field f = Color.class.getField(col);
            c = (Color) f.get(null);
         }
      }
      catch (Exception ex)
      {
      }

      return c;
   }

   public Dimension getDimension (String val)
   {
      double w;
      double h;
      final Dimension d = new Dimension();

      try
      {
         w = Double.parseDouble(val.substring(0, val.indexOf('x')));
         h = Double.parseDouble(val.substring(1 + val.indexOf('x')));

         d.setSize(w, h);
      }
      catch (RuntimeException ex)
      {
         ex.printStackTrace();
         throw ex;
      }

      return d;
   }

   public Integer getInteger (String value, Object properties) throws Exception
   {
      int i;

      try
      {
         i = Integer.decode(value).intValue();
      }
      catch (Exception ex)
      {
         try
         {
            final Field f = properties.getClass().getField(value);

            i = f.getInt(null);
         }
         catch (Exception exx)
         {
            final Field f = java.awt.Font.class.getField(value);

            i = f.getInt(null);
         }
      }

      return new Integer(i);
   }

   public AlphaComposite getAlphaComposite (String value, Object properties)
         throws Exception
   {
      AlphaComposite alpha = null;

      try
      {
         final Field f = properties.getClass().getField(value.toUpperCase());

         alpha = (AlphaComposite) f.get(null);
      }
      catch (Exception exx)
      {
         try
         {
            final Field f = AlphaComposite.class.getField(value.toUpperCase());
            alpha = AlphaComposite.getInstance(f.getInt(null));
         }
         catch (Exception ex)
         {
            try
            {
               final Field f = AlphaComposite.class.getField(value);

               alpha = (AlphaComposite) f.get(null);
            }
            catch (Exception exxx)
            {
            }
         }
      }

      return alpha;
   }

   private void fillChartData (final GraphChart2D chart)
   {
      chart.setObject2DProperties(mObject2DProperties);
      chart.setChart2DProperties(mChart2DProperties);
      chart.setGraphChart2DProperties(mGraphChart2DProperties);
      chart.setLegendProperties(mLegendProperties);

      Iterator i = mDataSets.iterator();
      while (i.hasNext())
      {
         chart.addDataset((Dataset) i.next());
      }

      i = mMultiColorsProperties.iterator();
      while (i.hasNext())
      {
         chart.addMultiColorsProperties((MultiColorsProperties) i.next());
      }

      i = mGraphProperties.iterator();
      while (i.hasNext())
      {
         chart.addGraphProperties((GraphProperties) i.next());
      }

      i = mWarningRegionProperties.iterator();
      while (i.hasNext())
      {
         chart.addWarningRegionProperties(
               (WarningRegionProperties) i.next());
      }
   }
   
   /**
    * Reads the xpath query cache if possible.
    */
   private void readCache ()
   {
       BufferedReader reader = null;
       try
       {
           final File inFile = new File(mDataRepository, "stat-cache");
           if (inFile.canRead())
           {
               reader = new BufferedReader(new FileReader(inFile));
               String line = reader.readLine();
               while (line != null)
               {
                   if (line.length() > 0)
                   {
                       final String[] data = line.split("\t");
                       if (data.length > 1)
                       {
                           mXpathCache.put(data[0], data[1]);
                       }
                       else
                       {
                           mXpathCache.put(data[0], "");
                       }
                       line = reader.readLine();
                   }
               }
           }
       }
       catch (IOException ex)
       {
           throw new RuntimeException("Failed to read cache.", ex);
       }
       finally
       {
           IoUtil.close(reader);
       }
   }

   /**
    * Writes the xpath query cache if possible.
    */
   public void writeCache ()
   {
       if (mDataRepository != null)
       {
           BufferedWriter writer = null;
           try
           {
               final File file = new File(mDataRepository, "stat-cache");
               writer = new BufferedWriter(new FileWriter(file, false));
               final Iterator i = mXpathCache.entrySet().iterator();
               while (i.hasNext())
               {
                   final Entry entry = (Entry) i.next();
                   writer.write((String) entry.getKey());
                   writer.write('\t');
                   writer.write((String) entry.getValue());
                   writer.write('\n');
               }
           }
           catch (IOException ex)
           {
               throw new RuntimeException("Failed to write cache.", ex);
           }
           finally
           {
               IoUtil.close(writer);
           }
       }
   }
   
   /**
    * Collects files available in the given path.
    * @param dataRepository the path to look in
    * @param path the pattern representation of the path;
    */
   private void collectPatterns (File dataRepository, String path)
   {
       final String thisPath;
       if (path == null)
       {
           thisPath = "";
       }
       else
       {
           thisPath = path + "/" + dataRepository.getName();
       }
       if (dataRepository.isFile())
       {
           mFilePatterns.add(thisPath);
       }
       else if (dataRepository.isDirectory())
       {
           final File[] sub = dataRepository.listFiles();
           for (int i = 0; i < sub.length; i++)
           {
               collectPatterns(sub[i], thisPath);
           }
       }
   }
   
    /** Returns the lazy initialized document builder. */
    private DocumentBuilder getDocumentBuilder ()
       throws ParserConfigurationException
    {
        if (mDocumentBuilder == null)
        {
            mDocumentBuilder = DocumentBuilderFactory.newInstance().
                     newDocumentBuilder();
        }
        return mDocumentBuilder;
    }

    
    private class Pair
    {
       private final String mFileName;
       private final String mLabel;
       public Pair (final String fileName, final String label)
       {
           super();
           mFileName = fileName;
           mLabel = label;
       }
       public String getFileName ()
       {
           return mFileName;
       }
       public String getLabel ()
       {
           return mLabel;
       }
   }

   
}

