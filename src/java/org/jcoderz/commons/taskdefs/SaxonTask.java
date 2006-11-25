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
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.jcoderz.commons.util.IoUtil;


/**
 * Ant task wrapper for Saxon.
 *
 * @author Michael Griffel
 */
public class SaxonTask
      extends Java
{
   private static final String SAXON_TRANSFORMER = "com.icl.saxon.StyleSheet";
   /** The output file. */
   private File mOutFile;
   /** The input file. */
   private File mInFile;
   /** The XSL stylesheet file. */
   private File mXslFile;
   private File mHtmlStyleSheet;
   private boolean mHaveRenderX;

   /**
    * Constructor.
    */
   public SaxonTask ()
   {
      super();
      super.setClassname(SAXON_TRANSFORMER);
      // defaults
      setFork(true);
      setFailonerror(true);
   }

   /**
    * Sets the XML input file that contains the document.
    * @param f the XML input file (log message info).
    */
   public void setIn (File f)
   {
      mInFile = f;

   }

   /**
    * Set the destination file into which the result is written.
    * @param f the name of the destination file.
    **/
   public void setOut (File f)
   {
       mOutFile = f;
   }

   /**
    * Sets the XSL stylesheet file.
    * @param f the XSL stylesheet file.
    */
   public void setXsl (File f)
   {
      mXslFile = f;
   }

   /**
    * Sets the CSS stylesheet file.
    * @param f the CSS stylesheet file.
    */
   public void setCss (File f)
   {
      mHtmlStyleSheet = f;
   }

   /**
    * @see Java#setClassname(String)
    */
   public void setClassname (String arg0)
         throws BuildException
   {
      throw new BuildException("classname attribute is not allowed!");
   }

   public void setHaveRenderX (boolean b)
   {
      mHaveRenderX = b;
   }


   /**
    * @see org.apache.tools.ant.Task#execute()
    */
   public void execute ()
         throws BuildException
   {
      createArg().setValue("-x");
      createArg().setValue("org.apache.xml.resolver.tools.ResolvingXMLReader");

      createArg().setValue("-y");
      createArg().setValue("org.apache.xml.resolver.tools.ResolvingXMLReader");

      createArg().setValue("-r");
      createArg().setValue("org.apache.xml.resolver.tools.CatalogResolver");

      createArg().setValue("-u");

      createArg().setValue("-o");
      createArg().setFile(mOutFile);
      createArg().setValue(mInFile.toURI().toString());
      createArg().setValue(mXslFile.toURI().toString());

      createJvmarg().setValue("-Djava.awt.headless=true");

      if (mHtmlStyleSheet != null)
      {
         log("Using CSS stylesheet " + mHtmlStyleSheet, Project.MSG_VERBOSE);
         final File out = new File(
               mOutFile.getParent(), mHtmlStyleSheet.getName());
         try
         {
            IoUtil.copy(mHtmlStyleSheet, out);
         }
         catch (IOException e)
         {
            throw new BuildException("Failed to copy CSS stylesheet "
                  + mHtmlStyleSheet + " to " + out, e);
         }
         createArg().setValue(
               "html.stylesheet=" + out.getName());
      }

      if (mHaveRenderX)
      {
         createArg().setValue(
               "xep.extensions=1");
      }

      super.execute();
   }
}
