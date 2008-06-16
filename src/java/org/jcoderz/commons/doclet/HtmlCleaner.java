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
package org.jcoderz.commons.doclet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.tidy.Configuration;
import org.w3c.tidy.Tidy;

/**
 * This class provides an easy interface to jTidy to clean up
 * html fragments as used within javadoc.
 *
 * @author Andreas Mandel
 */
public class HtmlCleaner
{
   /** The full qualified name of this class. */
   private static final String CLASSNAME = HtmlCleaner.class.getName();

   /** The logger to use. */
   private static final Logger logger = Logger.getLogger(CLASSNAME);

   private static final String FIX_HEADER
         = "<html><head><title>clean</title></head><body>";

   private static final String FIX_FOOTER
         = "</body></html>";

   private String mWarnings = "";
   private boolean mHasErrors = false;

   /**
    * Converts the given HTML fragment string into wellformed xhtml.
    * @param in the html fragment to be cleaned up.
    * @return a cleaned up wellformed xhtml version of the in string.
    */
   public String clean (CharSequence in)
   {
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, "clean(CharSequence)", in);
      }
      mHasErrors = false;
      final Tidy tidy = new Tidy();
      final String inData = FIX_HEADER + in + FIX_FOOTER;
      final StringWriter err = new StringWriter();
      String result = null;
      try
      {
         tidy.setCharEncoding(Configuration.UTF8);
         tidy.setMakeClean(true);
         tidy.setXmlOut(true);
         tidy.setRawOut(true);
         tidy.setNumEntities(true);
         tidy.setWraplen(0); // do not care about line length
         // tidy.setOnlyErrors(true);
         tidy.setErrout(new PrintWriter(err));

         final InputStream inStream = new ByteArrayInputStream(
               inData.getBytes("utf-8"));

         final ByteArrayOutputStream out = new ByteArrayOutputStream();

         tidy.parse(inStream, out);

         final String resultString = new String(out.toByteArray(), "utf-8");

         final int start = resultString.indexOf("<body>");
         final int end = resultString.lastIndexOf("</body>");

         if (start != -1 && end != -1)
         {
            result = resultString.substring(
                  start + "<body>\n".length(), end).trim();
         }
         else
         {
            result = "Invalid HTML could not be parsed.";
         }

         if (tidy.getParseWarnings() == 0 && tidy.getParseErrors() == 0)
         {
            mWarnings = "";
         }
         else
         {
            mWarnings = err.toString();
         }
         mHasErrors = (tidy.getParseErrors() == 0);
      }
      catch (Exception ex)
      {
         result = "Invalid HTML could not be parsed.";
         err.write(result);
         err.write("Got exception:");
         err.write(ex.toString());
         ex.printStackTrace(new PrintWriter(err));
         mWarnings = err.toString();
         logger.log(Level.FINER,
               "Could not handle html fragment. '" + in + "'." , ex);
         mHasErrors = true;
      }
      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, "clean(CharSequence)", result);
      }
      return result;
   }

   /**
    * Returns the warnings encountered during last clean.
    * @return the warnings encountered during last clean.
    */
   public String getWarnings ()
   {
      return mWarnings;
   }

   public boolean hasErrors ()
   {
      return mHasErrors;
   }
}

