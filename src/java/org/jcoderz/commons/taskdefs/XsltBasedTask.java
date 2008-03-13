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
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.jcoderz.commons.util.StringUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class implements common functionality for XSLT based Ant tasks.
 *
 * @author Michael Griffel
 */
public abstract class XsltBasedTask
      extends Task
{
   /** System property for the XML Parser Configuration (Xalan2). */
   private static final String XML_PARSER_CONFIGURATION_PROPERTY
         = "org.apache.xerces.xni.parser.XMLParserConfiguration";

   /** Xalan2 XML Parser Configuration w/ XInclude support. */
   private static final String XML_PARSER_CONFIG_WITH_XINCLUDE
         = "org.apache.xerces.parsers.XIncludeParserConfiguration";

   /** The fawkeZ VERSION file. */
   private static final String FAWKEZ_VERSION_FILE
         = "/org/jcoderz/commons/VERSION";

   /** The destination directory. */
   private File mDestDir = null;

   /** The XSL stylesheet file. */
   private String mXslFile = null;

   /** The Input XML document (log message info fil) to be used. */
   private File mInFile = null;

   /** The Output file. */
   private File mOutFile = null;

   /** force output of target files even if they already exist. */
   private boolean mForce = false;

   /** terminate ant build on error. */
   private boolean mFailOnError = false;

   private boolean mResolveExternalEntities = true;

   /**
    * Set the destination directory into which the XSL result
    * files should be copied to. This parameter is required.
    * @param dir the name of the destination directory.
    **/
   public void setDestdir (File dir)
   {
       mDestDir = dir;
   }

   /**
    * Sets the XSL file that is used to generate the log message info classes.
    * @param s the XSL file to use.
    */
   public void setXsl (String s)
   {
      mXslFile = s;
   }

   /**
    * Sets the XML input file that contains the log message info document.
    * @param f the XML input file (log message info).
    */
   public void setIn (File f)
   {
      mInFile = f;
   }

   /**
    * Sets the ouput file.
    * @param f The output file.
    */
   public void setOut (File f)
   {
      mOutFile = f;
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

   /**
    * Execute this task.
    *
    * @throws BuildException An building exception occured.
    */
   public void execute ()
         throws BuildException
   {
      try
      {
         checkAttributes();

         if (mForce || mInFile.lastModified() > mOutFile.lastModified())
         {
            if (mDestDir != null)
            {
               log("Generating files to directory " + mDestDir);
            }
            log("Processing " + mInFile + " to " + mOutFile
                  + " using stylesheet " + mXslFile, Project.MSG_VERBOSE);
            transform();
            postExecute();
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

   /**
    * @return the fawkez version used for build.
    */
   public String getFawkezVersionAsString ()
   {
      final StringBuffer version = new StringBuffer();
      try
      {
         final Properties fawkezProps
               = getFawkezVersionProperties();
         version.append("fawkeZ ");
         version.append(fawkezProps.getProperty("version"));
         version.append(", [");
         version.append(fawkezProps.getProperty("cvs_name"));
         version.append(']');
      }
      catch (Exception x)
      {
         // sorry, we cannot read fawkeZ VERSION file
         version.append("unknown");
      }
      return version.toString();
   }

   /**
    * If set to <tt>false</tt>, external entities will not be resolved.
    * @param b new value.
    */
   public void resolveExternalEntities (boolean b)
   {
      mResolveExternalEntities = b;
   }

   static void checkXercesVersion (Task task)
   {
       final String xercesVersion =
           org.apache.xerces.impl.Version.getVersion();
       if (StringUtil.contains(xercesVersion, ("2.6.2")))
       {

           task.log("Found " + xercesVersion + " on classpath.",
               Project.MSG_WARN);
           task.log("This Version only supports the outdated 2003 "
               + "namespace for XInclude ",
               Project.MSG_WARN);
           task.log("please put a newer version of xerxes on your classapth or use",
               Project.MSG_WARN);
           task.log("at least ANT 1.7.0.", Project.MSG_WARN);
           // TODO: Add hint how to do this + throw exception?
       }
   }

   /**
    * Returns the build-in default stylesheet file name that
    * should be used by XSL transformer.
    * <p>
    * The stylesheet must be stored in the
    * <tt>/org/jcoderz/commons/taskdefs</tt> directory.
    * @return the default stylesheet file name.
    */
   abstract String getDefaultStyleSheet ();

   /**
    * This method can be overwritten by subclasses to set additional
    * transformer parameters.
    * @param transformer the XSL transformer.
    */
   void setAdditionalTransformerParameters (Transformer transformer)
   {
      // NOP
   }


   File getInFile ()
   {
      return mInFile;
   }

   File getOutFile ()
   {
      return mOutFile;
   }

   File getDestDir ()
   {
      return mDestDir;
   }

   boolean getFailOnError ()
   {
      return mFailOnError;
   }

   /**
    * Checks the attributes provided by this class.
    * @throws BuildException
    */
   void checkAttributes ()
         throws BuildException
   {
      checkAttributeInFile();
      checkAttributeOutFile();
      checkAttributeDestDir();
      checkAttributeXslFile();
      checkXercesVersion(this);
   }

   void checkAttributeXslFile ()
   {
      if (mXslFile == null || !new File(mXslFile).exists())
      {
         mXslFile = getDefaultStyleSheet();
      }
   }

   void checkAttributeDestDir ()
   {
      if (mDestDir == null)
      {
         throw new BuildException(
               "Missing mandatory attribute 'outdir'.", getLocation());
      }
      AntTaskUtil.ensureDirectory(mDestDir);
   }

   void checkAttributeOutFile ()
   {
      if (mOutFile == null)
      {
         throw new BuildException(
               "Missing mandatory attribute 'out'.", getLocation());
      }
      AntTaskUtil.ensureDirectoryForFile(mOutFile);
   }

   void checkAttributeInFile ()
   {
      if (mInFile == null)
      {
         throw new BuildException(
               "Missing mandatory attribute 'in'.", getLocation());
      }
      if (!mInFile.exists())
      {
         throw new BuildException(
               "Input file '" + mInFile + "' not found.", getLocation());
      }
   }

   /**
    * This method is the last callback in the execute method.
    * Can be overwritten by subclasses.
    */
   void postExecute ()
   {
      // NOP
   }

   /**
    * Execute the XSL transformation.
    * @throws BuildException if an error during transformation occurs.
    */
   void transform ()
         throws BuildException
   {
      try
      {
         final String xmlParserConfig
               = System.getProperty(XML_PARSER_CONFIGURATION_PROPERTY);
         if (!XML_PARSER_CONFIG_WITH_XINCLUDE.equals(xmlParserConfig))
         {
            System.setProperty(XML_PARSER_CONFIGURATION_PROPERTY,
                  XML_PARSER_CONFIG_WITH_XINCLUDE);
            log("Using XML Parser configuration "
                  + XML_PARSER_CONFIG_WITH_XINCLUDE, Project.MSG_VERBOSE);
         }

         // Xalan2 transformer is required,
         // that why we explicit use this factory
         final TransformerFactory factory
               = new org.apache.xalan.processor.TransformerFactoryImpl();

         factory.setURIResolver(new JarArchiveUriResolver(this));

         final InputStream xslStream = getXslFileAsStream();

         final Transformer transformer
               = factory.newTransformer(new StreamSource(xslStream));

         setAdditionalTransformerParameters(transformer);
         transformer.setParameter("outdir", mDestDir != null
               ? mDestDir.getAbsolutePath() : "");

         final Source xml = getInAsStreamSource();
         final StreamResult out = new StreamResult(mOutFile);

         transformer.setErrorListener(new MyErrorListener());
         transformer.transform(xml, out);
      }
      catch (TransformerException e)
      {
         throw new BuildException("Error during transformation: " + e, e);
      }
   }

   private InputStream getXslFileAsStream ()
   {
      final InputStream result;
      final InputStream xslStream
          = XsltBasedTask.class.getResourceAsStream(mXslFile);
        if (xslStream == null)
        {
            try
            {
                final InputStream xslFile = new FileInputStream(mXslFile);
                result = xslFile;
            }
            catch (FileNotFoundException e)
            {
                throw new BuildException("Cannot locate stylesheet "
                    + mXslFile);
            }
        }
        else
        {
            result = xslStream;
        }
        return result;
   }

   /**
    * @return a resource stream from in file.
    * @throws FileNotFoundException
    */
   Source getInAsStreamSource ()
   {
      final Source result;
      if (!mResolveExternalEntities)
      {
         final org.xml.sax.XMLReader reader;
         try
         {
            //reader = XMLReaderFactory.createXMLReader(
            //   "org.apache.xerces.parsers.SAXParser");
            reader = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
            reader.setEntityResolver(new DummyEntityResolver(this));
            result = new SAXSource(reader,
                     new InputSource(new FileInputStream(mInFile)));
         }
         catch (SAXException e)
         {
            throw new BuildException("Cannot create SAX XML Reader: " + e, e);
         }
         catch (FileNotFoundException e)
         {
            throw new BuildException("Ups, cannot open file: " + e, e);
         }
      }
      else
      {
         result =  new StreamSource(mInFile);
      }
      return result;
   }

   /**
    * Returns the VERSION file properties.
    * @return the VERSION file properties.
    * @throws IOException if the VERSION file cannot be found or read.
    */
   private Properties getFawkezVersionProperties ()
         throws IOException
   {
      final Properties props = new Properties();
      props.load(XsltBasedTask.class.getResourceAsStream(
            FAWKEZ_VERSION_FILE));
      return props;
   }

   private static class MyErrorListener
       implements ErrorListener
       {

    /** {@inheritDoc} */
    public void warning (TransformerException arg0)
            throws TransformerException
    {
        throw arg0;
    }

    /** {@inheritDoc} */
    public void error (TransformerException arg0)
            throws TransformerException
    {
        throw arg0;
    }

    /** {@inheritDoc} */
    public void fatalError (TransformerException arg0)
            throws TransformerException
    {
        throw arg0;
    }
   }
}
