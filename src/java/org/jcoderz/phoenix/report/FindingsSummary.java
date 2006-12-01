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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jcoderz.phoenix.report.jaxb.Item;

/**
 * This class holds all findings, by type and file for the project.
 *
 * This class in NOT thread save in any way.
 *
 * @author Andreas Mandel
 */
public final class FindingsSummary
{
   private static FindingsSummary sFindingsSummary = new FindingsSummary();

   private final Map mFindings = new HashMap();
   private int mOverallCounter = 0;

   private FindingsSummary ()
   {
      // singleton class only instantiated by the factory
   }

   /**
    * Utility method to get the Singleton.
    * @return the one and only findings summary object.
    */
   public static FindingsSummary getFindingsSummary ()
   {
      return sFindingsSummary;
   }

   /**
    * Adds the finding to the findings data structure.
    * All references and counters are updated.
    * @param finding the concrete item that was detected
    * @param file the FileSummary object of the detected finding.
    */
   public static void addFinding (Item finding, FileSummary file)
   {
      getFindingsSummary().getFindingSummary(finding)
         .addFinding(finding, file);
   }

   /**
    * Provides access to all findings of the given type.
    * @param findingType the type of the finding.
    * @param severity the severity of the finding.
    * @return a FindingSummary of all findings of
    *          the given FindingType, might be null if
    *          no such finding exists.
    */
   public FindingSummary getFindingSummary (FindingType findingType,
         Severity severity)
   {
      final FindingSummary result
         = (FindingSummary) mFindings.get(
               findingType.getSymbol() + "_" + severity.toString());
      return result;
   }

   public FindingSummary getFindingSummary (Item item)
   {
      final String key
         = item.getFindingType() + "_" + item.getSeverity().toString();
      // cast to make sure we get an exception once item.getFindingType
      // returns a real FindingType
      FindingSummary result = (FindingSummary) mFindings.get(key);

      if (result == null)
      {
         result = new FindingSummary(item);
      }
      return result;
   }

   public Map getFindings ()
   {
      return Collections.unmodifiableMap(mFindings);
   }

   public String toString ()
   {
      return "[FindingsSummary: " + mFindings + "(" + mOverallCounter + ")]";
   }


      public static void createOverallContent (Writer out) throws IOException
      {
         final FindingSummary[] allFindings 
                 = (FindingSummary[]) getFindingsSummary().getFindings().values().
                     toArray(new FindingSummary[0]);

         out.write("<h1><a href='index.html'>View by Classes</a></h1>");
         out.write("<h1>Findings - Overview</h1>");

         Arrays.sort(allFindings);

         Severity currentSeverity = null;

         final Iterator i = Arrays.asList(allFindings).iterator();
         out.write("<table border='0' cellpadding='0' cellspacing='0' "
                 + "width='95%'>");
         int row = 0;
         while (i.hasNext())
         {

            final FindingSummary summary = (FindingSummary) i.next();
            if (summary.getSeverity() != currentSeverity)
            {
               out.write("<tr><td colspan='3' class='severityheader'>");
               currentSeverity = summary.getSeverity();
               out.write("Severity: ");
               out.write(currentSeverity.toString());
               out.write("</div>\n");
               out.write("</td></tr>");
               row = 0;
            }
            row++;
            out.write("<tr class='" + currentSeverity
                  + Java2Html.toOddEvenString(row) + "'>");
            out.write("<td class='finding-counter'>");
            out.write("" + summary.getCounter());
            out.write("</td>");
            out.write("<td class='finding-origin'>");
            out.write(summary.getOrigin().toString());
            out.write("</td>");
            out.write("<td class='finding-data' width='100%'>");

            out.write("<a href='");
            out.write(summary.createFindingDetailFilename());
            out.write("'>");
   //         if (summary.isFindingsHaveSameMessage()
   //               && summary.getFindingMessage() != null)
   //         {
   //            out.write(summary.getFindingMessage());
   //         }
   //         else
            {
               out.write(summary.getFindingType().getShortText());
            }
            out.write("</td>");
            out.write("</tr>");
         }
         out.write("</table>");
      }



   /**
    * Holds all findings of a specific type.
    * @author Andreas Mandel
    */
   public final class FindingSummary implements Comparable
   {
      private final Map mOccurrences = new HashMap();
      private final Severity mSeverity;
      private final Origin mOrigin;
      private int mCounter;
      private boolean mFindingsHaveSameMessage = true;
      private final String mFindingMessage;
      private final FindingType mFindingType;

      public FindingSummary (Item finding)
      {
         final String key
               = finding.getFindingType() + "_"
                  + finding.getSeverity().toString();
         mFindingType = FindingType.fromString(finding.getFindingType());
         mSeverity = finding.getSeverity();
         mOrigin = finding.getOrigin();
         mFindingMessage = finding.getMessage();
         mFindings.put(key, this);
      }

      /**
       * @return Returns the counter.
       */
      public int getCounter ()
      {
         return mCounter;
      }

      /**
       * @return Returns the counter.
       */
      public Origin getOrigin ()
      {
         return mOrigin;
      }

      /**
       * @return Returns the severity.
       */
      public Severity getSeverity ()
      {
         return mSeverity;
      }

      /**
       * @return Returns the findingMessage.
       */
      public String getFindingMessage ()
      {
         return mFindingMessage;
      }
      /**
       * @return Returns the findingsHaveSameMessage.
       */
      public boolean isFindingsHaveSameMessage ()
      {
         return mFindingsHaveSameMessage;
      }

      /**
       * @return Returns the finding type.
       */
      public FindingType getFindingType ()
      {
         return mFindingType;
      }

      /**
       * @return Returns the occurrences.
       */
      public Map getOccurrences ()
      {
         return Collections.unmodifiableMap(mOccurrences);
      }

      public FindingOccurrence getOccurrence (FileSummary fileSummary)
      {
         FindingOccurrence result =
            getOccurrence(fileSummary.getFullClassname());

         if (result == null)
         {
            result = new FindingOccurrence(fileSummary);
         }

         return result;
      }

      public void addFinding (Item finding, FileSummary summary)
      {
         if (mFindingsHaveSameMessage)
         {
            if (mFindingMessage == null)
            {
               mFindingsHaveSameMessage
                  = (finding.getMessage() == null);
            }
            else
            {
               mFindingsHaveSameMessage
                  = mFindingMessage.equals(finding.getMessage());
            }
         }
         getOccurrence(summary).addFinding(finding);
      }

      public String toString ()
      {
         return "[" + mFindingType + "(" + mSeverity
               + (mFindingsHaveSameMessage ? " " + mFindingMessage : "")
               + "): " + mOccurrences + "(" + mCounter + ")]";
      }


      /**
       * Be aware that the order (result of {@link #compareTo} can change
       * if new findings are added.
       * The order is from severe with most findings to info with
       * fewer findings.
       * @see java.lang.Comparable#compareTo(java.lang.Object)
       */
      public int compareTo (Object o)
      {
         final FindingSummary other = (FindingSummary) o;
         int result = -mSeverity.compareTo(other.mSeverity);
         if (result == 0)
         {
            result = other.mCounter - mCounter;
         }
         return result;
      }

      /**
       * @return Returns the occurrences.
       */
      private void addOccurrence (FindingOccurrence occurrence)
      {
         mOccurrences.put(occurrence.getFullClassname(), occurrence);
      }

      private FindingOccurrence getOccurrence (String filename)
      {
         return (FindingOccurrence) mOccurrences.get(filename);
      }

      public String createFindingDetailFilename ()
      {
         return "finding-" + getSeverity() + "-"
            + getFindingType().getSymbol() + ".html";
      }

      public void createFindingTypeContent (Writer out)
         throws IOException
      {
         final FindingOccurrence[] allFindings 
                 = (FindingOccurrence[]) mOccurrences.values().toArray(
                     new FindingOccurrence[0]);

         Arrays.sort(allFindings);

         out.write("<h1><a href='index.html'>View by Classes</a></h1>");
         out.write("<h1><a href='findings.html'>Findings - Overview</a></h1>");

         out.write("<h1>");

         out.write(getSeverity().toString());
         out.write(" ");
         out.write(getFindingType().getShortText());
         out.write(" (");
         out.write(getOrigin().toString());
         out.write(")");
         out.write("</h1>\n");

         if (isFindingsHaveSameMessage()
               && getFindingMessage() != null)
         {
            out.write("<h2>");
            out.write(getFindingMessage());
            out.write("</h2>\n");
         }

         if (getWikiPrefix() != null)
         {
            out.write("<a href='" + getWikiPrefix()
                  + getFindingType().getSymbol()
                  + "'>Further info on the wiki.</a>");
         }

         out.write("<p>");
         out.write(getFindingType().getDescription());
         out.write("</p>");

         final Iterator i = Arrays.asList(allFindings).iterator();

         out.write("<p />");
         out.write("<table border='0' cellpadding='0' cellspacing='0' " 
                 + "width='95%'>");

         while (i.hasNext())
         {
            final FindingSummary.FindingOccurrence
               occurrence = (FindingOccurrence) i.next();

            out.write("<tr><td class='findingtype-counter'>");
            out.write(Integer.toString(occurrence.getFindings().size()));
            out.write("</td><td class='findingtype-class' width='100%'>");
//            out.write("<a href='");
//            out.write(occurrence.getHtmlLink());
//            out.write("'>");
            out.write(occurrence.getFullClassname());
            out.write("</td></tr>");

            out.write("<tr><td class='findingtype-data' colspan='2'>");
            final Iterator j = occurrence.getFindings().iterator();

            while (j.hasNext())
            {
               final Item item = (Item) j.next();
               out.write("<a href='");
               out.write(occurrence.getHtmlLink());
               out.write("#LINE");
               out.write(Integer.toString(item.getLine()));
               out.write("'>");
               if (!isFindingsHaveSameMessage() && item.getMessage() != null)
               {
                  out.write(item.getMessage());
               }
               out.write("&#160;[");
               out.write(Integer.toString(item.getLine()));
               if (item.getColumn() != 0)
               {
                  out.write(":");
                  out.write(Integer.toString(item.getColumn()));
               }
               out.write("]</a>");
               if (j.hasNext())
               {
                  out.write(", ");
               }
               if (!isFindingsHaveSameMessage() && item.getMessage() != null)
               {
                  out.write("<br />");
               }
            }
            out.write("</td></tr>");
         }
         out.write("</table>\n");
      }

      /**
       * Checks for the wiki prefix to be used.
       * @return the wiki prefix to be used.
       */
      private String getWikiPrefix ()
      {
         return System.getProperty(Java2Html.WIKI_BASE_PROPERTY);
      }


      /**
       * A occurrence of a finding.
       * This class encapsulates all findings of a single type in one file.
       * Be aware that the order (result of {@link #compareTo} can change
       * if new findings are added.
       *
       * @author Andreas Mandel
       */
      public final class FindingOccurrence implements Comparable
      {
         final FileSummary mFileSummary;
         final List mFindingsInFile = new ArrayList();

         private FindingOccurrence (FileSummary summary)
         {
            mFileSummary = summary;
            addOccurrence(this);
         }

         /**
          * @return the name of the package of this class/file
          */
         public String getPackagename ()
         {
            return mFileSummary.getPackage();
         }

         public void addFinding (Item finding)
         {
            mFindingsInFile.add(finding);
            mCounter++;
            mOverallCounter++;
         }

         public List getFindings ()
         {
            return Collections.unmodifiableList(mFindingsInFile);
         }

         /**
          * @return Classname including package.
          */
         public String getFullClassname ()
         {
            return mFileSummary.getFullClassname();
         }

         public String getClassname ()
         {
            return mFileSummary.getClassname();
         }

         public String getHtmlLink ()
         {
            return mFileSummary.getHtmlLink();
         }

         public int countFindingsInFile ()
         {
            return mFindingsInFile.size();
         }

         public String toString ()
         {
            return "[" + getClassname() + ": " + findingsToString()
                  + "(" + mFindingsInFile.size() + ")]";
         }

         public String findingsToString ()
         {
            final StringBuffer sb = new StringBuffer();
            sb.append("{");
            final Iterator i = mFindingsInFile.iterator();
            while (i.hasNext())
            {
               final Item finding = (Item) i.next();
               sb.append("@");
               sb.append(finding.getLine());
               sb.append(":");
               sb.append(finding.getColumn());
               if (i.hasNext())
               {
                  sb.append(", ");
               }
            }
            sb.append("}");
            return sb.toString();
         }

         /**
          * Be aware that the order (result of {@link #compareTo} can change
          * if new findings are added.
          * The order is from most findings to fewer findings.
          * @see java.lang.Comparable#compareTo(java.lang.Object)
          */
         public int compareTo (Object o)
         {
            return ((FindingOccurrence) o).mFindingsInFile.size()
               - this.mFindingsInFile.size();
         }
      }
   }
}
