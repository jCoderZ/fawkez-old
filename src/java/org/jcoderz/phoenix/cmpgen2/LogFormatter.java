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
package org.jcoderz.phoenix.cmpgen2;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author Albrecht Messner
 */
public class LogFormatter
   extends Formatter
{
   private static SimpleDateFormat sDateFormat =
      new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");

   private static final int MAX_METHOD_LENGTH = 25;

   /** {@inheritDoc} */
   public final String format (LogRecord record)
   {
      final String date = sDateFormat.format(new Date());
      final StringBuffer sbuf = new StringBuffer();
      sbuf.append(date).append(' ');
      // sbuf.append(Thread.currentThread().getName()).append(' ');
      sbuf.append(padSourceMethod(record.getSourceMethodName())).append(' ');
      sbuf.append(record.getLevel()).append(' ');
      sbuf.append(formatMessage(record));
      if (record.getThrown() != null)
      {
         final StringWriter sw = new StringWriter();
         final PrintWriter pw = new PrintWriter(sw);
         record.getThrown().printStackTrace(pw);
         pw.flush();
         sbuf.append('\n').append(sw.getBuffer().toString());
      }
      sbuf.append('\n');
      return sbuf.toString();
   }

   private String padSourceMethod (String sourceMethod)
   {
      final String result;
      if (sourceMethod.length() > MAX_METHOD_LENGTH)
      {
          result = sourceMethod.substring(0, MAX_METHOD_LENGTH);
      }
      else
      {
         final StringBuffer sbuf = new StringBuffer();
         sbuf.append(sourceMethod);
         {
            for (int i = 0; i < (MAX_METHOD_LENGTH - sourceMethod.length()); 
                i++)
            {
               sbuf.append(' ');
            }
         }
         result = sbuf.toString();
      }
      return result;
   }
}
