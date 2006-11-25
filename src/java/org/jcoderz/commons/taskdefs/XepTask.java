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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FileSet;

/**
 * Ant task to run the xep (renderX) fo processor.
 *
 * @author Michael Griffel
 */
public class XepTask
      extends Java
{
   private static final String XEP_CLASS = "com.renderx.xep.XSLDriver";
   private File mFoFile;
   private File mOutFile;
   private File mXepHome;

   /**
    * Constructor.
    */
   public XepTask ()
   {
      super();
      super.setClassname(XEP_CLASS);
      setTaskName("xep");
      // defaults
      setFork(true);
      setFailonerror(true);
   }

   public void setFo (File fo)
   {
      mFoFile = fo;
   }

   public void setOut (File out)
   {
      mOutFile = out;
   }

   public void setXephome (File dir)
   {
      mXepHome = dir;
   }
   /**
    * @see Java#setClassname(String)
    */
   public void setClassname (String arg0)
         throws BuildException
   {
      throw new BuildException("classname attribute is not allowed!");
   }

   /**
    * @see org.apache.tools.ant.Task#execute()
    */
   public void execute ()
         throws BuildException
   {
      // TODO: check attributes.

      final FileSet fs = new FileSet();
      fs.setDir(new File(mXepHome, "/lib"));
      fs.setIncludes("*.jar");
      createClasspath().addFileset(fs);

      createJvmarg().setValue("-Dcom.renderx.xep.ROOT=" + mXepHome);
      createJvmarg().setValue("-Djava.awt.headless=true");

      createArg().setValue("-fo");
      createArg().setFile(mFoFile);

      createArg().setValue("-pdf");
      createArg().setFile(mOutFile);

      super.execute();
   }

}
