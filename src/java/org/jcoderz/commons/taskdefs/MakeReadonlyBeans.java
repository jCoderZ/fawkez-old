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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;



/**
 * This task is used to modify a deployment descriptor, making a
 * copy of the given entity beans with their name changed from
 * FooEntity to FooReaderEntity. It also modifies the accompanying
 * weblogic specific deployment descriptors.
 * @author Albrecht Messner
 */
public class MakeReadonlyBeans
      extends Task
{
   private static final String STYLE_GENERIC = "make-readonly-bean-generic.xsl";
   private static final String STYLE_WEBLOGIC = "make-readonly-bean-wl.xsl";
   private static final String STYLE_WEBLOGIC_CMP
         = "make-readonly-bean-wlcmp.xsl";
   private static final String STYLE_GENERATE_UTIL
         = "generate-readonly-entity-util.xsl";

   /** The output directory. */
   private File mDestDir;

   /** The generic deployment descriptor (ejb-jar.xml) to transform. */
   private File mGenericDeploymentDescriptor = null;

   /**
    * The weblogic ejb deployment descriptor (weblogic-ejb-jar.xml) to
    * transform.
    */
   private File mWlsDeploymentDescriptor = null;

   /**
    * The weblogic RDBMS deployment descriptor (weblogic-cmp-rdbms-jar.xml)
    * to transform.
    */
   private File mWlsCmpDeploymentDescriptor = null;

   /** List of all beans that need to be made read-only. */
   private final List mReadonlyBeans = new ArrayList();

   /** force output of target files even if they already exist. */
   private boolean mForce = false;

   /** terminate ant build on error. */
   private boolean mFailOnError = false;

   /** The directory to which generated source files should be written. */
   private File mSourceDestDir;

   /**
    * Set the destination directory into which the XSL result
    * files should be copied to. This parameter is required.
    * @param dir the name of the destination directory.
    **/
   public void setDestdir (File dir)
   {
       mDestDir = dir;
   }

   public void setSourceDestdir (File dir)
   {
      mSourceDestDir = dir;
   }

   /**
    * Sets the force output of target files flag to the given value.
    *
    * @param b Whether we should force the generation of output files.
    */
   public void setForce (boolean b)
   {
      mForce = b;
   }

   /**
    * Set whether we should fail on an error.
    *
    * @param b Whether we should fail on an error.
    */
   public void setFailonerror (boolean b)
   {
      mFailOnError = b;
   }

   public void setDeploymentDescriptor (File f)
   {
      mGenericDeploymentDescriptor = f;
   }

   public void setWeblogicDeploymentDescriptor (File f)
   {
      mWlsDeploymentDescriptor = f;
   }

   public void setWeblogicCmpDeploymentDescriptor (File f)
   {
      mWlsCmpDeploymentDescriptor = f;
   }

   public ReadOnlyBean createReadOnlyBean ()
   {
      final ReadOnlyBean bn = new ReadOnlyBean();
      mReadonlyBeans.add(bn);
      return bn;
   }

   /** {@inheritDoc} */
   public void execute ()
         throws BuildException
   {
      try
      {
         checkFilesAndDirectories();

         if (mForce || checkIfBuildRequired())
         {
            final File genericDdTarget
                  = new File(mDestDir, mGenericDeploymentDescriptor.getName());
            transform(mGenericDeploymentDescriptor,
                  genericDdTarget,
                  STYLE_GENERIC);
            transform(mWlsDeploymentDescriptor,
                  new File(mDestDir, mWlsDeploymentDescriptor.getName()),
                  STYLE_WEBLOGIC);
            transform(mWlsCmpDeploymentDescriptor,
                  new File(mDestDir, mWlsCmpDeploymentDescriptor.getName()),
                  STYLE_WEBLOGIC_CMP);
            transform(genericDdTarget,
                  new File(mDestDir, "generate-utils-stdout.txt"),
                  STYLE_GENERATE_UTIL);
         }
         else
         {
            log("All files are up-to-date.", Project.MSG_INFO);
         }
      }
      catch (BuildException e)
      {
         if (mFailOnError)
         {
            throw e;
         }
         log(e.getMessage(), Project.MSG_ERR);
      }
   }

   private String createListOfReadonlyBeans ()
   {
      final StringBuffer sbuf = new StringBuffer();
      for (final Iterator it = mReadonlyBeans.iterator(); it.hasNext(); )
      {
         if (sbuf.length() == 0)
         {
            sbuf.append('|');
         }
         final ReadOnlyBean bean = (ReadOnlyBean) it.next();
         sbuf.append(bean.getName());
         sbuf.append('|');
      }
      if (sbuf.length() == 0)
      {
         sbuf.append("||");
      }
      return sbuf.toString();
   }

   private boolean checkIfBuildRequired ()
   {
      final File genericOutFile
            = new File(mDestDir, mGenericDeploymentDescriptor.getName());
      final File weblogicOutFile
            = new File(mDestDir, mWlsDeploymentDescriptor.getName());
      final File weblogicCmpOutFile
            = new File(mDestDir, mWlsCmpDeploymentDescriptor.getName());
      boolean buildRequired = false;
      if (! genericOutFile.exists()
            || genericOutFile.lastModified()
               < mGenericDeploymentDescriptor.lastModified())
      {
         buildRequired = true;
      }
      if (! weblogicOutFile.exists()
            || weblogicOutFile.lastModified()
               < mWlsDeploymentDescriptor.lastModified())
      {
         buildRequired = true;
      }
      if (! weblogicCmpOutFile.exists()
            || weblogicCmpOutFile.lastModified()
               < mWlsCmpDeploymentDescriptor.lastModified())
      {
         buildRequired = true;
      }
      return buildRequired;
   }

   private void checkFilesAndDirectories ()
   {
      checkFile("genericDeploymentDescriptor", mGenericDeploymentDescriptor);
      checkFile("weblogicDeploymentDescriptor", mWlsDeploymentDescriptor);
      checkFile("weblogicCmpDeploymentDescriptor", mWlsCmpDeploymentDescriptor);
      checkDirectory("destdir", mDestDir);
      checkDirectory("sourceDestdir", mSourceDestDir);
   }

   private void checkFile (String paramName, File file)
   {
      if (file == null)
      {
         throw new BuildException(
               "Mandatory attribute '" + paramName + "' is missing.");
      }
      if (! file.exists())
      {
         throw new BuildException("File '" + file + "' does not exist");
      }
   }

   private void checkDirectory (String paramName, File directory)
   {
      if (directory == null)
      {
         throw new BuildException(
               "Mandatory attribute '" + paramName + "' missing.");
      }
      if (! directory.exists())
      {
         throw new BuildException(
               "Output directory '" + directory + "' does not exist.");
      }
      if (! directory.isDirectory())
      {
         throw new BuildException("'" + directory + "' is not a directory");
      }
   }

   /**
    * Execute the XSL transformation.
    * @throws BuildException if an error during transformation occurs.
    */
   private void transform (File inFile, File outFile, String xslFile)
         throws BuildException
   {
      try
      {
         // Xalan2 transformator is required,
         // that why we explicit use this factory
         final TransformerFactory factory
               = new org.apache.xalan.processor.TransformerFactoryImpl();
         factory.setURIResolver(new JarArchiveUriResolver(this));

         final InputStream xslStream
            = LogMessageGenerator.class.getResourceAsStream(xslFile);

         final Transformer transformer
               = factory.newTransformer(new StreamSource(xslStream));

         transformer.setParameter("outdir", mDestDir.getAbsolutePath());
         transformer.setParameter("srcdir", mSourceDestDir.getAbsolutePath());
         transformer.setParameter("bean-names", createListOfReadonlyBeans());

         final StreamResult out = new StreamResult(outFile);

         transformer.transform(getSaxSource(inFile), out);
      }
      catch (Exception e)
      {
         throw new BuildException("Error during transformation: " + e, e);
      }
   }

   private SAXSource getSaxSource (File xmlFile)
         throws SAXException, FileNotFoundException
   {
      final XMLReader xr = XMLReaderFactory.createXMLReader(
            "org.apache.xerces.parsers.SAXParser");
      xr.setEntityResolver(new DummyEntityResolver(this));
      final FileInputStream fileInStream = new FileInputStream(xmlFile);
      final InputSource inSource = new InputSource(fileInStream);
      return new SAXSource(xr, inSource);
   }

   public final class ReadOnlyBean
   {
      private String mName;

      /**
       * @return Returns the beanName.
       */
      public String getName ()
      {
         return mName;
      }

      /**
       * @param beanName The beanName to set.
       */
      public void setName (String beanName)
      {
         mName = beanName;
      }
   }
}
