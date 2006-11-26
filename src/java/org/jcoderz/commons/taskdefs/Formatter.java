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
package org.jcoderz.commons.taskdefs;

import java.io.File;
import java.util.Locale;

import org.apache.fop.tools.anttasks.Fop;
import org.jcoderz.commons.taskdefs.XtremeDocs.FormatterInfoData;


/**
 * Abstract class that defines the interface of a DocBook renderer.
 *
 * @author Michael Griffel
 */
public abstract class Formatter
{
   private final FormatterInfoData mInfoData;

   private Formatter (FormatterInfoData i)
   {
      mInfoData = i;
   }

   /**
    * Factory method to create a concrete instance of a formatter.
    * @param f the meta data object initialized by the ant task.
    * @return a concrete formatter instance.
    */
   public static Formatter getInstance (FormatterInfoData f)
   {
      if (f.getType() == null)
      {
         throw new IllegalArgumentException("formatter type cannot be null");
      }
      final String typeAsLowercase = f.getType().toLowerCase(Locale.US);
      final Formatter result;
      if (typeAsLowercase.equals("html"))
      {
         result = new HtmlFormatter(f);
      }
      else if (typeAsLowercase.equals("pdf"))
      {
         result = new PdfFormatter(f);
      }
      else
      {
         throw new NoSuchMethodError("Unsupported type " + f.getType());
      }
      return result;
   }

   /**
    * Transforms the given DocBook input file and writes the
    * result to <tt>out</tt>.
    * @param parent the parent task.
    * @param in the input file.
    * @param out the ouput file.
    */
   public abstract void transform (XtremeDocs parent, File in, File out);

   /**
    * Returns the filename extension.
    * For example, the DocBook to HTML formatter will return <tt>"html"</tt>.
    * @return teh filename extension.
    */
   public abstract String getFileExtension ();

   /**
    * Returns the formatter's meta data.
    * This data structure is set by the ant task.
    * @return the formatter's meta data.
    */
   public FormatterInfoData getInfoData ()
   {
      return mInfoData;
   }

   protected void executeSaxon (XtremeDocs parent, File in, final File out)
   {
      final SaxonTask task = new SaxonTask();
      task.setProject(parent.getProject());
      task.setTaskName("saxon");
      task.setCss(getInfoData().getCascadingStyleSheet());
      task.setXsl(getInfoData().getStyleSheet());
      task.setIn(in);
      task.setOut(out);
      task.setDir(in.getParentFile());
      task.setFailonerror(parent.failOnError());
      task.setClasspath(parent.getClassPath());
      task.setHaveRenderX(haveRenderX(parent));
      task.execute();
   }

   protected boolean haveRenderX (XtremeDocs parent)
   {
      return parent.getXepHome() != null && parent.getXepHome().isDirectory();
   }

   /**
    * Transforms DocBook to HTML.
    *
    * @author Michael Griffel
    */
   private static class HtmlFormatter
         extends Formatter
   {
      HtmlFormatter (FormatterInfoData i)
      {
         super(i);
      }

      /**
       * @see Formatter#transform(XtremeDocs, File, File)
       */
      public void transform (XtremeDocs parent, File in, File out)
      {
         executeSaxon(parent, in, out);
         parent.log("Transformed " + in + " successfully to " + out);
      }

      /**
       * @see Formatter#getFileExtension()
       */
      public String getFileExtension ()
      {
         return "html";
      }
   }

   /**
    * Transforms DocBook to PDF.
    *
    * @author Michael Griffel
    */
   private static class PdfFormatter
         extends Formatter
   {

      /**
       * Constructor.
       * @param i formatter's meta data.
       */
      PdfFormatter (FormatterInfoData i)
      {
         super(i);
      }

      /**
       * @see Formatter#transform(XtremeDocs, File, File)
       */
      public void transform (XtremeDocs parent, File in, File out)
      {
         final File tmp = new File(out.getParentFile(), out.getName() + ".fo");
         executeSaxon(parent, in, tmp);

         if (haveRenderX(parent))
         {
            executeXep(parent, in, out, tmp);
         }
         else
         {
            executeFop(parent, in, out, tmp);
         }
         parent.log("Transformed " + tmp + " successfully to " + out);
      }

      private void executeXep (XtremeDocs parent, File in, File out, File tmp)
      {
         final XepTask xep = new XepTask();
         xep.setProject(parent.getProject());
         xep.setTaskName("xep");
         xep.setFo(tmp);
         xep.setOut(out);
         xep.setXephome(parent.getXepHome());
         xep.execute();
      }

      private void executeFop (XtremeDocs parent, File in, File out, File tmp)
      {
         final Fop fop = new Fop();
         fop.setProject(parent.getProject());
         fop.setTaskName("fop");
         fop.setFofile(tmp);
         fop.setOutfile(out);
         fop.setFormat("application/pdf");
         fop.setMessagelevel("info");
         fop.setBasedir(in.getParentFile());
         fop.execute();
      }

      /**
       * @see Formatter#getFileExtension()
       */
      public String getFileExtension ()
      {
         return "pdf";
      }

   }

}
