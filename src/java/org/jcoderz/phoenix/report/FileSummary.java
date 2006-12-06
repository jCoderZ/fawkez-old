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

/**
 * This class encapsulates data collected for a file or a group of files.
 *
 * @author Andreas Mandel
 */
public final class FileSummary
      implements Comparable
{
   /** 
    * Penalty weight for info level findings. One finding of this type
    * "destroys" the given number of good lines.
    */ 
   public static final int INFO_WEIGHT = 0;

   /** 
    * Penalty weight for coverage level findings. One finding of this type
    * "destroys" the given number of good lines.
    */ 
   public static final int COVERAGE_FAILURE_WEIGHT = 1;

   /** 
    * Penalty weight for warning level findings. One finding of this type
    * "destroys" the given number of good lines.
    */ 
   public static final int WARNING_WEIGHT = 4;

   /** 
    * Penalty weight for error level findings. One finding of this type
    * "destroys" the given number of good lines.
    */ 
   public static final int ERROR_WEIGHT = 12;

   /** Constant for percentage calculation 1% = 1 / MAX_PERCENTAGE. */
   private static final int MAX_PERCENTAGE = 100;

   private int mFiles = 0;

   private boolean mPercentUpToDate = false;
   private int mLinesOfCode;
   private int mCoveredLinesOfCode;
   private int mFiltered;
   private int mFalsePositive;
   private int mInfo;
   private int mCoverage;
   private int mWarning;
   private int mError;
   private int mCpd;


   private int mPercentOk;
   private int mPercentInfo;
   private int mPercentCoverage;
   private int mPercentWarning;
   private int mPercentError;

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
      result.append(mPercentOk);
      result.append("%), ");
      result.append(Severity.INFO.toString());
      result.append(':');
      result.append(mInfo);
      result.append('(');
      result.append(mPercentInfo);
      result.append("%), ");
      result.append(Severity.COVERAGE.toString());
      result.append(':');
      result.append(mCoverage);
      result.append(" of ");
      result.append(mCoveredLinesOfCode);
      result.append('(');
      result.append(mPercentCoverage);
      result.append("%), ");
      result.append(Severity.WARNING.toString());
      result.append(':');
      result.append(mWarning);
      result.append('(');
      result.append(mPercentWarning);
      result.append("%), ");
      result.append(Severity.ERROR.toString());
      result.append(':');
      result.append(mError);
      result.append('(');
      result.append(mPercentError);
      result.append("%)}");
      return result.toString();
   }

   public void add (FileSummary other)
   {
      mLinesOfCode += other.mLinesOfCode;
      mInfo += other.mInfo;
      mCoverage += other.mCoverage;
      mWarning += other.mWarning;
      mError += other.mError;
      mCoveredLinesOfCode += other.mCoveredLinesOfCode;
      mPercentUpToDate = false;
      mFiltered += other.mFiltered;
      mFalsePositive += other.mFalsePositive;
      mFiles++;
   }

   public void addCoveredLine ()
   {
      mPercentUpToDate = false;
      mCoveredLinesOfCode++;
   }

   public void addViolation (Severity severity)
   {
      mPercentUpToDate = false;
      if (severity == Severity.INFO)
      {
         mInfo++;
      }
      else if (severity == Severity.COVERAGE)
      {
         mCoverage++;
      }
      else if (severity == Severity.WARNING)
      {
         mWarning++;
      }
      else if (severity == Severity.ERROR)
      {
         mError++;
      }
      else if (severity == Severity.FALSE_POSITIVE)
      {
         mFalsePositive++;
      }
      else if (severity == Severity.FILTERED)
      {
         mFiltered++;
      }
      else if (severity == Severity.CPD)
      {
         mCpd++;
      }
      else
      {
         throw new RuntimeException("Unknown severity " + severity);
      }
   }

   /**
    * @return the full classname including package declaration.
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
         mPercentError = (mError * MAX_PERCENTAGE * ERROR_WEIGHT)
               / mLinesOfCode;
         if (mError > 0 && mPercentError == 0)
         {
            mPercentError = 1;
         }
         if (mPercentError > remainingPercentage)
         {
            mPercentError = remainingPercentage;
         }
         remainingPercentage -= mPercentError;
         // warning
         mPercentWarning = (mWarning * MAX_PERCENTAGE * WARNING_WEIGHT)
              / mLinesOfCode;
         if (mWarning > 0 && mPercentWarning == 0)
         {
            mPercentWarning = 1;
         }
         if (mPercentWarning > remainingPercentage)
         {
            mPercentWarning = remainingPercentage;
         }
         remainingPercentage -= mPercentWarning;
         // coverage
         if (mCoverageData)
         {
            calcPercentCoverage(remainingPercentage);
            remainingPercentage -= mPercentCoverage;
         }
         else
         {
            mPercentCoverage = 0;
         }
         // info
         mPercentInfo = (mInfo * MAX_PERCENTAGE * INFO_WEIGHT)
               / mLinesOfCode;
         if (mInfo > 0 && mPercentInfo == 0)
         {
            mPercentInfo = 1;
         }
         if (mPercentInfo > remainingPercentage)
         {
            mPercentInfo = remainingPercentage;
         }
         remainingPercentage -= mPercentInfo;
      }
      mPercentOk = remainingPercentage;
      mPercentUpToDate = true;
   }

   /**
    * Calculates the penalty percentage of the coverage tests.
    * The maximum persentage returned is the 
    * <code>remainingPercentage</code> given as argument.
    * The members <code>mCoveredLinesOfCode</code> and 
    * <code>mCoverage</code> should be uptodate before caling 
    * this method.
    * @param remainingPercentage the maximum penalti percentage 
    *     that is available.
    */
   private void calcPercentCoverage (int remainingPercentage)
   {
      if ((mCoveredLinesOfCode + mCoverage) == 0)
      {  // no coverage info at all!
         // TODO: Count as 0% or 100%???
//                  mPercentCoverage = remainingPercentage;
//                  remainingPercentage = 0;
         // -> no coverage is ok...
         mPercentCoverage = 0;
      }
      else
      {
         mPercentCoverage
               = (mCoverage * MAX_PERCENTAGE * COVERAGE_FAILURE_WEIGHT)
                  / (mCoveredLinesOfCode + mCoverage);
         if (mCoverage > 0 && mPercentCoverage == 0)
         {
            mPercentCoverage = 1;
         }
         if (mPercentCoverage > remainingPercentage)
         {
            mPercentCoverage = remainingPercentage;
         }
      }
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
         quality = calcUnwhightedQuality(mLinesOfCode, mInfo, mWarning, mError, 
                     mCoverage);
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
       return FileSummary.calculateQuality (mLinesOfCode, mInfo, mWarning,
               mError, mCoverage);
   }

   public static float calculateQuality (int loc, int info, int warning,
         int error, int coverage)
   {
      float quality = 0;
      if (loc > 0)
      {
         quality = calcUnwhightedQuality(loc, info, warning, error, coverage);
         quality = (quality * MAX_PERCENTAGE) / loc;
      }
      return quality;
   }

   /**
    * Calculates the unwighted quality points scored for the code.
    * Maximum returned is <code>loc</code> the minimum is <code>0</code>.
    * @param loc total number of lines of code. This is also the maximum 
    *         that might be returned by this method.
    * @param info number of info level findings.
    * @param warning number of warning level findings.
    * @param error number of error level findings.
    * @param coverage number of coverage level findings.
    * @return the unwighted quality score.
    */
   private static int calcUnwhightedQuality (int loc, int info, int warning, 
        int error, int coverage)
   {
       int quality = loc; // lines of code

       // not covered lines are bad this
       // not files with no coverage test at all get no penalty here!
       quality -= info * INFO_WEIGHT;
       quality -= coverage * COVERAGE_FAILURE_WEIGHT;
       quality -= warning * WARNING_WEIGHT; // warning lines are worse
       quality -= error * ERROR_WEIGHT; // error lines are errors
       if (quality < 0)
       {
          quality = 0;
       }
       return quality;
   }

   
   
   public String getPercentBar ()
   {
      calcPercent();
      final StringBuffer sb = new StringBuffer();
      sb.append("<table width='100%' cellspacing='0' cellpadding='0'>"
            + "<tr valign='middle'>");
      if (mPercentOk > 0)
      {
         sb.append("<td class=\"ok\" width='");
         sb.append(mPercentOk);
         sb.append("%' height='10'></td>");
      }
      if (mPercentInfo > 0)
      {
         sb.append("<td class=\"info\" width='");
         sb.append(mPercentInfo);
         sb.append("%' height=\"10\"></td>");
      }
      if (mPercentCoverage > 0)
      {
         sb.append("<td class=\"coverage\" width=\"");
         sb.append(mPercentCoverage);
         sb.append("%\" height=\"10\"></td>");
      }
      if (mPercentWarning > 0)
      {
         sb.append("<td class=\"warning\" width=\"");
         sb.append(mPercentWarning);
         sb.append("%\" height=\"10\"></td>");
      }
      if (mPercentError > 0)
      {
         sb.append("<td class=\"error\" width=\"");
         sb.append(mPercentError);
         sb.append("%\" height=\"10\"></td>");
      }
      sb.append("</tr></table>");
      return sb.toString();
   }

   public int getCoverage ()
   {
      int notCovered;
      if (mCoveredLinesOfCode != 0)
      {
         notCovered = (mCoverage * MAX_PERCENTAGE)
              / (mCoveredLinesOfCode + mCoverage);
         if ((notCovered == 0) && (mCoverage != 0))
         {  // below 1% -> round up to 1%
            notCovered = 1;
         }
      }
      else if (mCoverage != 0)
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
      // coverage is not part of this number any more.
      return mInfo + mWarning + mError;
   }

   public String getCoverageBar ()
   {
      final int notCovered = MAX_PERCENTAGE - getCoverage();

      final StringBuffer sb = new StringBuffer();
      sb.append("<table width='100%' cellspacing='0' cellpadding='0'>"
            + "<tr valign='middle'>");
      if (notCovered < MAX_PERCENTAGE)
      {
         sb.append("<td class=\"ok\" width=\"");
         sb.append(MAX_PERCENTAGE - notCovered);
         sb.append("%\" height=\"10\"></td>");
      }
      if (notCovered != 0)
      {
         sb.append("<td class=\"error\" width=\"");
         sb.append(notCovered);
         sb.append("%\" height=\"10\"></td></tr>");
      }
      sb.append("</table>");
      return sb.toString();
   }

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
   static class SortByPackage implements Comparator, Serializable
   {
      private static final long serialVersionUID = 2244367340241672131L;

      public int compare (Object o1, Object o2)
      {
         return ((FileSummary) o1).compareTo(o2);
      }
   }
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
