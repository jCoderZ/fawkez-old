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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import org.jcoderz.commons.util.Assert;
import org.jcoderz.phoenix.report.jaxb.File;
import org.jcoderz.phoenix.report.jaxb.Item;

/**
 * This class encapsulates all finding information collected for a 
 * file or a group of files.
 *
 * <p>This class also allows to perform the magic quality calculation for 
 * the data collected in the summary.</p>
 * 
 * @author Andreas Mandel
 */
public final class FileSummary
      implements Comparable
{
   /** Constant for percentage calculation 1% = 1 / MAX_PERCENTAGE. */
   private static final int MAX_PERCENTAGE = 100;

   private int mFiles = 0;

   /** Lines of code in the file. */
   private int mLinesOfCode;
   
   /** 
    * Lines of code in the file that contain coverage information that is
    * not 0.
    */ 
   private int mCoveredLinesOfCode;
   /**
    * Holds the number of violations for each severity level. 
    */
   private int[] mViolations = new int[Severity.VALUES.size()];

   /** 
    * Percentage values for the violations.
    * Data stored in here is only valid if <code>mPercentUpToDate</code> 
    * is true.
    */   
   private int[] mPercent = new int[Severity.VALUES.size()];
   private boolean mPercentUpToDate = false;

   private final String mClassname;
   private final String mPackage;
   private final String mDetailedFile;
   private final boolean mCoverageData;

   public FileSummary (String classname, String packagename,
                       boolean withCoverage)
   {
      this (classname, packagename, null, 0, withCoverage);
   }

   public FileSummary (String classname, String packagename,
                       String reportfile, int linesOfCode,
                       boolean withCoverage)
   {
      mClassname = classname;
      mPackage = packagename;
      mDetailedFile = reportfile;
      mLinesOfCode = linesOfCode;
      mCoverageData = withCoverage;
   }

   public String getClassname ()
   {
      return mClassname;
   }

   public String getPackage ()
   {
      return mPackage;
   }

   public int getNumberOfFiles ()
   {
      return mFiles;
   }

   public String toString ()
   {
      final StringBuffer result = new StringBuffer();
      calcPercent();

      result.append(getClassname());
      result.append("{ LOC:");
      result.append(mLinesOfCode);
      result.append('(');
      result.append(mViolations[Severity.OK.toInt()]);
      result.append("%)");
      final Iterator i = Severity.VALUES.iterator();
      while (i.hasNext())
      {
          final Severity s = (Severity) i.next();
          if (mViolations[s.toInt()] != 0)
          {
              result.append(", ");
              result.append(s.toString());
              result.append(':');
              result.append(mViolations[s.toInt()]);
              result.append('(');
              result.append(mPercent[s.toInt()]);
              result.append("%)");
          }
      }
      result.append('}');
      return result.toString();
   }

   /**
    * Add the counters of an other FileSummary to this one.
    * @param other the FileSummary be added.
    */
   public void add (FileSummary other)
   {
       for (int i = 0; i < mViolations.length; i++)
       {
           mViolations[i] += other.mViolations[i];
       }
       mLinesOfCode += other.mLinesOfCode;
       mCoveredLinesOfCode += other.mCoveredLinesOfCode;
       mPercentUpToDate = false;
       mFiles++;
    }

   public void addCoveredLine ()
   {
      mPercentUpToDate = false;
      mCoveredLinesOfCode++;
   }

   /**
    * Increments the counter for the given severity in this summary.  
    * @param severity the severity of the counter to be incremented.
    */
   public void addViolation (Severity severity)
   {
       Assert.notNull(severity, "severity");
       mPercentUpToDate = false;
       mViolations[severity.toInt()]++;
   }

   /**
    * @return the full class name including package declaration.
    */
   public String getFullClassname ()
   {
      final String fullClassname;
      if (mPackage == null || mPackage.length() == 0)
      {
         fullClassname = mClassname;
      }
      else
      {
         fullClassname = mPackage + "." + mClassname;
      }
      return fullClassname;
   }

   public String getHtmlLink ()
   {
      return mDetailedFile;
   }

   public int getLinesOfCode ()
   {
      return mLinesOfCode;
   }

   private void calcPercent ()
   {
      if (!mPercentUpToDate)
      {
         doCalcPercent();
      }
   }

   private void doCalcPercent ()
   {
      int remainingPercentage = MAX_PERCENTAGE;
      // errors
      if (mLinesOfCode != 0)
      {
          for (int i = Severity.ERROR.toInt(); i > Severity.INFO.toInt(); i--)
          {
              int percent;
              
              if (i == Severity.COVERAGE.toInt())
              {
                  percent = calcPercentCoverage();
              }
              else
              {
                  percent = calcPercentage(
                      mViolations[i] * Severity.fromInt(i).getPenalty(),
                      mLinesOfCode * Severity.PENALTY_SCALE);
              }
              // do not round to 0.
              if (mViolations[i] > 0 && percent == 0)
              {
                  percent = 1;
              }
              if (percent > remainingPercentage)
              {
                  percent = remainingPercentage;
              }
              mPercent[i] = percent;
              remainingPercentage -= percent;
          }
      }
      else
      {
          for (int i = Severity.ERROR.toInt(); i > Severity.INFO.toInt(); i--)
          {
              mPercent[i] = 0;
          }
      }
      mPercent[Severity.OK.toInt()] = remainingPercentage;
      mPercentUpToDate = true;
   }

   /**
    * Calculates the penalty percentage of the coverage tests.
    */
   private int calcPercentCoverage ()
   {
       final int coverageViolationPercentage;
       final int notCoveredLines = mViolations[Severity.COVERAGE.toInt()];
       if (!mCoverageData)
       {  
           coverageViolationPercentage = 0;
       }
       else 
       {
           coverageViolationPercentage = calcPercentage(
               notCoveredLines * Severity.COVERAGE.getPenalty(),
               Severity.PENALTY_SCALE 
                   * (mCoveredLinesOfCode + notCoveredLines)); 
       }
       return coverageViolationPercentage;
    }
   
    private static int calcPercentage (int part, int all)
    {
        final int result;
        if (all == 0)
        {
            result = 0;
        }
        else
        {
            result = part * MAX_PERCENTAGE / all;
        }
        return result;
    }

   /**
    * Returns the magic quality as percentage int. 
    * The maximum quality code gets a score of 100. The lowest score
    * possible is 0.
    * @return the magic quality as percentage int (0-100).
    */
   public int getQuality ()
   {
      int quality = 0;
      if (mLinesOfCode > 0)
      {
         quality = calcUnweightedQuality(mLinesOfCode, mViolations);
         quality = (quality * MAX_PERCENTAGE) / mLinesOfCode;
      }
      return quality;
   }

   /**
    * Returns the magic quality as percentage float. 
    * The maximum quality code gets a score of 100. The lowest score
    * possible is 0.
    * @return the magic quality as percentage float (0.0-100.0).
    */
   public float getQualityAsFloat ()
   {
       // might be we should cache the result?
       return FileSummary.calculateQuality (mLinesOfCode, mViolations);
   }

   public static float calculateQuality (int loc, int[] violations)
   {
      float quality = 0;
      if (loc > 0)
      {
         quality = calcUnweightedQuality(loc, violations);
         quality = (quality * MAX_PERCENTAGE) / loc;
      }
      return quality;
   }

   /**
    * Calculates the unweighted quality points scored for the code.
    * Maximum returned is <code>loc</code> the minimum is <code>0</code>.
    * @param loc total number of lines of code. This is also the maximum 
    *         that might be returned by this method.
    * @param info number of info level findings.
    * @param warning number of warning level findings.
    * @param error number of error level findings.
    * @param coverage number of coverage level findings.
    * @return the unweighted quality score.
    */
   private static int calcUnweightedQuality (int loc, int[] violations)
   {
       Assert.assertEquals(
           "Violations array length must match number of severities.", 
           Severity.VALUES.size(), violations.length);
       int quality = loc * Severity.PENALTY_SCALE; // lines of code
       for (int i = 0; i < Severity.VALUES.size() && quality > 0; i++)
       {
           // not covered lines are bad this
           // not files with no coverage test at all get no penalty here!
           quality -= violations[i] * Severity.fromInt(i).getPenalty();
       }
       if (quality < 0)
       {
          quality = 0;
       }
       else
       {
           quality /= Severity.PENALTY_SCALE;
       }
       return quality;
   }
   
   public String getPercentBar ()
   {
      calcPercent();
      final StringBuffer sb = new StringBuffer();
      sb.append("<table width='100%' cellspacing='0' cellpadding='0' " 
            + "summary='quality-bar'><tr valign='middle'>");
      for (int i = Severity.OK.toInt(); i < Severity.MAX_SEVERITY_INT; i++)
      {
          if (mPercent[i] > 0)
          {
             sb.append("<td class='");
             sb.append(Severity.fromInt(i).toString());
             sb.append("' width='");
             sb.append(mPercent[i]);
             sb.append("%' height='10'></td>");
          }
      }
      sb.append("</tr></table>");
      return sb.toString();
   }

   /**
    * Returns the number of violations for the given severity.
    * @param severity the severity to check.
    * @return the number of violations for the given severity
    */
   public int getViolations (Severity severity)
   {
       return mViolations[severity.toInt()];
   }

   public int getCoverage ()
   {
       Assert.assertTrue("Unexpected call if no coverage data is available.", 
           mCoverageData);

       final int notCoveredLinesOfCode = mViolations[Severity.COVERAGE.toInt()];
       
       int notCovered;
       if (mCoveredLinesOfCode != 0)
       {
           notCovered = (notCoveredLinesOfCode * MAX_PERCENTAGE)
              / (mCoveredLinesOfCode + notCoveredLinesOfCode);
           if ((notCovered == 0) && (notCoveredLinesOfCode > 0))
           {  // below 1% -> round up to 1%
               notCovered = 1;
           }
       }
       else if (notCoveredLinesOfCode > 0)
       {
           notCovered = MAX_PERCENTAGE;
       }
       else // no coverage at all (might be interface...
       {
           notCovered = 0;
       }
       return MAX_PERCENTAGE - notCovered;
   }

   public int getNumberOfFindings ()
   {
       int sum = 0;
       for (int i = 0; i < Severity.COVERAGE.toInt(); i++)
       {
           sum += mViolations[i];
       }
       return sum;
   }

   public String getCoverageBar ()
   {
      final int notCovered = MAX_PERCENTAGE - getCoverage();

      final StringBuffer sb = new StringBuffer();
      sb.append("<table width='100%' cellspacing='0' cellpadding='0' "
            + "summary='coverage-bar'><tr valign='middle'>");
      if (notCovered < MAX_PERCENTAGE)
      {
         sb.append("<td class='ok' width='");
         sb.append(MAX_PERCENTAGE - notCovered);
         sb.append("%' height='10'></td>");
      }
      if (notCovered != 0)
      {
         sb.append("<td class='error' width='");
         sb.append(notCovered);
         sb.append("%' height='10'></td></tr>");
      }
      sb.append("</tr></table>");
      return sb.toString();
   }

   /** 
    * Adds the counters from the given file to this summary.
    * @param file the data to be added.
    */
   public void add (File file)
   {
       final Iterator i = file.getItem().iterator();
       mFiles++;
       mLinesOfCode += file.getLoc(); 
       while (i.hasNext())
       {
          final Item item = (Item) i.next();
          final Severity severity = item.getSeverity();
          addViolation(severity);
       }
   }
   
   /** {@inheritDoc} */
   public int compareTo (Object o)
   {
      int result = 0;
      if (mPackage != null)
      {
         result = mPackage.compareTo(((FileSummary) o).mPackage);
      }
      if (result == 0)
      {
         if (getClassname() != null)
         {
            result = getClassname().compareTo(
                  ((FileSummary) o).getClassname());
         }
      }
      return result;
   }

   /** 
    * @deprecated This method does not support the new severity levels. 
    */
   public static float calculateQuality (int loc, int info, int warning, 
       int error, int coverage)
   {
       final int[] violations = new int[Severity.VALUES.size()];
       violations[Severity.INFO.toInt()] = info;
       violations[Severity.COVERAGE.toInt()] = coverage;
       violations[Severity.WARNING.toInt()] = warning;
       violations[Severity.ERROR.toInt()] = error;
       return FileSummary.calculateQuality(loc, violations);
   }
   
   /**
    * Comparator that allows to sort the FileSummary by name of the package.
    * @author Andreas Mandel
    */
   static class SortByPackage implements Comparator, Serializable
   {
      private static final long serialVersionUID = 2244367340241672131L;

      public int compare (Object o1, Object o2)
      {
         return ((FileSummary) o1).compareTo(o2);
      }
   }

   /**
    * Comparator that allows to sort the FileSummary by quality.
    * @author Andreas Mandel
    */
   static class SortByQuality implements Comparator, Serializable
   {
      private static final long serialVersionUID = 1718175789352629538L;

      public int compare (Object o1, Object o2)
      {
         int result;
         final float qualityA = ((FileSummary) o1).getQualityAsFloat();
         final float qualityB = ((FileSummary) o2).getQualityAsFloat();
         if (qualityA < qualityB)
         {
            result = -1;
         }
         else if (qualityA > qualityB)
         {
            result = 1;
         }
         else
         {
            result = ((FileSummary) o1).compareTo(o2);
         }
         return result;
      }
   }

   /**
    * Comparator that allows to sort the FileSummary by coverage.
    * @author Andreas Mandel
    */
   static final class SortByCoverage implements Comparator, Serializable
   {
      private static final long serialVersionUID = -4275903074787742250L;

      public int compare (Object o1, Object o2)
      {
         final FileSummary a = (FileSummary) o1;
         final FileSummary b = (FileSummary) o2;

         final long coverA = a.getCoverage();
         final long coverB = b.getCoverage();

         final int result;
         if (coverA < coverB)
         {
            result = -1;
         }
         else if (coverA > coverB)
         {
            result = 1;
         }
         else
         {
            result = ((FileSummary) o1).compareTo(o2);
         }
         return result;
      }
   }
}
